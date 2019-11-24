package lang.interpreter.defaultscope

import groovy.transform.CompileStatic
import lang.*
import lang.interpreter.*

import static lang.interpreter.NoValue.INSTANCE as no
import static lang.interpreter.YesValue.INSTANCE as yes

@CompileStatic
class ClassClass extends ClassValue {
	static final ClassClass INSTANCE = new ClassClass()

	private ClassClass() {
		super(3)
		type = this
		name = 'Class'
		props.putAll name: new ClassValue.Property() { Value get(ClassedValue c) { new StringValue(c.type.name) } }
	}
}

@CompileStatic
class MacroByRefMacro extends MacroValue {
	{ precedence = -2; start = true }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		List<Token> cont = right.content
		Token first
		if (cont.size() == 1) {
			MacroValue m
			if ((first = cont[0]) instanceof ReferenceToken) m = scope.macro(first.text)
			else if (first instanceof StringToken) m = scope.macro(((StringToken) first).value)
			else if (first instanceof ParensToken) {
				Value x = scope.eval(new Statement(right.parent, ((ParensToken) first).tokens))
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
			Value value = scope.eval(new Statement(right.parent, right.content, block))
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
		List<Token> cont = left.content
		Value x = scope.eval(right)
		if (x instanceof NumberValue) number = ((NumberValue) x).inner.intValue()
		else throw new ArgumentException('Right side of upscope doesn\'t evaluate to number')
		for (int i = 0; i < number; ++i) scope = scope.parent
		if (cont.size() == 0) scope.eval(block)
		else scope.eval(new Statement(left.parent, cont, block))
	}
}

@CompileStatic
class IfMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond = scope.eval(right)
		if (cond != no)
			if (left.content.size() == 0)
				scope.child().eval(block)
			else
				scope.eval(new Statement(left.parent, left.content, block))
		else cond
	}
}

@CompileStatic
class UnlessMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond = scope.eval(right)
		if (cond == no)
			if (left.content.size() == 0)
				scope.child().eval(block)
			else
				scope.eval(new Statement(left.parent, left.content, block))
		else cond
	}
}

@CompileStatic
class WhileMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond
		while ((cond = scope.eval(right)) != no)
			if (left.content.size() == 0)
				scope.child().eval(block)
			else scope.eval(new Statement(left.parent, left.content, block))
		cond
	}
}

@CompileStatic
class UntilMacro extends MacroValue {
	{ alignment = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value cond
		while ((cond = scope.eval(right)) != no)
			if (left.content.size() == 0)
				scope.child().eval(block)
			else scope.eval(new Statement(left.parent, left.content, block))
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
class AppendMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		def list = scope.eval(left)
		def item = scope.eval(right)
		if (list instanceof ListValue) {
			((ListValue) list).list.add(item)
			list
		} else if (list instanceof SetValue) {
			((SetValue) list).set.add(item)
			list
		} else throw new ArgumentException('Durrrrrrrrrrrrrrr watt?????????')
	}
}

@CompileStatic
class AssignMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Assigner.assign(scope, left, block, scope.eval(right), ':=')
	}
}

@CompileStatic
class DoAssignMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value result = no
		for (l in Assigner.split(left))
			Assigner.assign(scope, new Statement(left.parent, l),
					block, (result = scope.eval(right)), "do=")
		result
	}
}

@CompileStatic
class EachAssignMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		IteratorValue iterator = scope.eval(right).iterator()
		Value result = no
		for (l in Assigner.split(left))
			Assigner.assign(scope, new Statement(left.parent, l),
				block, iterator.hasNext() ? (result = ++iterator) : no, "<<:=")
		result
	}
}

@CompileStatic
class AssignFindMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Assigner.assignFind(scope, left, block, scope.eval(right))
	}
}

@CompileStatic
class IncrementMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value result = no
		for (l in Assigner.split(left)) {
			Statement le = new Statement(left.parent, l)
			Value leftValue = scope.eval(le)
			if (leftValue instanceof NumberValue)
				leftValue = NumberValue.get(((Number) ((NumberValue) leftValue).inner + 1))
			else throw new ArgumentException('Can\'t increment non number yet')
			Assigner.assignFind(scope, le, block, leftValue)
		}
		result
	}
}

@CompileStatic
class DecrementMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value result = no
		for (l in Assigner.split(left)) {
			Statement le = new Statement(left.parent, l)
			Value leftValue = scope.eval(le)
			if (leftValue instanceof NumberValue)
				leftValue = NumberValue.get(((Number) ((NumberValue) leftValue).inner - 1))
			else throw new ArgumentException('Can\'t decrement non number yet')
			Assigner.assignFind(scope, le, block, leftValue)
		}
		result
	}
}

@CompileStatic
class OrMacro extends MacroValue {
	{ precedence = 1 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		if (lef instanceof NoValue) scope.eval(right)
		else lef
	}
}

@CompileStatic
class AndMacro extends MacroValue {
	{ precedence = 2 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		if (lef instanceof NoValue) lef
		else scope.eval(right)
	}
}

@CompileStatic
class BitorMacro extends MacroValue {
	{ precedence = 3 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner | ((NumberValue) ref).inner)
		else throw new ArgumentException('bitor only works for numbers')
	}
}

@CompileStatic
class BitxorMacro extends MacroValue {
	{ precedence = 4 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner ^ ((NumberValue) ref).inner)
		else throw new ArgumentException('bitxor only works for numbers')
	}
}

@CompileStatic
class XorMacro extends MacroValue {
	{ precedence = 4 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		// inverting both sides in an xor operation returns the same result
		lef instanceof NoValue ^ ref instanceof NoValue ? yes : no
	}
}

