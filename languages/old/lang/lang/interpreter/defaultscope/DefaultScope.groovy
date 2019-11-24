package hlaaftana.karmafields.lang.interpreter.defaultscope

import groovy.transform.CompileStatic
import hlaaftana.karmafields.lang.*
import hlaaftana.karmafields.lang.interpreter.*

import static Interpreter.evaluate
import static YesValue.INSTANCE as yes
import static NoValue.INSTANCE as no

@CompileStatic
class ClassClass extends ClassValue {
	static final ClassClass INSTANCE = new ClassClass()

	private ClassClass() {
		type = this
		name = 'Class'
		props.putAll name: new Property() { Value get(ClassedValue c) { new StringValue(c.type.name) } }
	}
}

@CompileStatic
class MacroByRefMacro extends MacroValue {
	{ precedence = -2; start = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Token[] cont = right.content
		Token first
		if (cont.length == 1) {
			MacroValue m
			if ((first = cont[0]) instanceof ReferenceToken) m = scope.macro(first.text)
			else if (first instanceof StringToken) m = scope.macro(((StringToken) first).value)
			else if (first instanceof ParensToken) {
				Value x = evaluate(scope, new Statement(right.parent, ((ParensToken) first).tokens))
				if (x instanceof StringValue) m = scope.macro(((StringValue) x).inner)
				else throw new ArgumentException('Evaluated parentheses on left side of macroByRef is not a string')
			}
			else throw new ArgumentException('macroByRef with invalid left side')
			null == m ? no : m
		} else throw new ArgumentException('Right value for macroByRef needs to be of length 1')
	}
}

@CompileStatic
class MacroLeftAssignMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		if (left.content.size() == 1 && left.content[0] instanceof ReferenceToken) {
			Value value = evaluate(scope, new Statement(right.parent, right.content, block))
			if (value instanceof MacroValue)
				scope.setMacro(left.content[0].text, (MacroValue) value)
			else
				throw new ArgumentException('Tried to set a non-macro value to a macro reference')
		} else
			throw new ArgumentException('Weird macro reference arguments')
	}
}

@CompileStatic
class UpscopeMacro extends MacroValue {
	{ alignment = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		int number
		Token[] cont = left.content
		Value x = evaluate(scope, right)
		if (x instanceof NumberValue) number = ((NumberValue) x).inner.intValue()
		else throw new ArgumentException('Right side of upscope doesn\'t evaluate to number')
		for (int i = 0; i < number; ++i) scope = scope.parent
		if (cont.length == 0) evaluate(scope, block)
		else evaluate(scope, new Statement(left.parent, cont, block))
	}
}

@CompileStatic
class LambdaMacro extends MacroValue {
	{ alignment = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		return null
	}
}

@CompileStatic
class IfMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond = evaluate(scope, right)
		if (cond != no)
			if (left.content.length == 0)
				evaluate(new Scope(scope), block)
			else
				evaluate(scope, new Statement(left.parent, left.content, block))
		else cond
	}
}

@CompileStatic
class UnlessMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond = evaluate(scope, right)
		if (cond == no)
			if (left.content.length == 0)
				evaluate(new Scope(scope), block)
			else
				evaluate(scope, new Statement(left.parent, left.content, block))
		else cond
	}
}

@CompileStatic
class WhileMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond
		while ((cond = evaluate(scope, right)) != no)
			if (left.content.length == 0)
				evaluate(new Scope(scope), block)
			else evaluate(scope, new Statement(left.parent, left.content, block))
		cond
	}
}

@CompileStatic
class UntilMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond
		while ((cond = evaluate(scope, right)) != no)
			if (left.content.length == 0)
				evaluate(new Scope(scope), block)
			else evaluate(scope, new Statement(left.parent, left.content, block))
		cond
	}
}

@CompileStatic
class BreakMacro extends MacroValue {
	{ start = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		throw new BreakException(right.content*.toString().join(''))
	}
}

