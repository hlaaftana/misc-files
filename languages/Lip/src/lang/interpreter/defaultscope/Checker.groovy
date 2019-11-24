package lang.interpreter.defaultscope

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import lang.*
import lang.interpreter.ArgumentException
import lang.interpreter.ArrayValue
import lang.interpreter.ClassValue
import lang.interpreter.ClassedValue
import lang.interpreter.CodeFunctionValue
import lang.interpreter.ExceptionValue
import lang.interpreter.FloatingValue
import lang.interpreter.FunctionValue
import lang.interpreter.IntegerValue
import lang.interpreter.Interpreter
import lang.interpreter.IteratorAdapter
import lang.interpreter.IteratorValue
import lang.interpreter.ListValue
import lang.interpreter.MacroValue
import lang.interpreter.MapValue
import lang.interpreter.NoValue
import lang.interpreter.NumberValue
import lang.interpreter.PureIterator
import lang.interpreter.PureValue
import lang.interpreter.Scope
import lang.interpreter.SetValue
import lang.interpreter.StringValue
import lang.interpreter.Value
import lang.interpreter.YesValue

@CompileStatic
class Checker {
	@SuppressWarnings('GroovyPointlessBoolean')
	static IsCheck parse(Scope scope, List<Token> tokens) {
		if (!tokens) new DefiniteCheck(true)
		else {
			final ands = splitAnd(tokens)
			AndCheck a = new AndCheck()
			List<IsCheck> anders = []
			boolean anyNotDefiniteAnd = false
			boolean definiteAnd = true
			for (x in ands) {
				final ors = splitOr(x)
				if (ors.empty) {
					anders.add(new DefiniteCheck(true))
				} else if (ors.size() == 1) {
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

	static List<List<Token>> splitAnd(List<Token> tokens) {
		List<List<Token>> split = [[]]
		for (t in tokens) {
			if (t instanceof ReferenceToken && t.text == 'or')
				split.add([])
			else split.last().add(t)
		}
		split
	}

	static List<List<Token>> splitOr(List<Token> tokens) {
		if (tokens.empty) return []
		List<List<Token>> split = [[]]
		for (t in tokens) {
			if (t instanceof ReferenceToken && t.text == 'or')
				split.add([])
			else split.last().add(t)
		}
		split
	}

	static IsCheck parseSingle(Scope scope, List<Token> tokens) {
		int x = tokens.size()
		if (x == 0) return new DefiniteCheck(true)
		Token first = tokens[0]
		if (first instanceof ParensToken && x == 1) {
			parse(scope, ((ParensToken) first).tokens)
		} else if (first instanceof ReferenceToken) {
			String firstText = first.text
			if (x > 1) {
				Token second = tokens[1]
				String secondText = second.text
				if (firstText == 'not') new NotCheck(parseSingle(scope, tokens.drop(1)))
				else if (firstText == 'size') new SizeCheck(parseSingle(scope, tokens.drop(1)))
				else if (firstText == 'check') new CheckCheck(scope, second, tokens[2], parseSingle(scope, tokens.drop(3)))
				else if (x == 2)
					if (firstText == 'of') new ClassCheck(scope, second)
					else if (firstText == 'in') new MembershipCheck(scope, second, tokens[2])
					else if (firstText == 'a' || firstText == 'an') new ACheck(scope, second)
					else throw new ArgumentException('Invalid check where first token is a '
								.concat('reference and has 2 tokens'))
				else if (x == 3)
					if (secondText == 'than')
						if (firstText == 'greater') new GreaterCheck(scope, tokens[2])
						else if (firstText == 'less') new LessCheck(scope, tokens[2])
						else if (firstText == 'bigger' || firstText == 'larger') new BiggerCheck(scope, tokens[2])
						else if (firstText == 'smaller') new SmallerCheck(scope, tokens[2])
						else throw new ArgumentException('Invalid check where first token is a '
									.concat('reference and has 3 tokens and the second one is \'than\''))
					else if (firstText == 'equal' && secondText == 'to')
						new EqualityCheck(scope, tokens[2])
					else throw new ArgumentException('Invalid check where first token is a '
								.concat('reference and has 3 tokens'))
				else throw new ArgumentException('Invalid check where first token is a '
							.concat('reference and has more than 1 token'))
			}
			else if (firstText == 'pure') new PureCheck()
			else if (firstText == 'code') new CodeCheck()
			else if (firstText == 'yes') new DefiniteCheck(true)
			else if (firstText == 'no') new DefiniteCheck(false)
			else throw new ArgumentException('Invalid check where the first and only token is a reference')
		} else if (x == 1) new EqualityCheck(scope, first)
		else throw new ArgumentException('Invalid check where first token isn\'t a reference '
			.concat('and there are more than 1 tokens'))
	}
}

@CompileStatic
abstract class IsCheck implements Value {
	Boolean definitely

	abstract boolean doCheck(Value a)

	@Override
	Value subscriptGet(Value value) {
		check(value) ? YesValue.INSTANCE : NoValue.INSTANCE
	}

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
		value = Interpreter.evaluate(s, new Statement(t instanceof ParensToken ? ((ParensToken) t).tokens : [t]))
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
class MembershipCheck extends ComparisonCheck {
	String name

	MembershipCheck(Scope s, Token n, Token t) {
		super(s, t)
		if (n instanceof ReferenceToken) name = n.text
		else if (n instanceof StringToken) name = ((StringToken) n).value
		else if (n instanceof ParensToken) name = s.eval(new Statement(((ParensToken) n).tokens))
		else throw new ArgumentException('First token of in check was not a reference, string or parens')
	}

	@Override
	boolean doCheck(Value a) {
		IteratorValue iter = a.iterator()
		while (iter.hasNext()) if ((++iter).propertyGet(name) == a) return true
		false
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
abstract class WrapperCheck extends IsCheck {
	IsCheck wrapped

	WrapperCheck(IsCheck w) { wrapped = w }

	abstract Value getValue(Value v)

	@Override
	boolean doCheck(Value a) {
		wrapped.check(getValue(a))
	}
}

@CompileStatic
class CheckCheck extends WrapperCheck {
	String name
	Scope parent
	Statement value

	CheckCheck(Scope s, Token n, Token v, IsCheck w) {
		super(w)
		if (n instanceof ReferenceToken) name = n.text
		else if (n instanceof StringToken) name = ((StringToken) n).value
		else if (n instanceof ParensToken) name = s.eval(new Statement(((ParensToken) n).tokens))
		else throw new ArgumentException('First token of check check was not a reference, string or parens')
		parent = s
		value = new Statement(v instanceof ParensToken ? ((ParensToken) v).tokens : [v])
	}

	Value getValue(Value v) {
		Scope s = parent.child()
		s.set(name, v)
		s.eval(value)
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
		map: MapValue,
		set: SetValue,
		iterator: IteratorValue,
		block: Block,
		statement: Statement,
		token: Token,
		checker: Checker,
		check: IsCheck,
		iteration: IteratorAdapter,
		javaIterator: IteratorAdapter,
		pure: PureValue,
		pureIterator: PureIterator,
		exception: ExceptionValue
	]
	Class<T> clazz

	ACheck(Scope s, Token t) {
		String n
		if (t instanceof StringToken)
			n = new StringValue(((StringToken) t).value)
		else if (t instanceof ParensToken) {
			final tokens = ((ParensToken) t).tokens
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

