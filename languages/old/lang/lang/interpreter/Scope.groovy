package hlaaftana.karmafields.lang.interpreter

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import Block
import Statement
import ClassClass

@CompileStatic
class Scope implements Value {
	String label
	Scope parent
	Map<String, MacroValue> macros = [:]
	Map<String, Value> variables = [:]

	Scope(Scope p = null) {
		parent = p
	}

	Value propertyGet(String name) {
		if (name == 'label') new StringValue(label)
		else if (name == 'parent') null == parent ? NoValue.INSTANCE : parent
		else if (name == 'macros') {
			Map<StringValue, MacroValue> map = [:]
			for (e in macros) map.put(new StringValue(e.key), e.value)
			new MapValue(map)
		} else if (name == 'variables') {
			Map<StringValue, Value> map = [:]
			for (e in variables) map.put(new StringValue(e.key), e.value)
			new MapValue(map)
		} else throw new ArgumentException('Unknown scope variable ' + name)
	}

	Value propertySet(String name, Value value) {
		if (checkSetProperty(name, value, 'label', StringValue)) { label = ((StringValue) value).inner; value }
		else if (checkSetProperty(name, value, 'parent', Scope)) parent = (Scope) value
		else if (checkSetProperty(name, value, 'macros', MapValue)) {
			Map<String, MacroValue> map = [:]
			Value k
			Value v
			for (e in ((MapValue) value).map)
				if ((k = e.key) instanceof StringValue)
					if ((v = e.value) instanceof MacroValue) map.put(((StringValue) k).inner, (MacroValue) v)
					else throw new ArgumentException('Value in map to set as macro to a scope not macro')
				else throw new ArgumentException('Key in map to set as macro to a scope not string')
			macros = map
			value
		} else if (checkSetProperty(name, value, 'variables', MapValue)) {
			Map<String, Value> map = [:]
			Value k
			for (e in ((MapValue) value).map)
				if ((k = e.key) instanceof StringValue) map.put(((StringValue) k).inner, e.value)
				else throw new ArgumentException('Key in map to set as variable to a scope not string')
			variables = map
			value
		} else throw new ArgumentException('Wowowowowowowow you tried to set something in a scope but what?????? ' + name)
	}

	Value set(String name, Value value) {
		variables.put(name, value)
	}

	Value findSet(Scope orig = this, String name, Value value) {
		if (variables.containsKey(name)) variables.put(name, value)
		else if (parent) parent.findSet(orig, name, value)
		else set(name, value)
	}

	Value get(String name) {
		Value v = variables.get(name) ?: (null != parent ? parent.get(name) : null)
		if (null != v) v
		else throw new ArgumentException('Undefined variable ' + name)
	}

	boolean any(String name) {
		variables.get(name)
	}

	MacroValue macro(String name) {
		macros.get(name) ?: (null != parent ? parent.macro(name) : null)
	}

	MacroValue setMacro(String name, MacroValue macro) {
		macros.put(name, macro)
	}

	Value remove(String name) {
		variables.remove(name)
	}

	Scope copy(Scope p = parent) {
		new Scope(variables: new HashMap<String, Value>(variables), macros: new HashMap<String, MacroValue>(macros),
			parent: p)
	}
}

@CompileStatic
class PureValue implements Value {
	FunctionValue equals
	FunctionValue hash
	FunctionValue size
	FunctionValue iterator
	FunctionValue subscriptGet
	FunctionValue propertyGet
	FunctionValue subscriptSet
	FunctionValue propertySet
	FunctionValue call

	boolean equals(x) {
		x instanceof Value && (null == equals ? super.equals(x) : !(equals.call(this, x) instanceof NoValue))
	}

	int hashCode() {
		if (null == hash) super.hashCode()
		else {
			Value a = hash.call(this)
			if (!(a instanceof IntegerValue))
				throw new ArgumentException('Hash function has to return integer')
			((IntegerValue) a).inner.hashCode()
		}
	}

