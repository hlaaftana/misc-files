package hlaaftana.karmafields.lang.interpreter

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import hlaaftana.karmafields.lang.*

@CompileStatic
class Checker implements Value {
	Map<Token[], Token[]> aliases = [:]

	@Override
	Value propertyGet(String name) {
		if (name == 'aliases') {
			Map<ArrayValue, ArrayValue> map = [:]
			for (e in aliases) map.put(new ArrayValue(e.key), new ArrayValue(e.value))
			new MapValue(map)
		}
		else throw new ArgumentException('Invalid property ' + name + ' for checker')
	}

	@Override
	Value propertySet(String name, Value value) {
		if (checkSetProperty(name, value, 'aliases', MapValue)) {
			Map<Token[], Token[]> map = [:]
			for (e in ((MapValue) value).map)
				if (e.key instanceof ArrayValue)
					if (e.value instanceof ArrayValue) {
						Value[] ke = ((ArrayValue) e.key).array
						Value[] va = ((ArrayValue) e.value).array
						Token[] k = new Token[ke.length]
						Token[] v = new Token[va.length]
						Value a
						for (int i = 0; i < k.length; ++i) if ((a = ke[i]) instanceof Token)
							k[i] = (Token) a
						else throw new ArgumentException('Set aliases on checker iterating over key array not a token')
						for (int i = 0; i < v.length; ++i) if ((a = va[i]) instanceof Token)
							v[i] = (Token) a
						else throw new ArgumentException('Set aliases on checker iterating over value array not a token')
						map.put(k, v)
					} else throw new ArgumentException('Set aliases on checker with value not array')
				else throw new ArgumentException('Set aliases on checker with key not array')
			aliases = map
			value
		} else throw new ArgumentException('Invalid property ' + name + ' for checker')
	}

	@SuppressWarnings('GroovyPointlessBoolean')
	IsCheck parse(Scope scope, Token[] tokens) {
		if (!tokens) new DefiniteCheck(true)
		else {
			Token[][] ands = splitAnd(tokens)
			AndCheck a = new AndCheck()
			List<IsCheck> anders = []
			boolean anyNotDefiniteAnd = false
			boolean definiteAnd = true
			for (x in ands) {
				Token[][] ors = splitOr(x)
				if (ors.length == 0) {
					anders.add(new DefiniteCheck(true))
				} else if (ors.length == 1) {
					IsCheck single = parseSingle(scope, ors[0])
					if (false == single.definitely) {
						a.definitely = false
						return a
					} else if (!anyNotDefiniteAnd) {
						if (null != single.definitely)
							definiteAnd |= single.definitely
						else anyNotDefiniteAnd = true
					}
					anders.add(single)
				} else {
					OrCheck b = new OrCheck()
					List<IsCheck> orers = []
					boolean anyNotDefinite = false
					boolean definite = true
					for (y in ors) {
						IsCheck single = parseSingle(scope, y)
						if (single.definitely) {
							b.definitely = true
							break
						} else if (!anyNotDefinite) {
							if (null != single.definitely)
								definite |= single.definitely
							else anyNotDefinite = true
						}
						orers.add(single)
					}
					if (!anyNotDefinite) b.definitely = definite
					else if (null != b.definitely) b.checks = orers as IsCheck[]
					if (false == b.definitely) {
						a.definitely = false
						return a
					} else if (!anyNotDefiniteAnd) {
						if (null != b.definitely)
							definiteAnd |= b.definitely
						else anyNotDefiniteAnd = true
					}
					anders.add(b)
				}
			}
			if (!anyNotDefiniteAnd) a.definitely = definiteAnd
			else if (null != a.definitely) a.checks = anders as IsCheck[]
			a
		}
	}

	static Token[][] splitAnd(Token[] tokens) {
		List<List<Token>> split = [[]]
		for (t in tokens) {
			if (t instanceof ReferenceToken && t.text == 'or')
				split.add([])
			else split.last().add(t)
		}
		split as Token[][]
	}