@CompileStatic
class AssignMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Token[] cont = left.content
		Value value = evaluate(scope, right)
		Token first
		if (cont.length == 1)
			if ((first = cont[0]) instanceof ReferenceToken) scope.findSet(first.text, value)
			else if (first instanceof StringToken) scope.findSet(((StringToken) first).value, value)
			else if (first instanceof ParensToken) {
				Value x = evaluate(scope, new Statement(left.parent, ((ParensToken) first).tokens))
				if (x instanceof StringValue) scope.findSet(((StringValue) x).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of = is not a string')
			}
			else throw new ArgumentException('= with invalid left side')
		else {
			Value x = evaluate(scope, new Statement(left.parent, cont.dropRight(1), block))
			Token last = cont.last()
			if (last instanceof SubscriptToken)
				x.subscriptSet(evaluate(scope, new Statement(left.parent, ((SubscriptToken) last).tokens)), value)
			else if (last instanceof ReferenceToken || last instanceof NumberToken)
				x.propertySet(last.text, value)
			else if (last instanceof StringToken)
				x.propertySet(((StringToken) last).value, value)
			else if (last instanceof ParensToken) {
				Value y = evaluate(scope, new Statement(left.parent, ((ParensToken) last).tokens))
				if (y instanceof StringValue) x.propertySet(((StringValue) y).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of = with multiple tokens not string')
			} else throw new ArgumentException('= left side none of subscript, reference, number, string or parens')
		}
	}
}

@CompileStatic
class AssignFindMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Token[] cont = left.content
		Value value = evaluate(scope, right)
		Token first
		if (cont.length == 1)
			if ((first = cont[0]) instanceof ReferenceToken) scope.findSet(first.text, value)
			else if (first instanceof StringToken) scope.findSet(((StringToken) first).value, value)
			else if (first instanceof ParensToken) {
				Value x = evaluate(scope, new Statement(left.parent, ((ParensToken) first).tokens))
				if (x instanceof StringValue) scope.findSet(((StringValue) x).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of *= is not a string')
			}
			else throw new ArgumentException('*= with invalid left side')
		else throw new ArgumentException('The point of *= is to find and set variables with a name, not something else')
	}
}

@CompileStatic
class OrMacro extends MacroValue {
	{ precedence = 1 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		if (lef instanceof NoValue) evaluate(scope, right)
		else lef
	}
}

@CompileStatic
class AndMacro extends MacroValue {
	{ precedence = 2 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		if (lef instanceof NoValue) lef
		else evaluate(scope, right)
	}
}

@CompileStatic
class BorMacro extends MacroValue {
	{ precedence = 3 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner | ((NumberValue) ref).inner)
		else throw new ArgumentException('bor only works for numbers')
	}
}

@CompileStatic
class BxorMacro extends MacroValue {
	{ precedence = 4 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner ^ ((NumberValue) ref).inner)
		else throw new ArgumentException('bxor only works for numbers')
	}
}

@CompileStatic
class XorMacro extends MacroValue {
	{ precedence = 4 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		// inverting both sides in an xor operation returns the same result
		lef instanceof NoValue ^ ref instanceof NoValue ? yes : no
	}
}

@CompileStatic
class BandMacro extends MacroValue {
	{ precedence = 5 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner & ((NumberValue) ref).inner)
		else throw new ArgumentException('band only works for numbers')
	}
}

@CompileStatic
class EqMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef == ref ? yes : no
	}
}

@CompileStatic
class NeqMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef == ref ? no : yes
	}
}

@CompileStatic
class SameMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef.is(ref) ? yes : no
	}
}

@CompileStatic
class SamentMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef.is(ref) ? no : yes
	}
}

@CompileStatic
class CmpMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		NumberValue.get(lef <=> ref)
	}
}

@CompileStatic
class GreaterMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef > ref ? yes : no
	}
}

@CompileStatic
class GreaterEqMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef >= ref ? yes : no
	}
}

@CompileStatic
class LessMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef < ref ? yes : no
	}
}

@CompileStatic
class LessEqMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		lef <= ref ? yes : no
	}
}

@CompileStatic
class InMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (ref instanceof ArrayValue)
			((ArrayValue) ref).array.contains(lef) ? yes : no
		else throw new ArgumentException('Right value isn\'t array')
	} // TODO: for loop
}

@SuppressWarnings("GrUnnecessarySemicolon")
@CompileStatic
class IsMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Checker checker = scope.any('checker') ? scope.get('checker') : scope.set('checker', )
		scope.get('checker').parse(scope, right.content).check(lef) ? yes : no
	}
}

@SuppressWarnings("GrUnnecessarySemicolon")
@CompileStatic
class IsntMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		checker.parse(scope, right.content).check(lef) ? no : yes
	}
}

@CompileStatic
class LshMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner << ((NumberValue) ref).inner)
		else throw new ArgumentException('lsh only works for numbers')
	}
}