	NumberValue size() {
		if (null == size) { unsupport('size'); null }
		else {
			Value a = size.call(this)
			if (!(a instanceof NumberValue))
				throw new ArgumentException('Size function has to return number')
			(NumberValue) a
		}
	}

	Value subscriptGet(Value v) {
		if (null == subscriptGet) { unsupport('subscriptGet'); null }
		else subscriptGet.call(this, v)
	}

	Value subscriptSet(Value v, Value w) {
		if (null == subscriptSet) { unsupport('subscriptSet'); null }
		else subscriptSet.call(this, v, w)
	}

	Value propertyGet(String v) {
		if (null == propertyGet) { unsupport('propertyGet'); null }
		else propertyGet.call(this, new StringValue(v))
	}

	Value propertySet(String x, Value v) {
		if (null == propertySet) { unsupport('propertySet'); null }
		else propertySet.call(this, new StringValue(x), v)
	}

	Value call(Value... args) {
		if (null == call) { unsupport('call'); null }
		else call.call(this, *args)
	}
}

@CompileStatic
class ClassValue extends ClassedValue {
	String name
	Map<String, Property> props = [:]
	ClassValue[] parents = []

	ClassedValue construct(ClassedValue it, Value... args) {
		for (p in parents)
			p.construct(it, args)
		it
	}
	
	boolean equals(ClassedValue a, x) { a.is(x) }

	int hashCode(ClassedValue a) { a.hashCode() }

	NumberValue size(ClassedValue a) {
		throw new UnsupportedOperationException('size for class ' + name)
	}

	Value subscriptGet(ClassedValue a, Value v) {
		throw new UnsupportedOperationException('subscriptGet for class ' + name)
	}

	Value subscriptSet(ClassedValue a, Value v, Value w) {
		throw new UnsupportedOperationException('subscriptSet for class ' + name)
	}

	Value propertyGet(ClassedValue a, String v) {
		getProp(v).get(a)
	}

	Value propertySet(ClassedValue a, String x, Value v) {
		getProp(x).set(a, v)
	}

	Value call(ClassedValue a, Value... args) {
		type.call(this, args)
	}

	int compareTo(ClassedValue a, Value x) {
		type.compareTo(this, x)
	}

	boolean canLabel(ClassValue t) {
		if (t == this) return true
		for (p in parents)
			if (p.canLabel(t)) return true
		false
	}

	String getIdentifying() {
		"type $name, parents ${parents*.identifying}"
	}

	Property getProp(String name) {
		Property x = props.get(name)
		if (null == x)
			for (p in parents)
				if (null != (x = p.getProp(name))) return x
		x
	}

	Property setProp(String name, Property property) { props.put(name, property) }

	static class Property {
		String name
		Value defaultValue
		boolean settable = true
		IsCheck check

		boolean checkGet(ClassedValue c) { null == check ? true : check.check(c) }

		boolean checkSet(ClassedValue c, Value a) {
			settable && (null == check ? true : check.check(c))
		}

		Value get(ClassedValue c) {
			if (!checkGet(c)) throw new ArgumentException('Check for getting property ' + name + ' failed')
			Value x = c.props.get(name)
			if (null != x) x
			else if (null != defaultValue) defaultValue
			else NoValue.INSTANCE
		}

		Value set(ClassedValue c, Value v) {
			if (!checkSet(c, v)) throw new ArgumentException('Check for setting property ' + name + ' failed')
			c.props.put(name, v)
		}
	}
}
@InheritConstructors
class ArgumentException extends Exception {}

@CompileStatic
class NoValue implements Value {
	static final NoValue INSTANCE = new NoValue()

	private NoValue() {}

	int hashCode() { 0 }
}

@CompileStatic
class YesValue implements Value {
	static final YesValue INSTANCE = new YesValue()

	private YesValue() {}

