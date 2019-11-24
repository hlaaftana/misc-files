package lang

import groovy.transform.CompileStatic
import lang.interpreter.ArgumentException
import lang.interpreter.ListValue
import lang.interpreter.NoValue
import lang.interpreter.Value

@CompileStatic
class Line {
	String content
	List<Line> block = []
}

@CompileStatic
class Statement implements Value {
	Block parent
	List<Token> content
	Block block

	Statement(Block p = null, List<Token> c) {
		parent = p
		content = c
	}

	Statement(Block p = null, List<Token> c, Block b) {
		this(p, c)
		block = b
	}

	Value propertyGet(String name) {
		if (name == 'parent') null == parent ? NoValue.INSTANCE : parent
		else if (name == 'block') null == block ? NoValue.INSTANCE : block
		else if (name == 'content') new ListValue(content as List<Value>)
		else throw new ArgumentException('Unknown statement property ' + name)
	}

	Value propertySet(String name, Value value) {
		if (checkSetProperty(name, value, 'parent', Block)) parent = (Block) value
		else if (checkSetProperty(name, value, 'block', Block)) block = (Block) value
		else if (checkSetProperty(name, value, 'content', ListValue)) {
			List<Value> x = ((ListValue) value).list
			List<Token> list = new ArrayList<>(x.size())
			Value last
			int i = 0
			for (token in list) {
				if ((last = (Value) token) instanceof Token) list.add((Token) last)
				else throw new ArgumentException('Tried to set content of statement but did not get a token at index ' + i)
				++i
			}
			new ListValue(content = list)
		} else throw new ArgumentException('Unknown statement property to set ' + name)
	}
}