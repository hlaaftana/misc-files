package hlaaftana.karmafields.lang

import groovy.transform.CompileStatic
import ArgumentException
import FloatingValue
import IntegerValue
import NumberValue
import StringValue
import hlaaftana.karmafields.lang.interpreter.Value
import hlaaftana.karmafields.relics.StringEscaper

@CompileStatic
abstract class Token implements Value {
	boolean done = false
	StringBuilder builder = new StringBuilder()
	String text
	int ln
	int cl

	boolean equals(Value x) {
		x.class == this.class && text == ((Token) x).text
	}

	Value propertyGet(String name) {
		if (name == 'ln') new IntegerValue(ln)
		else if (name == 'cl') new IntegerValue(cl)
		else if (name == 'text') new StringValue(text)
		else throw new ArgumentException('Unknown token property ' + name)
	}

	Token at(int ln, int cl) {
		this.ln = ln
		this.cl = cl
		this
	}

	Token start(int ch) {
		builder.appendCodePoint(ch)
		this
	}

	Token start(ch) {
		builder.append(ch)
		this
	}

	def cp(int cp) {
		builder.appendCodePoint(cp)
	}

	Token finish() {
		text = builder.toString()
		builder = null
		done = true
		this
	}
}

@CompileStatic class WhitespaceToken extends Token {}

@CompileStatic
class StringToken extends Token {
	private static final StringValue type = new StringValue('string')

	String getValue() {
		StringEscaper.unescapeSoda(text)
	}

	Value propertyGet(String name) {
		if (name == 'type') type
		else if (name == 'value') new StringValue(value)
		else super.propertyGet(name)
	}
}

@CompileStatic
class NumberToken extends Token {
	private static final StringValue type = new StringValue('number')
	boolean dotted
	boolean exponentiated
	Class<? extends Number> specified

	NumberValue toNumberValue() {
		if (specified)
			if (dotted || exponentiated && specified == int || specified == long || specified == BigInteger)
				new IntegerValue(text.toBigDecimal().asType(specified))
			else
				new FloatingValue(text.asType(specified))
		else {
			Number x
			if (dotted || exponentiated)
				try {
					x = text.toFloat()
				} catch (NumberFormatException ignore) {
					try {
						x = text.toDouble()
					} catch (NumberFormatException ignored) {
						x = text.toBigDecimal()
					}
				}
			else
				try {
					x = text.toInteger()
				} catch (NumberFormatException ignore) {
					try {
						x = text.toLong()
					} catch (NumberFormatException ignored) {
						x = text.toBigInteger()
					}
				}
			NumberValue.get(x)
		}
	}

	Value propertyGet(String name) {
		if (name == 'type') type
		else if (name == 'value') toNumberValue()
		else super.propertyGet(name)
	}
}

@CompileStatic
class PropertyToken extends Token {
	private static final StringValue type = new StringValue('property')

	Value propertyGet(String name) {
		if (name == 'type') type
		else super.propertyGet(name)
	}
}

@CompileStatic
class ReferenceToken extends Token {
	private static final StringValue type = new StringValue('reference')

	Value propertyGet(String name) {
		if (name == 'type') type
		else super.propertyGet(name)
	}
}

@CompileStatic
abstract class TokensToken extends Token {
	List<Token> tokenBuilder = []
	Token[] tokens

	@Override
	Token finish() {
		tokens = tokenBuilder as Token[]
		tokenBuilder = null
		done = true
		this
	}

	boolean equals(Value x) {
		x.class == this.class && tokens.equals(((TokensToken) x).tokens)
	}

	def add(Token t) { tokenBuilder.add t }

	Token last() { tokenBuilder.empty ? null : tokens.last() }
}

@CompileStatic
class SubscriptToken extends TokensToken {
	private static final StringValue type = new StringValue('subscript')

	Value propertyGet(String name) {
		if (name == 'type') type
		else super.propertyGet(name)
	}
}

@CompileStatic
class ParensToken extends TokensToken {
	private static final StringValue type = new StringValue('parentheses')

	Value propertyGet(String name) {
		if (name == 'type') type
		else super.propertyGet(name)
	}
}

