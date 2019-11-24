package lang

import groovy.transform.CompileStatic

// this is bad and i have written better
@CompileStatic
class Parser {
	String indent
	String filename

	Block parse(String text) {
		blockize(lineize(text.readLines()))
	}

	Block teetee(String str) {
		List<Token> lastList = []
		Block b = new Block()
		Block lastBlock = b
		boolean comment = false
		int ln = 1
		int cl = 1
		boolean escaped = false
		boolean indenting = true
		int lastIndentSize = 0
		Integer indentSize = (Integer) null
		int currentIndentSize = 0
		final chars = str.chars
		for (int i = 0; i < chars.length; ++i) {
			final char ch1 = (char) chars[i]
			final int ch = (int) ch1
			def whitespace = Character.isWhitespace(ch1)
			boolean newline
			if (ch1 == (char) '\r') {
				newline = true
				if (chars[i + 1] == (char) '\n') ++i
			} else newline = ch1 == (char) '\n'
			if (newline) {
				++ln
				cl = 0
				if (!escaped) {
					currentIndentSize = 0
					comment = false
				}
			} else if (indenting) {
				if (whitespace) { ++currentIndentSize; continue }
				else {
					indenting = false
					if (currentIndentSize > lastIndentSize) {
						try {
							final lastState = lastBlock.code.last()
							lastBlock = new Block(parent: lastState)
							lastState.block = lastBlock
						} catch (ignored) {
							throw new SyntaxException('No indenting at top level')
						}
					} else if (currentIndentSize < lastIndentSize) {
						final times = (int) Math.ceil((lastIndentSize - currentIndentSize) / indentSize)
						for (int die = 0; die < times; ++die) lastBlock = lastBlock.parent.parent
					}
					if (null == indentSize && currentIndentSize != 0) indentSize = currentIndentSize
					lastIndentSize = currentIndentSize
				}
			}
			final Token last = lastList.empty ? (Token) null : lastList.last()
			if (!(last instanceof StringToken && !last.done) && ch1 == ((char) '#')) {
				comment = true
			}
			if (comment) continue
			/*{
				if (!escaped && newline) {
					last?.finish()
					lastBlock.code.add(new Statement(lastBlock, lastList))
					lastList = []
					comment = false
				}
			} else */
			if (whitespace) {
				def list2 = lastList
				def last2 = last
				boolean ever = false
				while (last2 instanceof TokensToken && !((TokensToken) last2).done) {
					ever = true
					list2 = ((TokensToken) last2).tokenBuilder
					last2 = ((TokensToken) last2).last()
				}
				if (last2 instanceof StringToken && !last2.done) last2.cp(ch)
				else {
					last2?.finish()
					if (newline && !ever) {
						lastBlock.code.add(new Statement(lastBlock, withoutWhitespace(lastList)))
						lastList = []
						indenting = true
					} else list2.add(new WhitespaceToken().at(ln, cl))
				}
			} else {
				if (ch1 >= ((char) '0') && ch1 <= ((char) '9'))
					digitCheck(lastList, last, ch, ln, cl)
				else if (ch == 46) // '.'
					dotCheck(lastList, last, ch, ln, cl)
				else if (ch == 40) // '('
					openParensCheck(lastList, last, ch, ln, cl)
				else if (ch == 41) // ')'
					closeParensCheck(lastList, last, ch, ln, cl)
				else if (ch == 91) // '['
					openBracksCheck(lastList, last, ch, ln, cl)
				else if (ch == 93) // ']'
					closeBracksCheck(last, ch, ln, cl)
				else if (ch == 34) // '"'
					quoteCheck(escaped, lastList, last, ch, ln, cl)
				else letterCheck(lastList, last, ch, ln, cl)
			}
			escaped = !escaped && ch == 92
			++cl
		}
		Token last = lastList.empty ? null : lastList.last()
		if (null != last && !last.done)
			if (last instanceof StringToken)
				throw new SyntaxException("Unfinished quote at file $filename line $ln")
			else if (last instanceof ParensToken)
				throw new SyntaxException("Unclosed parentheses at file $filename line $ln")
			else if (last instanceof SubscriptToken)
				throw new SyntaxException("Unclosed brackets at file $filename line $ln")
			else last.finish()
		lastBlock.code.add(new Statement(lastBlock, withoutWhitespace(lastList)))
		b
	}

