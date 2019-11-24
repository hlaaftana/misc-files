package lang.interpreter

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import lang.Block
import lang.Statement
import lang.interpreter.defaultscope.ArgumentParser
import lang.interpreter.defaultscope.ClassClass
import lang.interpreter.defaultscope.IsCheck

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
		else if (name == 'parent') null == parent ? get('no') : parent
		else if (name == 'macros') {
			final map = new HashMap<Value, Value>(macros.size())
			for (e in macros) map.put(new StringValue(e.key), e.value)
			new MapValue(map)
		} else if (name == 'variables') {
			final map = new HashMap<Value, Value>(variables.size())
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
		else orig.set(name, value)
	}

	Value get(String name) {
		Value v = variables.get(name) ?: (null != parent ? parent.get(name) : null)
		if (null != v) v
		else throw new ArgumentException('Undefined variable ' + name)
	}

	boolean any(String name) {
		null != variables.get(name)
	}

	MacroValue macro(String name) {
		macros.get(name) ?: (null != parent ? parent.macro(name) : null)
	}

	MacroValue setMacro(String name, MacroValue macro) {
		macros.put(name, macro)
	}

	Value remove(String name) {
		variables.remove(name) ?: parent?.remove(name)
	}

	Scope child() {
		new Scope(this)
	}

	Value eval(Statement stmt) {
		Interpreter.evaluate(this, stmt)
	}

	Value eval(Block block) {
		Interpreter.evaluate(this, block)
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
	FunctionValue compareTo

	int compareTo(Value x) {
		if (null == compareTo) { unsupport('compareTo'); 0 }
		else {
			Value a = compareTo.call(this, x)
			if (!(a instanceof IntegerValue))
				throw new ArgumentException('Hash function has to return integer')
			((IntegerValue) a).inner.intValue()
		}
	}

	boolean equals(Value x) {
		(null == equals ? super.equals(x) : !(equals.call(this, x) instanceof NoValue))
	}

	int hashCode() {
		if (null == hash) super.hashCode()
		else {
			Value a = hash.call(this)
			if (!(a instanceof IntegerValue))
				throw new ArgumentException('Hash function has to return integer')
			((IntegerValue) a).inner.intValue()
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

	IteratorValue iterator() {
		if (null == iterator) { unsupport('iterator'); null }
		else {
			Value a = iterator.call(this)
			if (!(a instanceof IteratorValue))
				throw new ArgumentException('Iterator function has to return iterator')
			(IteratorValue) a
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
		else {
			Value[] a = new Value[args.length + 1]
			a[0] = this
			System.arraycopy(args, 0, a, 1, args.length)
			call.call(a)
		}
	}
}

@CompileStatic
class PureIterator implements IteratorValue {
	FunctionValue hasNext
	FunctionValue next
	FunctionValue remove

	@Override
	void remove() {
		if (null == remove) unsupport('remove')
		else remove.call(this)
	}

	@Override
	boolean hasNext() {
		if (null == hasNext) throw new ArgumentException('Iterator needs hasNext')
		else hasNext.call(this)
	}

	@Override
	Value next() {
		if (null == next) throw new ArgumentException('Iterator needs next')
		else next.call(this)
	}
}

@CompileStatic
class ClassValue extends ClassedValue {
	String name
	Map<String, Property> props = [:]
	ClassValue[] parents = []

	// for ClassClass
	protected ClassValue(int x) {
		super(null)
	}

	ClassValue(Value... args) {
		super(ClassClass.INSTANCE, args)
	}

	ClassedValue construct(ClassedValue it, Value... args) {
		for (p in parents) p.construct(it, args)
		it
	}

	FunctionValue classedEquals
	FunctionValue classedHash
	FunctionValue classedSize
	FunctionValue classedIterator
	FunctionValue classedSubscriptGet
	FunctionValue classedPropertyGet
	FunctionValue classedSubscriptSet
	FunctionValue classedPropertySet
	FunctionValue classedCall
	FunctionValue classedCompareTo

	boolean equals(ClassedValue c, Value x) {
		(null == classedEquals ? c == x : !(classedEquals.call(c, x) instanceof NoValue))
	}

	int compareTo(ClassedValue c, Value x) {
		if (null == classedCompareTo) { unsupport('compareTo for class ' + name); 0 }
		else {
			Value a = classedCompareTo.call(c, x)
			if (!(a instanceof IntegerValue))
				throw new ArgumentException('Hash function for class ' + name + ' has to return integer')
			((IntegerValue) a).inner.intValue()
		}
	}

	int hashCode(ClassedValue c) {
		if (null == classedHash) c.hashCode()
		else {
			Value a = classedHash.call(c)
			if (!(a instanceof IntegerValue))
				throw new ArgumentException('Hash function for class ' + name + ' has to return integer')
			((IntegerValue) a).inner.intValue()
		}
	}

	NumberValue size(ClassedValue c) {
		if (null == classedSize) { unsupport('size for class ' + name); null }
		else {
			Value a = classedSize.call(c)
			if (!(a instanceof NumberValue))
				throw new ArgumentException('Size function for class ' + name + ' has to return number')
			(NumberValue) a
		}
	}

	Value subscriptGet(ClassedValue c, Value v) {
		if (null == classedSubscriptGet) { unsupport('subscriptGet for class ' + name); null }
		else classedSubscriptGet.call(c, v)
	}

	Value subscriptSet(ClassedValue c, Value v, Value w) {
		if (null == classedSubscriptSet) { unsupport('subscriptSet for class ' + name); null }
		else classedSubscriptSet.call(c, v, w)
	}

	Value propertyGet(ClassedValue c, String v) {
		if (null == classedPropertyGet) getProp(v).get(c)
		else classedPropertyGet.call(c, new StringValue(v))
	}

	Value propertySet(ClassedValue c, String x, Value v) {
		if (null == classedPropertySet) getProp(x).set(c, v)
		else classedPropertySet.call(c, new StringValue(x), v)
	}

	Value call(ClassedValue c, Value... args) {
		if (null == classedCall) { unsupport('call for class ' + name); null }
		else {
			Value[] a = new Value[args.length + 1]
			a[0] = c
			System.arraycopy(args, 0, a, 1, args.length)
			classedCall.call(a)
		}
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

		boolean checkGet(ClassedValue c) { true }

		boolean checkSet(ClassedValue c, Value a) {
			settable && (null == check ? true : check.check(a))
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

@CompileStatic
class ArgumentException extends Exception {
	ArgumentException(String msg) { super(msg) }
}

@CompileStatic
class NoValue implements Value {
	static NoValue INSTANCE = new NoValue()

	private NoValue() {}

	int hashCode() { 0 }
}

@CompileStatic
class YesValue implements Value {
	static YesValue INSTANCE = new YesValue()

	private YesValue() {}

	int hashCode() { 1 }
}

@CompileStatic
class ClassedValue implements Value {
	ClassValue type
	Map<String, Value> props = [:]

	ClassedValue(ClassValue t, Value... constructorArgs) {
		type = t
		type.construct(this, constructorArgs)
	}

	@Override
	boolean equals(Value x) {
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
		if (x instanceof BigDecimal || x instanceof Double || x instanceof Float) new FloatingValue(x)
		else new IntegerValue(x)
	}

	boolean equals(Value x) { x instanceof NumberValue && inner == ((NumberValue) x).inner }

	int compareTo(Value o) { if (o instanceof NumberValue) inner <=> ((NumberValue) o).inner
			else throw new ArgumentException('Cannot compare number to valuetype ' + o.class) }

	int hashCode() { inner.hashCode() }
}

@CompileStatic
@InheritConstructors
class IntegerValue extends NumberValue {
	IntegerValue(Number inner) { super(inner) }
}

@CompileStatic
@InheritConstructors
class FloatingValue extends NumberValue {
	FloatingValue(Number inner) { super(inner) }
}

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

interface IteratorValue extends Value, Iterator<Value> {
	boolean hasNext()
	Value next()
}

@CompileStatic
class IteratorAdapter implements IteratorValue, Iterable<Value> {
	Iterator<Value> inner
	int index = -1

	IteratorAdapter(Iterator<Value> i) { inner = i }

	boolean hasNext() {
		inner.hasNext()
	}

	@Override
	Value next() {
		++index
		inner.next()
	}

	@Override
	Value getProperty(String name) {
		if (name == 'index') new IntegerValue(index)
		else if (name == 'iter') this
		else throw new ArgumentException('Unknown property ' + name + ' for java iterator')
	}

	@Override
	void remove() {
		inner.remove()
	}

	IteratorValue iterator() { this }
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

	boolean equals(Value val) { val instanceof ArrayValue && array.equals(((ArrayValue) val).array) }

	int hashCode() { array.hashCode() }

	IteratorValue iterator() { new IteratorAdapter(array.iterator()) }
}

@CompileStatic
class TupleValue implements Value {
	private final Value[] array

	TupleValue(Value... values) {
		array = values
	}

	Value subscriptGet(Value x) {
		if (!(x instanceof NumberValue))
			throw new ArgumentException('Tuples can only be subscripted with numbers')
		array[((NumberValue) x).inner.intValue()]
	}

	Value subscriptSet(Value x, Value y) {
		throw new ArgumentException('Tuples are immutable')
	}

	NumberValue size() {
		new IntegerValue(array.length)
	}

	Value getAt(int i) {
		array[i]
	}

	Value putAt(int i, Value v) {
		throw new ArgumentException('Tuples are immutable')
	}

	boolean equals(Value val) { val instanceof TupleValue && array.equals(((TupleValue) val).array) }

	int hashCode() { array.hashCode() }

	IteratorValue iterator() { new IteratorAdapter(array.iterator()) }
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

	boolean equals(Value val) { val instanceof ListValue && list == ((ListValue) val).list }

	int hashCode() { list.hashCode() }

	IteratorValue iterator() { new IteratorAdapter(list.iterator()) }
}

@CompileStatic
class SetValue implements Value {
	Set<Value> set

	SetValue(Set<Value> l) {
		set = l
	}

	NumberValue size() {
		new IntegerValue(set.size())
	}

	boolean equals(Value val) { val instanceof SetValue && set == ((SetValue) val).set }

	int hashCode() { set.hashCode() }

	IteratorValue iterator() { new IteratorAdapter(set.iterator()) }
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

	boolean equals(val) { val instanceof MapValue && map == val.map }

	int hashCode() { map.hashCode() }
}

@CompileStatic
class ExceptionValue implements Value {
	Exception inner

	Value propertyGet(String x) {
		if (x == 'message') new StringValue(inner.message)
		else if (x == 'name') new StringValue(inner.class.simpleName)
		else throw new ArgumentException('Unknown property ' + x + ' for exception')
	}
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
	String name
	Arguments arguments
	Block block
	Scope parentScope

	Value call(Scope scope, Value... args) {
		if (arguments.matches(args)) {
			final result = scope.eval(block)
			if (null != arguments.check && !arguments.check.check(result))
				throw new ArgumentException('Check for result failed for function ' + name)
			result
		} else throw new ArgumentException('Unmatched arguments for function ' + name)
	}

	Value call(Value... args) {
		def scope = parentScope.child()
		scope.setMacro('when', new WhenArgumentMacro(args))
		scope.set('arguments', new ArrayValue(args))
		call(scope, args)
	}

	@CompileStatic
	static class WhenArgumentMacro extends MacroValue {
		Value[] args

		WhenArgumentMacro(Value[] args) { this.args = args; start = true }

		Value call(Scope s, Statement left, Statement right, Block block) {
			final check = ArgumentParser.parse(s, right.content)
			if (check.matches(args)) return s.eval(block)
			NoValue.INSTANCE
		}
	}
}

@CompileStatic
class Arguments {
	Parameter[] preVarargs = []
	Parameter varargs
	Parameter[] postVarargs = []
	IsCheck check

	Arguments(Parameter... params) {
		List<Parameter> pre = []
		List<Parameter> post = []
		for (p in params)
			if (p.varargs)
				if (varargs) throw new IllegalArgumentException('Only one varargs is allowed')
				else varargs = p
			else (varargs ? post : pre).add(p)
		preVarargs = pre as Parameter[]
		postVarargs = post as Parameter[]
	}

	List<Parameter> getParameters() {
		def result = new Parameter[preVarargs.length + postVarargs.length + 1]
		System.arraycopy(preVarargs, 0, result, 0, preVarargs.length)
		result[preVarargs.length] = varargs
		System.arraycopy(postVarargs, 0, result, preVarargs.length + 1, postVarargs.length)
		result
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