	static Token[][] splitOr(Token[] tokens) {
		if (tokens.length == 0) return []
		List<List<Token>> split = [[]]
		for (t in tokens) {
			if (t instanceof ReferenceToken && t.text == 'or')
				split.add([])
			else split.last().add(t)
		}
		split as Token[][]
	}

	IsCheck parseSingle(Scope scope, Token[] tokens) {
		int x = tokens.size()
		if (x == 0) return new DefiniteCheck(true)
		Token first = tokens[0]
		Token[] foundAlias
		if (first instanceof ParensToken && x == 1) {
			parse(scope, ((ParensToken) first).tokens)
		} else if (first instanceof ReferenceToken) {
			String firstText = first.text
			if (x > 1) {
				Token second = tokens[1]
				String secondText = second.text
				if (firstText == 'not') new NotCheck(parseSingle(scope, tokens.drop(1)))
				else if (firstText == 'size') new SizeCheck(parseSingle(scope, tokens.drop(1)))
				else if (firstText == 'property') new PropertyCheck(tokens[1], parseSingle(scope, tokens.drop(2)))
				else if (firstText == 'subscript') new SubscriptCheck(tokens[1], parseSingle(scope, tokens.drop(2)))
				else if (firstText == 'iterate') new IterateCheck(parseSingle(scope, tokens.drop(1)))
				else if (firstText == 'compare') new ComparedCheck(tokens[1], parseSingle(scope, tokens.drop(2)))
				else if (firstText == 'check')
				else if (x == 2)
					if (firstText == 'of') new ClassCheck(scope, second)
					else if (firstText == 'in') new MembershipCheck(scope, second)
					else if (firstText == 'a' || firstText == 'an') new ACheck(scope, second)
					else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
					else throw new ArgumentException('Invalid check where first token is a '
								.concat('reference and has 2 tokens'))
				else if (x == 3)
					if (secondText == 'than')
						if (firstText == 'greater') new GreaterCheck(scope, tokens[2])
						else if (firstText == 'less') new LessCheck(scope, tokens[2])
						else if (firstText == 'bigger' || firstText == 'larger') new BiggerCheck(scope, tokens[2])
						else if (firstText == 'smaller') new SmallerCheck(scope, tokens[2])
						else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
						else throw new ArgumentException('Invalid check where first token is a '
									.concat('reference and has 3 tokens and the second one is \'than\''))
					else if (firstText == 'equal' && secondText == 'to')
						new EqualityCheck(scope, tokens[2])
					else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
					else throw new ArgumentException('Invalid check where first token is a '
								.concat('reference and has 3 tokens'))
				else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
				else throw new ArgumentException('Invalid check where first token is a '
							.concat('reference and has more than 1 token'))
			}
			else if (firstText == 'pure') new PureCheck()
			else if (firstText == 'code') new CodeCheck()
			else if (firstText == 'yes') new DefiniteCheck(true)
			else if (firstText == 'no') new DefiniteCheck(false)
			else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
			else throw new ArgumentException('Invalid check where the first and only token is a reference')
		} else if (x == 1) new EqualityCheck(scope, first)
		else if (null != (foundAlias = aliases.get(tokens))) parse(scope, foundAlias)
		else throw new ArgumentException('Invalid check where first token isn\'t a reference '
			.concat('and there are more than 1 tokens'))
	}
}

@CompileStatic
abstract class IsCheck {
	Boolean definitely

	abstract boolean doCheck(Value a)

	boolean check(Value a) {
		def x = definitely
		if (null != x) x
		else doCheck(a)
	}
}

@CompileStatic
class DefiniteCheck extends IsCheck {
	DefiniteCheck(boolean x) { definitely = x }

	@Override
	boolean doCheck(Value a) {
		throw new IllegalStateException('Check was thought to be definite but is not')
	}
}

@CompileStatic
abstract class ComparisonCheck extends IsCheck {
	Value value

	ComparisonCheck(Scope s, Token t) {
		value = Interpreter.evaluate(s, new Statement([t] as Token[]))
	}
}