	List<Token> tokenizeLine(String str, int ln) {
		List<Token> list = []
		int cl = str.length() - str.replaceAll(/^\s+/, '').length()
		boolean escaped = false
		for (char ch1: str.trim().toCharArray()) {
			int ch = (int) ch1
			final Token last = list.empty ? (Token) null : list.last()
			if ((null == last || last.done) && ch1 == ((char) '#')) break
			if (Character.isWhitespace(ch)) {
				def list2 = list
				def last2 = last
				while (last2 instanceof TokensToken && !((TokensToken) last2).done) {
					list2 = ((TokensToken) last2).tokenBuilder
					last2 = ((TokensToken) last2).last()
				}
				if (last2 instanceof StringToken) last2.cp(ch)
				else {
					last2.finish()
					list2.add(new WhitespaceToken().at(ln, cl))
				}
			} else {
				if (ch1 >= ((char) '0') && ch1 <= ((char) '9'))
					digitCheck(list, last, ch, ln, cl)
				else if (ch == 46) // '.'
					dotCheck(list, last, ch, ln, cl)
				else if (ch == 40) // '('
					openParensCheck(list, last, ch, ln, cl)
				else if (ch == 41) // ')'
					closeParensCheck(list, last, ch, ln, cl)
				else if (ch == 91) // '['
					openBracksCheck(list, last, ch, ln, cl)
				else if (ch == 93) // ']'
					closeBracksCheck(last, ch, ln, cl)
				else if (ch == 34) // '"'
					quoteCheck(escaped, list, last, ch, ln, cl)
				else letterCheck(list, last, ch, ln, cl)
			}
			escaped = ch == 92
			++cl
		}
		Token last = list.empty ? null : list.last()
		if (last && !last.done)
			if (last instanceof StringToken)
				throw new SyntaxException("Unfinished quote at file $filename line $ln")
			else if (last instanceof ParensToken)
				throw new SyntaxException("Unclosed parentheses at file $filename line $ln")
			else if (last instanceof SubscriptToken)
				throw new SyntaxException("Unclosed brackets at file $filename line $ln")
			else last.finish()
		withoutWhitespace(list)
	}

	List<Token> withoutWhitespace(List<Token> tokens) {
		List<Token> result = new ArrayList<>()
		for (t in tokens) {
			if (t instanceof ParensToken)
				result.add(new ParensToken(tokens: withoutWhitespace(((ParensToken) t).tokens)))
			else if (t instanceof SubscriptToken)
				result.add(new SubscriptToken(tokens: withoutWhitespace(((SubscriptToken) t).tokens)))
			else if (!(t instanceof WhitespaceToken)) result.add(t)
		}
		result
	}