@CompileStatic
class RshMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner >> ((NumberValue) ref).inner)
		else throw new ArgumentException('rsh only works for numbers')
	}
}

@CompileStatic
class UrshMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner >>> ((NumberValue) ref).inner)
		else throw new ArgumentException('ursh only works for numbers')
	}
}

@CompileStatic
class PlusMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get((Number) ((NumberValue) lef).inner + ((NumberValue) ref).inner)
		else throw new ArgumentException('+ only works for numbers')
	}
}

@CompileStatic
class MinusMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get((Number) ((NumberValue) lef).inner - ((NumberValue) ref).inner)
		else throw new ArgumentException('- only works for numbers')
	}
}

@CompileStatic
class MultiplyMacro extends MacroValue {
	{ precedence = 9 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get((Number) ((NumberValue) lef).inner * ((NumberValue) ref).inner)
		else throw new ArgumentException('* only works for numbers')
	}
}

@CompileStatic
class DivideMacro extends MacroValue {
	{ precedence = 9 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner / ((NumberValue) ref).inner)
		else throw new ArgumentException('/ only works for numbers')
	}
}

@CompileStatic
class DivMacro extends MacroValue {
	{ precedence = 9 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner.intdiv(((NumberValue) ref).inner))
		else throw new ArgumentException('div only works for numbers')
	}
}

@CompileStatic
class ModMacro extends MacroValue {
	{ precedence = 9 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner % ((NumberValue) ref).inner)
		else throw new ArgumentException('mod only works for numbers')
	}
}

@CompileStatic
class NegMacro extends MacroValue {
	{ start = true; precedence = 10 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value ref = evaluate(scope, right)
		if (ref instanceof NumberValue)
			NumberValue.get(-((NumberValue) ref).inner)
		else throw new ArgumentException('neg only works for numbers')
	}
}

@CompileStatic
class PowMacro extends MacroValue {
	{ precedence = 11 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = evaluate(scope, left)
		Value ref = evaluate(scope, right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get((Number) ((NumberValue) lef).inner ** ((NumberValue) ref).inner)
		else throw new ArgumentException('^ only works for numbers')
	}
}

@CompileStatic
class ForgetMacro extends MacroValue {
	{ start = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Token t
		if (right.content.size() == 1)
			if ((t = right.content[0]) instanceof StringToken)
				scope.variables.remove(((StringToken) t).value)
			else if (t instanceof ReferenceToken)
				scope.variables.remove(t.text)
			else if (t instanceof ParensToken) {
				Value x = evaluate(scope, new Statement(left.parent, ((ParensToken) t).tokens))
				if (x instanceof StringValue) scope.variables.remove(((StringValue) x).inner)
				else throw new ArgumentException('Right side of forget must evaluate to string')
			} else throw new ArgumentException('Given weird token type ' + t.class + ' for forget')
		else throw new SyntaxException('Forget WHAT??????????')
	}
}

@CompileStatic
class HashFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		NumberValue.get(args[0].hashCode())
	}
}

@CompileStatic
class SizeFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		NumberValue.get(args[0].hashCode())
	}
}

@CompileStatic
class NewFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		if (args.length == 0 || !(args[0] instanceof ClassValue))
			throw new ArgumentException('new needs class argument')
		ClassValue c = (ClassValue) args[0]
		new ClassedValue(c, args.drop(1))
	}
}

@CompileStatic
class PropertyGetFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		if (args.length == 2)
			if (args[1] instanceof StringValue)
				args[0].propertyGet(((StringValue) args[1]).inner)
			else throw new ArgumentException('Tried to property get but the second argument was not a string')
		else throw new ArgumentException('Argument length for property get not 2')
	}
}

@CompileStatic
class PropertySetFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		if (args.length == 3)
			if (args[1] instanceof StringValue)
				args[0].propertySet(((StringValue) args[1]).inner, args[2])
			else throw new ArgumentException('Tried to property set but the second argument was not a string')
		else throw new ArgumentException('Argument length for property set not 3')
	}
}

@CompileStatic
class SubscriptGetFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		if (args.length == 2) args[0].subscriptGet(args[1])
		else throw new ArgumentException('Argument length for subscript get not 2')
	}
}

@CompileStatic
class SubscriptSetFunction extends FunctionValue {
	@Override
	Value call(Value... args) {
		if (args.length == 3) args[0].subscriptSet(args[1], args[2])
		else throw new ArgumentException('Argument length for subscript set not 3')
	}
}
// defaultScope.set('Block', new ClassValue(name: 'Block'))