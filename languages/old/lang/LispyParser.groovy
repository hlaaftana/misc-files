import groovy.transform.CompileStatic

@CompileStatic class Expression {}

@CompileStatic class AtomExpression extends Expression {
	String text

	AtomExpression(String t) { text = t }
}

@CompileStatic class BlockExpression extends Expression {
	List<Expression> content

	BlockExpression(List<Expression> exprs) { content = exprs }
}

@CompileStatic class CallExpression extends Expression {
	Expression value
	List<Expression> arguments

	CallExpression(List<Expression> expressions) {
		value = expressions[0]
		arguments = expressions.drop(1)
	}
}

@CompileStatic class LiteralExpression extends Expression {}

@CompileStatic class NumberExpression extends LiteralExpression {
	Number value

	NumberExpression(boolean type, StringBuilder[] arr) {
		StringBuilder x = arr[0]
		boolean t = false
		if ((t |= null != arr[1])) x.append('.').append(arr[1])
		if ((t |= null != arr[2])) x.append('e').append(arr[2])
		String r = x.toString()
		if (null == arr[3]) t ? new BigDecimal(r) : new BigInteger(r) else {
			if (type) {
				if (arr[3].length() == 0) value = new BigDecimal(r)
				else {
					int b = new Integer(arr[3].toString())
					if (b == 32) value = new Float(r)
					else if (b == 64) value = new Double(r)
					else throw new NumberFormatException("Invalid number of bits $b for explicit float")
				}
			} else {
				if (t) throw new NumberFormatException('Added exponent or fraction to explicit integer')
				else if (arr[3].length() == 0) value = new BigInteger(r)
				else {
					int b = new Integer(arr[3].toString())
					if (b == 8) value = new Byte(r)
					else if (b == 16) value = new Short(r)
					else if (b == 32) value = new Integer(r)
					else if (b == 64) value = new Long(r)
					else throw new NumberFormatException("Invalid number of bits $b for explicit integer")
				}
			}
		}
	}
}

@CompileStatic
class StringExpression extends LiteralExpression {
	String value

	StringExpression(String v) {
		value = v
	}
}

@CompileStatic
abstract class ExprBuilder<T extends Expression> {
	abstract T push(int cp)
}

@CompileStatic class AtomBuilder extends ExprBuilder<AtomExpression> {
	StringBuilder last = new StringBuilder()
	boolean escaped = false
	boolean bracketed

	AtomBuilder(boolean b) { bracketed = b }

	AtomExpression push(int cp) {
		if (bracketed && !escaped && cp == 41 || cp == 40 || cp == 93 ||
				cp == 91 || cp == 123 || cp == 34 || cp == 39) return new AtomExpression(last.toString())
		if (!bracketed && Character.isWhitespace(cp)) return new AtomExpression(last.toString())
		if (escaped) last.deleteCharAt(last.codePointCount(0, last.length()))
		last.appendCodePoint(cp)
		escaped = cp == 92
		null
	}
}

@CompileStatic class NumberBuilder extends ExprBuilder<NumberExpression> {
	static Map<Integer, String> stageNames = [(0): 'number', (1): 'fraction',
	                                          (2): 'exponent', (3): 'number type bits']
	StringBuilder[] arr = [new StringBuilder(), null, null, null]
	int stage = 0
	boolean newlyStage = true
	boolean type

	def init(int s) {
		stage = s
		arr[s] = new StringBuilder()
		newlyStage = true
	}

	NumberExpression push(int cp) {
		int up
		if (cp > 47 && cp < 58) {
			newlyStage = false
			arr[stage].appendCodePoint(cp)
		} else if (cp == 46) {
			if (stage == 0) { if (newlyStage) { arr[0].append('0') }; init 1 }
			else throw new NumberFormatException('Tried to put fraction after ' + stageNames[stage])
		} else if (!newlyStage && cp == 101 || cp == 69) {
			if (stage < 2) init 2
			else throw new NumberFormatException('Tried to put exponent after ' + stageNames[stage])
		} else if ((up = Character.toUpperCase(cp)) == 73 || up == 70)
			if (stage == 3) throw new NumberFormatException('Tried to put number type bits after number type bits')
			else {
				type = up == 70
				init 3
			}
		else if (newlyStage && stage != 3) throw new NumberFormatException('Started number but wasnt number')
		else return new NumberExpression(type, arr)
		null
	}
}

@CompileStatic class StringExprBuilder extends ExprBuilder<StringExpression> {
	StringBuilder last = new StringBuilder()
	boolean escaped = true
	int quote

	StringExprBuilder(int q) {
		quote = q
	}

	StringExpression push(int cp) {
		if (!escaped && cp == q) return new StringExpression(last.toString())
		last.appendCodePoint(cp)
		escaped = cp == 92
		null
	}
}

@CompileStatic class CallBuilder extends ExprBuilder<CallExpression> {
	List<Expression> expressions = []
	ExprBuilder last = null
	boolean bracketed

	CallBuilder(boolean b) { bracketed = b }

	@Override
	CallExpression push(int cp) {
		if (null == last) {
			if (cp == 40) last = new AtomBuilder(true)
			else if (cp == 91) last = new CallBuilder(true)
			else if (cp == 123) last = new BlockBuilder(true) // blockbuilder
			else if ((cp > 47 && cp < 58) || cp == 46) (last = new NumberBuilder()).push(cp)
			else if (cp == 34 || cp == 39) last = new StringExprBuilder(cp)
			else if (!Character.isWhitespace(cp)) last = new AtomBuilder(false)
		} else {
			Expression x = last.push(cp)
			if (null != x) {
				expressions.add(x)
				last = null
			}
		}
		bracketed && cp == 93 && null == last ? new CallExpression(expressions) : null
	}
}

@CompileStatic class BlockBuilder extends ExprBuilder<BlockExpression> {
	List<Expression> expressions = []
	CallBuilder last = null
	boolean bracketed

	BlockBuilder(boolean b) { bracketed = b }

	@Override
	BlockExpression push(int cp) {
		if (null == last)
			if (cp == 91) last = new CallBuilder(true)
			else if (!Character.isWhitespace(cp)) (last = new CallBuilder(false)).push(cp)
		else if (cp == 10 || cp == 13 && !last.bracketed) {
			last.push(cp)
			CallExpression x = new CallExpression(last.expressions)
			expressions.add x.arguments ? x : x.value
		} else {
			CallExpression x = last.push(cp)
			if (null != x) {
				expressions.add x.arguments ? x : x.value
				last = null
			}
		}
		bracketed && cp == 125 && null == last ? new BlockExpression(expressions) : null
	}
}