	void quoteCheck(boolean escaped, List<Token> list, Token last, int ch, int ln, int cl) {
		if (null == last || last instanceof WhitespaceToken || last.done) {
			last?.finish()
			list.add(new StringToken().at(ln, cl))
		} else if (last instanceof StringToken && !escaped)
			last.finish()
		else if (last instanceof TokensToken && !((TokensToken) last).done)
			quoteCheck(escaped, ((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		else
			last.cp(ch)
	}

	void letterCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (null == last || last instanceof WhitespaceToken || last.done) {
			last?.finish()
			list.add(new ReferenceToken().at(ln, cl).start(ch))
		} else if (last instanceof TokensToken && !((TokensToken) last).done)
			letterCheck(((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		else if (last instanceof NumberToken) {
			NumberToken x = (NumberToken) last
			if (x.dotted || x.exponentiated)
				if (ch == 71 || ch == 103) // gG
					x.specified = BigDecimal
				else if (ch == 70 || ch == 102) // fF
					x.specified = Float
				else if (ch == 68 || ch == 100) // dD
					x.specified = Double
				else if (!x.exponentiated && ch == 69 || ch == 101) { // eE
					x.exponentiated = true
					x.cp(ch)
				} else { last.finish(); list.add(new ReferenceToken().at(ln, cl).start(ch)) }
			else
			if (ch == 71 || ch == 103) // gG
				x.specified = BigInteger
			else if (ch == 73 || ch == 105) // iI
				x.specified = Integer
			else if (ch == 76 || ch == 108) // lL
				x.specified = Long
			else if (ch == 46) { // .
				x.dotted = true
				x.cp(ch)
			} else { last.finish(); list.add(new ReferenceToken().at(ln, cl).start(ch)) }
		} else last.cp(ch)
	}

	void digitCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (null == last || last instanceof WhitespaceToken || last.done) {
			last?.finish()
			list.add(new NumberToken().at(ln, cl).start(ch))
		} else if (last instanceof TokensToken)
			digitCheck(((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		else
			last.cp(ch)
	}

	void dotCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (null == last || last instanceof WhitespaceToken) {
			last?.finish()
			list.add(new NumberToken().at(ln, cl).start('0.'))
		} else if (last instanceof TokensToken && !((TokensToken) last).done) {
			dotCheck(((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		} else {
			last.finish()
			list.add(new PropertyToken().at(ln, cl))
		}
	}

	void openParensCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (last instanceof TokensToken && !((TokensToken) last).done) {
			openParensCheck(((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		} else {
			last?.finish()
			list.add(new ParensToken().at(ln, cl))
		}
	}

	void closeParensCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (last instanceof TokensToken && !((TokensToken) last).done) {
			final tt = (TokensToken) last
			if (tt.last() instanceof TokensToken && !((TokensToken) tt.last()).done)
				closeParensCheck(tt.tokenBuilder, tt.last(), ch, ln, cl)
			else if (last instanceof ParensToken)
				((ParensToken) last).finish()
			else println('uhh')
		} else throw new SyntaxException("Invalid closed parentheses at $filename line $ln column $cl")
	}

	void openBracksCheck(List<Token> list, Token last, int ch, int ln, int cl) {
		if (last instanceof TokensToken && !((TokensToken) last).done) {
			openBracksCheck(((TokensToken) last).tokenBuilder, ((TokensToken) last).last(), ch, ln, cl)
		} else {
			last?.finish()
			list.add(new SubscriptToken().at(ln, cl))
		}
	}

	void closeBracksCheck(Token last, int ch, int ln, int cl) {
		if (last instanceof TokensToken && !((TokensToken) last).done) {
			final tt = (TokensToken) last
			if (tt.last() instanceof TokensToken && !((TokensToken) tt.last()).done)
				closeBracksCheck(tt.last(), ch, ln, cl)
			else if (last instanceof SubscriptToken)
				((TokensToken) last).finish()
			else println('uhh2')
		} else {
			throw new SyntaxException("Invalid closed brackets at $filename line $ln column $cl")
		}
	}

	List<Line> lineize(List<String> lines) {
		List<Line> newLines = []
		StringBuilder current = null
		boolean care = true
		int ln = 0
		for (l in lines) {
			String trimmed = l.trim()
			if (trimmed.empty) continue
			if (null == indent && Character.isWhitespace(l.codePointAt(0)))
				indent = (l =~ /^\s+/)[0]
			if (null != indent && l.startsWith(indent))
				if (current)
					if (care) current.append('\n').append(l)
					else current.append(l)
				else if (care)
					current = new StringBuilder(l)
				else
					newLines.last().content += l
			else if (current) {
				String c = current.toString()
				newLines.last().block.addAll(lineize(c.readLines()*.substring(indent.length())))
				current = null
			} else newLines.add(new Line(content: l))
			care = !trimmed.endsWith('\\') || (trimmed.length() > 1 && l[-2] == '\\')
		}
		newLines
	}

	Block blockize(List<Line> lines, Statement parent = null) {
		Block b = new Block()
		b.parent = parent
		int i = 0
		for (li in lines) {
			final s = new Statement(b, tokenizeLine(li.content, ++i))
			if (null != li.block) s.block = blockize(li.block, s)
			b.code.add(s)
		}
		b
	}

	static void printToken(Token t, boolean x) {
		if (x && !(t instanceof PropertyToken || t instanceof SubscriptToken)) print ' '
		if (t instanceof ReferenceToken) print "$t.text"
		else if (t instanceof PropertyToken) print ".$t.text"
		else if (t instanceof StringToken) print "\"$t.text\""
		else if (t instanceof NumberToken) print(((NumberToken) t).toNumberValue().inner)
		else if (t instanceof TokensToken) {
			final q = t instanceof SubscriptToken ? '[]' : '()'
			print q[0]
			printTokens(((TokensToken) t).tokens)
			print q[1]
		}
		else print t.text
	}

	static void printTokens(List<Token> tokens) {
		boolean x = false
		for (t in tokens) {
			printToken(t, x)
			x = true
		}
	}

	static void printStatement(Statement s, int ind = 0) {
		print ' ' * ind
		printTokens(s.content)
		println()
		if (s.block) printStatements(s.block.code, ind + 2)
	}

	static void printStatements(List<Statement> s, int ind = 0) {
		for (a in s) printStatement(a, ind)
	}

	static main(args) {
		File f = new File('old/lang/brainfuck.lip')
		try {
			printStatements(new Parser(filename: f.name).teetee(f.text).code)
		} catch (ex) {
			sleep 1000
			println()
			ex.printStackTrace()
		}
	}
}