	int hashCode() { 1 }
}

@CompileStatic
class ClassedValue implements Value {
	ClassValue type = ClassClass.INSTANCE
	Map<String, Value> props = [:]

	ClassedValue() {}

	ClassedValue(ClassValue t, Value... constructorArgs) {
		type = t
		type.construct(this, constructorArgs)
	}

	@Override
	boolean equals(x) {
		type.equals(this, x)
	}

	@Override
	int hashCode() {
		type.hashCode(this)
	}

	@Override
	NumberValue size() {
		type.size(this)
	}

	@Override
	Value subscriptGet(Value v) {
		type.subscriptGet(this, v)
	}

	@Override
	Value subscriptSet(Value v, Value w) {
		type.subscriptSet(this, v, w)
	}

	@Override
	Value propertyGet(String v) {
		type.propertyGet(this, v)
	}

	@Override
	Value propertySet(String x, Value v) {
		type.propertySet(this, x, v)
	}

	@Override
	Value call(Value... args) {
		type.call(this, args)
	}

	@Override
	int compareTo(Value x) {
		type.compareTo(this, x)
	}
}

@CompileStatic
abstract class NumberValue implements Value {
	Number inner

	NumberValue(Number inner) {
		this.inner = inner
	}

	static NumberValue get(Number x) {
		if (x instanceof BigDecimal || x instanceof double || x instanceof float)
			new FloatingValue(x)
		else new IntegerValue(x)
	}

	boolean equals(Value x) { x instanceof NumberValue && inner == ((NumberValue) x).inner }

	int compareTo(Value o) { if (o instanceof NumberValue) inner <=> ((NumberValue) o).inner
			else throw new ArgumentException('Cannot compare number to valuetype ' + o.class) }

	int hashCode() { inner.hashCode() }
}

@CompileStatic
@InheritConstructors
class IntegerValue extends NumberValue {}

@CompileStatic
@InheritConstructors
class FloatingValue extends NumberValue {}

@CompileStatic
class StringValue implements Value {
	String inner

	StringValue(String inner) {
		this.inner = inner
	}

	boolean equals(Value x) { x instanceof StringValue && inner == ((StringValue) x).inner }

	int compareTo(Value o) { if (o instanceof StringValue) inner <=> ((StringValue) o).inner
			else throw new ArgumentException('Cannot compare string to valuetype ' + o.class) }

	int hashCode() { inner.hashCode() }
}

@CompileStatic
class ArrayValue implements Value {
	Value[] array

	ArrayValue(Value... values) {
		array = values
	}

	Value subscriptGet(Value x) {
		if (!(x instanceof NumberValue))
			throw new ArgumentException('Arrays can only be subscripted with numbers')
		array[((NumberValue) x).inner.intValue()]
	}

	Value subscriptSet(Value x, Value y) {
		if (!(x instanceof NumberValue))
			throw new ArgumentException('Arrays can only be subscripted with numbers')
		array[((NumberValue) x).inner.intValue()] = y
	}

	NumberValue size() {
		new IntegerValue(array.length)
	}

	Value getAt(int i) {
		array[i]
	}

	Value putAt(int i, Value v) {
		array[i] = v
	}

	boolean equals(ArrayValue val) { array.equals val.array }

	int hashCode() { array.hashCode() }
}

@CompileStatic
class ListValue implements Value {
	List<Value> list

	ListValue(List<Value> l) {
		list = l
	}

	Value subscriptGet(Value x) {
		if (!(x instanceof NumberValue))
			throw new ArgumentException('Arrays can only be subscripted with numbers')
		list[((NumberValue) x).inner.intValue()]
	}

	Value subscriptSet(Value x, Value y) {
		if (!(x instanceof NumberValue))
			throw new ArgumentException('Arrays can only be subscripted with numbers')
		list[((NumberValue) x).inner.intValue()] = y
	}