@CompileStatic
class BitandMacro extends MacroValue {
	{ precedence = 5 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		if (lef instanceof NumberValue && ref instanceof NumberValue)
			NumberValue.get(((NumberValue) lef).inner & ((NumberValue) ref).inner)
		else throw new ArgumentException('bitand only works for numbers')
	}
}

@CompileStatic
class EqMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef == ref ? yes : no
	}
}

@CompileStatic
class NeqMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef == ref ? no : yes
	}
}

@CompileStatic
class SameMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef.is(ref) ? yes : no
	}
}

@CompileStatic
class SamentMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef.is(ref) ? no : yes
	}
}

@CompileStatic
class CmpMacro extends MacroValue {
	{ precedence = 6 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		NumberValue.get(lef <=> ref)
	}
}

@CompileStatic
class GreaterMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef > ref ? yes : no
	}
}

@CompileStatic
class GreaterEqMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef >= ref ? yes : no
	}
}

@CompileStatic
class LessMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef < ref ? yes : no
	}
}

@CompileStatic
class LessEqMacro extends MacroValue {
	{ precedence = 7 }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		lef <= ref ? yes : no
	}
}

/*@CompileStatic
class ForMacro extends MacroValue {
	{ start = true }
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		List<Token> iterTokens = []
		//List<Token>
	}
}*/

@CompileStatic
class OverMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		IteratorValue iter = scope.eval(right).iterator()
		Map<String, String> propNames = [:]
		String itselfName = (String) null
		Iterator<Token> x = left.content.iterator()
		while (x.hasNext()) {
			Token a = ++x
			if (x.hasNext()) {
				String prop
				if (a instanceof ReferenceToken) prop = a.text
				else if (a instanceof StringToken) prop = ((StringToken) a).value
				else if (a instanceof ParensToken) prop = scope.eval(new Statement(((ParensToken) a).tokens))
				else throw new ArgumentException('Property name token of in macro was not a reference, string or parens')
				Token n = ++x
				String name
				if (n instanceof ReferenceToken) name = n.text
				else if (n instanceof StringToken) name = ((StringToken) n).value
				else if (n instanceof ParensToken) name = scope.eval(new Statement(((ParensToken) n).tokens))
				else throw new ArgumentException("Name token of in macro for property $prop was not a reference, string or parens")
				propNames.put(prop, name)
			} else if (a instanceof ReferenceToken) itselfName = a.text
			else if (a instanceof StringToken) itselfName = ((StringToken) a).value
			else if (a instanceof ParensToken) itselfName = scope.eval(new Statement(((ParensToken) a).tokens))
			else throw new ArgumentException('Iteration name token of in macro was not a reference, string or parens')
		}
		Value l = no
		while (iter.hasNext()) {
			Value w = ++iter
			Scope s = scope.child()
			for (e in propNames) s.set(e.value, w.propertyGet(e.key))
			if (null != itselfName) s.set(itselfName, w)
			l = s.eval(block)
		}
		l
	}
}

@CompileStatic
class IsMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Checker.parse(scope, right.content).check(lef) ? yes : no
	}
}

@CompileStatic
class IsntMacro extends MacroValue {
	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Checker.parse(scope, right.content).check(lef) ? no : yes
	}
}

@CompileStatic
class LshMacro extends MacroValue {
	{ precedence = 8 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
		println lef
		println ref
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
		Value ref = scope.eval(right)
		if (ref instanceof NumberValue)
			NumberValue.get((Number) ((NumberValue) ref).inner.invokeMethod('negative', null))
		else throw new ArgumentException('neg only works for numbers')
	}
}

@CompileStatic
class PowMacro extends MacroValue {
	{ precedence = 11 }

	@Override
	Value call(Scope scope, Statement left, Statement right, Block block) {
		Value lef = scope.eval(left)
		Value ref = scope.eval(right)
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
				scope.remove(((StringToken) t).value)
			else if (t instanceof ReferenceToken)
				scope.remove(t.text)
			else if (t instanceof ParensToken) {
				Value x = scope.eval(new Statement(left.parent, ((ParensToken) t).tokens))
				if (x instanceof StringValue) scope.remove(((StringValue) x).inner)
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

@CompileStatic
class IteratorFunction extends FunctionValue {
	Value call(Value... args) {
		if (args.length == 1) args[0].iterator()
		else throw new ArgumentException('Argument length for iterator not 1')
	}
}

@CompileStatic
class NextFunction extends FunctionValue {
	Value call(Value... args) {
		if (args.length == 1)
			if (args[0] instanceof IteratorValue) ++((IteratorValue) args[0])
			else throw new ArgumentException('First argument of next not iterator')
		else throw new ArgumentException('Argument length for next not 1')
	}
}

@CompileStatic
class HasNextFunction extends FunctionValue {
	Value call(Value... args) {
		if (args.length == 1)
			if (args[0] instanceof IteratorValue) ((IteratorValue) args[0]).hasNext() ? yes : no
			else throw new ArgumentException('First argument of hasNext not iterator')
		else throw new ArgumentException('Argument length for hasNext not 1')
	}
}

@CompileStatic
class RemoveIterFunction extends FunctionValue {
	Value call(Value... args) {
		if (args.length == 1)
			if (args[0] instanceof IteratorValue) { ((IteratorValue) args[0]).remove(); no }
			else throw new ArgumentException('First argument of removeIter not iterator')
		else throw new ArgumentException('Argument length for removeIter not 1')
	}
}