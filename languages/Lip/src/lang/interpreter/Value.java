package lang.interpreter;

import groovy.transform.CompileStatic;

import java.util.Objects;

@SuppressWarnings("SameReturnValue")
@CompileStatic
public interface Value {
	default Value subscriptGet(Value x) { unsupport("subscriptGet"); return null; }

	default Value subscriptSet(Value x, Value y) { unsupport("subscriptSet"); return null; }

	default Value propertyGet(String x) { unsupport("propertyGet"); return null; }

	default Value propertySet(String x, Value y) { unsupport("propertySet"); return null; }

	default NumberValue size() { unsupport("size"); return null; }

	default int compareTo(Value x) { unsupport("compareTo"); return 0; }

	default Value call(Value... args) { unsupport("call"); return null; }

	default boolean equals(Value x) { return Objects.equals(this, x); }

	default int valueHash() { return hashCode(); }

	default IteratorValue iterator() { unsupport("iterator"); return null; }

	default void unsupport(String name) {
		throw new UnsupportedOperationException(name.concat(" for class ").concat(getClass().toString()));
	}

	default boolean checkSetProperty(String name, Value value, String check, Class... types) throws ArgumentException {
		if (name.equals(check)) {
			for (Class t : types) if (t.isInstance(value)) return true;
			throw new ArgumentException("Tried to set property " + name + " but wasn't the intended valuetype");
		} else return false;
	}
}