	NumberValue size() {
		new IntegerValue(list.size())
	}

	Value getAt(int i) {
		list[i]
	}

	Value putAt(int i, Value v) {
		list[i] = v
	}

	boolean equals(ListValue val) { list == val.list }

	int hashCode() { list.hashCode() }
}

@CompileStatic
class MapValue implements Value {
	Map<Value, Value> map

	MapValue(Map<Value, Value> m) {
		map = m
	}

	Value subscriptGet(Value x) { getAt(x) }

	Value subscriptSet(Value x, Value y) { putAt(x, y) }

	Value propertyGet(String x) { getAt(new StringValue(x)) }

	Value propertySet(String x, Value y) { putAt(new StringValue(x), y) }

	NumberValue size() {
		new IntegerValue(map.size())
	}

	Value getAt(Value i) {
		map[i]
	}

	Value putAt(Value i, Value v) {
		map[i] = v
	}

	boolean equals(MapValue val) { map == val.map }

	int hashCode() { map.hashCode() }
}

@CompileStatic
abstract class FunctionValue implements Value {}

@CompileStatic
abstract class MacroValue implements Value {
	boolean start = false
	int precedence = 0
	boolean alignment = false



	abstract Value call(Scope scope, Statement left, Statement right, Block block)
}

@CompileStatic
class CodeFunctionValue extends FunctionValue {
	List<CodeMethod> methods = []

	Value call(Scope scope = new Scope(), Value... args) {
		for (m in methods)
			if (m.matches(args))
				return m.apply(scope, args)
		throw new ArgumentException('Unmatched arguments')
	}
}

@CompileStatic
class CodeMethod {
	Arguments arguments
	Block code

	boolean matches(Value... args) { arguments.matches(args) }

	Value apply(Scope scope, Value... args) {
		int prelen = arguments.preVarargs.length
		for (int i = 0; i < prelen; ++i)
			scope.set(arguments.preVarargs[i].name, args[i])

		if (null != arguments.varargs) {
			int postlen = arguments.postVarargs.length
			for (int i = 0; i < postlen; ++i)
				scope.set(arguments.postVarargs[i].name, args[args.length - postlen + i])

			int vararglen = args.length - prelen - postlen
			Value[] var = new Value[vararglen]
			for (int i = 0; i < vararglen; ++i)
				var[i] = args[i + vararglen]

			scope.set(arguments.varargs.name, new ArrayValue(var))
		}

		return Interpreter.evaluate(scope, code)
	}


}

@CompileStatic
class Arguments {
	Parameter[] preVarargs = []
	Parameter varargs
	Parameter[] postVarargs = []

	Arguments(Parameter... params) {
		List<Parameter> pre = []
		List<Parameter> post = []
		for (p in params)
			if (p.varargs)
				if (varargs) throw new IllegalArgumentException('Only one varargs is allowed')
				else varargs = p
			else if (varargs) post.add(p)
			else pre.add(p)
		preVarargs = pre as Parameter[]
		postVarargs = post as Parameter[]
	}

	boolean matches(Value... values) {
		if (values.length < preVarargs.length + postVarargs.length) return false
		int i = 0
		for (int j = 0; j < preVarargs.length; ++j)
			if (!preVarargs[j].matches(values[i++])) return false
		Value[] varargSuppl = new Value[values.length - preVarargs.length - postVarargs.length]
		for (int j = 0; j < varargSuppl.length + preVarargs.length; ++j)
			varargSuppl[j] = values[i++]
		if (!varargs.matches(new ArrayValue(varargSuppl))) return false
		for (int j = 0; j < values.length; ++j)
			if (!postVarargs[j].matches(values[i++])) return false
		true
	}

	static class Parameter {
		boolean varargs
		String name
		IsCheck check

		boolean matches(Value val) {
			(varargs ? val instanceof ArrayValue : true) && (null == check ? true : check.check(val))
		}
	}
}