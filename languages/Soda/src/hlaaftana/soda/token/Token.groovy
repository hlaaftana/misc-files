package hlaaftana.soda.token

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
abstract class Token {
	final String text

	Token(String text) { this.text = text }

	def <T extends Token> T match(Class<T> type) {
		try {
			type.cast(this)
		} catch (ClassCastException ignored) {
			(T) null
		}
	}
}

@CompileStatic @InheritConstructors
abstract class NameToken extends Token {}

@CompileStatic @InheritConstructors
class WordToken extends NameToken {}

@CompileStatic @InheritConstructors
class SymbolToken extends NameToken {}

@CompileStatic @InheritConstructors
abstract class ValueToken extends Token {}

@CompileStatic @InheritConstructors
class NumberToken extends ValueToken {}

@CompileStatic @InheritConstructors
class StringToken extends ValueToken {}