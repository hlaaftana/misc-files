package hlaaftana.karmafields.lang

import groovy.transform.CompileStatic
import ArgumentException
import ListValue
import NoValue
import hlaaftana.karmafields.lang.interpreter.Value

@CompileStatic
class Block implements Value {
	Block parent
	List<Statement> code = []

	Value propertyGet(String name) {
		if (name == 'parent') parent == null ? NoValue.INSTANCE : parent
		else if (name == 'code') new ListValue(code)
		else throw new ArgumentException('Unknown block property ' + name)
	}

	Value propertySet(String name, Value value) {
		if (checkSetProperty(name, value, 'parent', Block)) parent = (Block) value
		else if (checkSetProperty(name, value, 'code', ListValue)) {
			List<Value> x = ((ListValue) value).list
			List<Statement> list = new ArrayList<>(x.size())
			for (s in x)
				if (s instanceof Statement) list.add((Statement) s)
				else throw new ArgumentException('Tried to set code of block but did not get a statement')
			new ListValue(code = list)
		} else throw new ArgumentException('Unknown block property to set ' + name)
	}
}