@CompileStatic
@InheritConstructors
class EqualityCheck extends ComparisonCheck {
	@Override
	boolean doCheck(Value a) {
		a == value
	}
}

@CompileStatic
class ClassCheck extends ComparisonCheck {
	ClassCheck(Scope s, Token t) {
		super(s, t)
		if (!(value instanceof ClassValue))
			throw new ArgumentException('Class to check is not a class')
	}

	@Override
	boolean doCheck(Value a) {
		a instanceof ClassedValue && ((ClassValue) value).canLabel(((ClassedValue) a).type)
	}
}

@CompileStatic
class SizeCheck extends IsCheck {
	IsCheck check

	SizeCheck(IsCheck c) {
		check = c
	}

	@Override
	boolean doCheck(Value a) {
		check.check(a.size())
	}
}

@CompileStatic
@InheritConstructors
class GreaterCheck extends ComparisonCheck {
	@Override
	boolean doCheck(Value a) {
		a > value
	}
}

@CompileStatic
@InheritConstructors
class LessCheck extends ComparisonCheck {
	@Override
	boolean doCheck(Value a) {
		a < value
	}
}

@CompileStatic
@InheritConstructors
class BiggerCheck extends ComparisonCheck {
	@Override
	boolean doCheck(Value a) {
		a.size() > value.size()
	}
}

@CompileStatic
@InheritConstructors
class SmallerCheck extends ComparisonCheck {
	@Override
	boolean doCheck(Value a) {
		a.size() < value.size()
	}
}

@CompileStatic
class AndCheck extends IsCheck {
	IsCheck[] checks

	boolean doCheck(Value a) {
		for (c in checks)
			if (!c.check(a))
				return false
		true
	}
}

@CompileStatic
class OrCheck extends IsCheck {
	IsCheck[] checks

	boolean doCheck(Value a) {
		for (c in checks)
			if (c.check(a))
				return true
		false
	}
}

@CompileStatic
class NotCheck extends IsCheck {
	IsCheck check

	NotCheck(IsCheck c) {
		if (null != c.definitely)
			definitely = !c.definitely
		else check = c
	}

	boolean doCheck(Value a) {
		!check.check(a)
	}
}

@CompileStatic
class PureCheck extends IsCheck {
	@Override
	boolean doCheck(Value a) {
		a instanceof PureValue
	}
}

@CompileStatic
class CodeCheck extends IsCheck {
	@Override
	boolean doCheck(Value a) {
		// to add: codemacrovalue, codeclassvalue
		a instanceof CodeFunctionValue
	}
}

@CompileStatic
class ACheck<T extends Value> extends IsCheck {
	static Map<String, Class> classMap = [
		string: StringValue,
		number: NumberValue,
		classed: ClassedValue,
		no: NoValue,
		yes: YesValue,
		array: ArrayValue,
		class: ClassedValue,
		function: FunctionValue,
		macro: MacroValue,
		floating: FloatingValue,
		integer: IntegerValue,
		list: ListValue,
		map: MapValue
	]
	Class<T> clazz

	ACheck(Scope s, Token t) {
		String n
		if (t instanceof StringToken)
			n = new StringValue(((StringToken) t).value)
		else if (t instanceof ParensToken) {
			Token[] tokens = ((ParensToken) t).tokens
			Value x = Interpreter.evaluate(s, new Statement(tokens))
			if (x instanceof StringValue)
				n = ((StringValue) x).inner
			else throw new ArgumentException('Parentheses after valuetype check doesn\'t evaluate to string')
		} else if (t instanceof ReferenceToken) {
			n = t.text
		} else throw new SyntaxException('Token after valuetype check was none of reference, ' +
				'string or parentheses. ')
		if (null == (clazz = classMap.get(n)))
			throw new ArgumentException('Invalid valuetype ' + n)
	}

	@Override
	boolean doCheck(Value a) {
		clazz.isInstance(a)
	}
}

