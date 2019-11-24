package hlaaftana.karmafields.lang

import groovy.transform.CompileStatic
import ArgumentException
import ArrayValue
import NoValue
import hlaaftana.karmafields.lang.interpreter.Value

@CompileStatic
class Line {
	int ln
	String content
	List<Line> block = []
}

@CompileStatic
class Statement implements Value {
	Block parent
	Token[] content
	Block block

	Statement(Block p = null, Token[] c) {
		parent = p
		content = c
	}

	Statement(Block p = null, Token[] c, Block b) {
		this(p, c)
		block = b
	}

	Value propertyGet(String name) {
		if (name == 'parent') null == parent ? NoValue.INSTANCE : parent
		else if (name == 'block') null == block ? NoValue.INSTANCE : block
		else if (name == 'content') new ArrayValue(content)
		else throw new ArgumentException('Unknown statement property ' + name)
	}

	Value propertySet(String name, Value value) {
		if (checkSetProperty(name, value, 'parent', Block)) parent = (Block) value
		else if (checkSetProperty(name, value, 'block', Block)) block = (Block) value
		else if (checkSetProperty(name, value, 'content', ArrayValue)) {
			Value[] x = ((ArrayValue) value).array
			Token[] array = new Token[x.size()]
			Value last
			for (int i = 0; i < array.length; ++i)
				if ((last = x[i]) instanceof Token) array[i] = (Token) last
				else throw new ArgumentException('Tried to set content of statement but did not get a token at index ' + i)
			new ArrayValue(content = array)
		} else throw new ArgumentException('Unknown statement property to set ' + name)
	}
}