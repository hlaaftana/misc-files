import times, pegs, sequtils, json, tables

type
  JsonStringNode = concept n
    n.kind == JString
  JsonIntNode = concept n
    n.kind == JInt
  JsonFloatNode = concept n
    n.kind == JFloat
  JsonBoolNode = concept n
    n.kind == JBool
  JsonNullNode = concept n
    n.kind == JNull
  JsonObjectNode = concept n
    n.kind == JObject
  JsonArrayNode = concept n
    n.kind == JArray
  Nil = concept n
    n == nil
  Iterable[T] = concept i
    i.len is Ordinal
    items(i) is T
    for v in i:
      type(v) is T

proc getType*(node: JsonNode): typedesc =
  return
    case node.kind
    of JString:
      string
    of JInt:
      BiggestInt
    of JFloat:
      float
    of JBool:
      bool
    of JNull:
      nil
    of JObject:
      Table[string, JsonNode]
    of JArray:
      seq[JsonNode]

proc value*(node: JsonStringNode): string = node.str
proc value*(node: JsonIntNode): BiggestInt = node.num
proc value*(node: JsonFloatNode): float = node.fnum
proc value*(node: JsonBoolNode): bool = node.bval
proc value*(node: JsonNullNode): auto = nil
proc value*(node: JsonObjectNode): Table[string, any] =
  var nt: Table[string, any] = initTable[string, any]()
  for k, v in node.fields:
    nt[k] = v.value
  nt
proc value*(node: JsonArrayNode): seq[any] =
  node.elems.map(proc(n: JsonNode): any = n.value)

proc value*(t: Nil): auto = nil

proc parseDiscordJsonDate*(raw: string): TimeInfo =
  parse(raw.replace(peg"\.(\d+)"), "yyyy-MM-dd'T'HH-mm-sszzz")

proc enumValues*[T](enumType: typedesc[T]): Slice[T] =
  low(enumType)..high(enumType)

proc find*[T](enumType: typedesc[T], predicate: proc(item: T): bool) =
  for val in enumValues(enumType):
    if predicate(val):
      return val

proc find*[E, T](enumType: typedesc[T], value: E): T =
  find(enumType) do (item: T) -> bool:
    cast[E](item) == value

proc find*[T](coll: seq[T], predicate: proc(item: T): bool): T =
  for val in coll:
    if predicate(val):
      return val

proc find*[T](coll: openArray[T], predicate: proc(item: T): bool): T =
  for val in coll:
    if predicate(val):
      return val

proc toSeq*(iter: tuple): seq =
  for a in iter:
    result.add(a)

proc asBool*(iter: Iterable): bool = iter.len != 0
proc asBool*(num: SomeNumber): bool = num != 0
proc asBool*(a: bool): bool = a
proc asBool*(n: Nil): bool = false

template `vs`*(cond: typed|untyped, fall: typed|untyped): any =
  var r = cond
  if asBool(r):
    r
  else:
    fall

template json*(a: untyped): string =
  $(%*(a))