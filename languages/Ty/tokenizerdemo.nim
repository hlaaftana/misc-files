import strutils, unicode, options

type
  TyTokenType* = enum
    pttComma, pttDot, pttQuestionMark, pttBackslash,
    pttString, pttNumber, pttWord, pttSymbol,
    pttIndent, pttIndentBack, pttNewLine, pttColon,
    pttOpenBrack, pttCloseBrack, pttOpenParen, pttCloseParen

type
  TyToken* = object
    case kind*: TyTokenType
    of pttString, pttNumber, pttWord, pttSymbol:
      raw*: string
    of pttNewLine:
      ampersand*: bool
    else: discard

## for unescape
proc parseBinInt*(s: string): int
## my unescapedSoda from Kismet
proc unescape*(str: string): string

{.push warning[ProveField]: off.}
proc `$`*(token: TyToken): string =
  result = case token.kind
  of pttComma: ","
  of pttDot: "."
  of pttQuestionMark: "?"
  of pttWord, pttNumber, pttSymbol: token.raw
  of pttString: "\"" & token.raw & "\""
  of pttIndent: "<indent>"
  of pttIndentBack: "<indentback>"
  of pttNewLine: "\p"
  of pttColon: ":"
  of pttOpenBrack: "["
  of pttCloseBrack: "]"
  of pttOpenParen: "("
  of pttCloseParen: ")"
  of pttBackslash: "\\"

proc tokenize*(str: string): seq[TyToken] =
  result = newSeq[TyToken]()
  var
    recorded: string
    recordedType: TyTokenType
    stringEscaped: bool
    stringQuote: Rune
    currentLineBars: int
    lastToken: Option[TyToken]
    lastIndents: seq[int] = @[]
    lastIndentSum: int
    recordingIndent: bool
    indent: int
    comment: bool

  template addToken(t: TyToken) =
    result.add(t)
    lastToken = some t

  template endRecording =
    if not recorded.isNil:
      var token = TyToken(kind: recordedType)
      token.raw = recorded
      addToken(token)
      recorded = nil

  template addTokenOf(tt: TyTokenType) =
    addToken(TyToken(kind: tt))

  for c in str.runes:
    if comment:
      if c == Rune('\l'):
        comment = false
      else: continue

    let w = c.isWhitespace

    if recordingIndent and c.char notin {'\l', '\r'}:
      if c == '\t'.Rune: inc indent, 4
      elif w: inc indent
      elif c != Rune('#'):
        let diff = indent - lastIndentSum
        if diff < 0:
          var d = -diff
          var am = 1
          for indt in lastIndents:
            if d > indt:
              dec d, indt
              inc am
            else: break
          for i in 1..am:
            dec lastIndentSum, lastIndents[^i]
            addTokenOf(pttIndentBack)
          lastIndents.setLen(lastIndents.len - am)
        elif diff > 0:
          lastIndents.add(diff)
          inc lastIndentSum, diff
          addTokenOf(pttIndent)
        indent = 0
        recordingIndent = false

    if not recorded.isNil:
      case recordedType
      of pttString:
        if stringEscaped:
          recorded.add('\\')
          recorded.add($c)
          stringEscaped = false
        elif c == stringQuote:
          endRecording()
          stringQuote = 0.Rune
        elif c == Rune('\\'): stringEscaped = true
        else: recorded.add($c)
        continue
      of pttNumber:
        if c.char in {'0'..'9', '.', 'e', 'E', '-', '+', 'i', 'f'}:
          recorded.add(c.char)
          continue
        else: endRecording()
      of pttSymbol:
        if w or c.char in {'0'..'9'} or c.isAlpha or c == '_'.Rune:
          endRecording()
        else:
          recorded.add($c)
          continue
      elif c.isAlpha or c == '_'.Rune:
        recorded.add($c)
        continue
      else: endRecording()

    case c
    of Rune('#'): comment = true
    of Rune(','): addTokenOf(pttComma)
    of Rune(':'): addTokenOf(pttColon)
    of Rune('.'): addTokenOf(pttDot)
    of Rune('\\'): addTokenOf(pttBackslash)
    of Rune('?'): addTokenOf(pttQuestionMark)
    of Rune('['): addTokenOf(pttOpenBrack)
    of Rune(']'): addTokenOf(pttCloseBrack)
    of Rune('('): addTokenOf(pttOpenParen)
    of Rune(')'): addTokenOf(pttCloseParen)
    of Rune('|'):
      addTokenOf(pttNewLine)
      addTokenOf(pttIndent)
      inc currentLineBars
    of Rune('^'):
      addTokenOf(pttNewLine)
      addTokenOf(pttIndentBack)
      dec currentLineBars
    of Rune('&'):
      addToken(TyToken(kind: pttNewLine, ampersand: true))
    of Rune('\''), Rune('"'):
      stringQuote = c
      recorded = ""
      recordedType = pttString
    elif w:
      if c == Rune('\l'):
        let ltAmpersand = block:
          if lastToken.isNone: false
          else:
            let lt = lastToken.unsafeGet
            lt.kind == pttNewLine and lt.ampersand
        if ltAmpersand:
          result.setLen(result.len - 1)
          lastToken = some result[^1]
        else:
          addTokenOf(pttNewLine)
          for _ in 1..currentLineBars:
            addTokenOf(pttIndentBack)
          currentLineBars = 0
          recordingIndent = true
      else: endRecording()
    else:
      recorded = $c
      recordedType =
        if c.isAlpha: pttWord
        elif c.char in '0'..'9': pttNumber
        else: pttSymbol
{.pop.}

proc parseBinInt*(s: string): int =
  let m = s.high
  for i, c in s.normalize:
    case c
    of '0', '_': discard
    of '1': result = result or 1 shl (m - i)
    else: raise newException(ValueError, "invalid binary: " & s) 

proc unescape*(str: string): string =
  result = newStringOfCap(str.len)
  var
    escaped = false
    recordU = false
    uFunc: proc(s: string): int = nil
    u: string = nil
  for c in str:
    if escaped:
      if not u.isNil:
        if recordU:
          if c == '}':
            recordU = false
            result.add($Rune(uFunc(u)))
            u = nil
            uFunc = parseHexInt
          else: u.add(c)
        else:
          case c
          of 'x', 'X': uFunc = parseHexInt
          of 'o', 'O': uFunc = parseOctInt
          of 'd', 'D': uFunc = parseInt
          of 'b', 'B': uFunc = parseBinInt
          of '{': recordU = true
          else:
            recordU = false
            result.add('\\')
            result.add('u')
            result.add(c)
            u = nil
          continue
      else:
        if c == 'u':
          u = newStringOfCap(4)
          continue
        else:
          result.setLen(result.len - 1)
          let ch = case c
            of 't': '\t'
            of '"': '"'
            of '\'': '\''
            of '\\': '\\'
            of 'r': '\r'
            of 'n': '\l'
            of 'f': '\f'
            of 'v': '\v'
            of 'a': '\a'
            of 'b': '\b'
            of 'e': '\e'
            else:
              result.add('\\')
              c
          result.add(ch)
    else: result.add(c)
    escaped = c == '\\'

when isMainModule:
  let tokens = tokenize(readFile("lang.ty"))
  var indent = ""
  var lastWasNl = false
  for t in tokens:
    if t.kind == pttNewLine:
      lastWasNl = true
      echo ""
    elif t.kind == pttIndent:
      indent &= "  "
    elif t.kind == pttIndentBack:
      indent.setLen(indent.len - 2)
    else:
      if lastWasNl:
        stdout.write(indent)
        lastWasNl = false
      stdout.write($t & " ")