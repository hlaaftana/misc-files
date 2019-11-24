grammar SodaCode;

package : ;

Identifier
	:	JavaLetter JavaLetterOrDigit*
	;

fragment
JavaLetter
	:	[a-zA-Z$_] // these are the "java letters" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
JavaLetterOrDigit
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
;

FLOAT    : INTEGER '.' DECIMAL+ (('e' | 'E') INTEGER)?;
INTEGER     : [+\-]? (DECINT | HEXINT | OCTINT | BININT);

DECINT      : ('0d'|'0D')? DECIMAL+;
HEXINT      : ('0x'|'0X') HEXADECIMAL+;
OCTINT      : ('0o'|'0O') OCTAL+;
BININT      : ('0b'|'0B') [01]+;

DECIMAL     : ('0'..'9');
OCTAL       : ('0'..'7');
HEXADECIMAL : ('0'..'F');