package hlaaftana.jvm

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings('GroovyUnusedDeclaration')
abstract class BaseDSL {
	static final char PUBLIC       = 0x0001
	static final char PRIVATE      = 0x0002
	static final char PROTECTED    = 0x0004
	static final char STATIC       = 0x0008
	static final char FINAL        = 0x0010
	static final char SUPER        = 0x0020
	static final char SYNCHRONIZED = SUPER
	static final char VOLATILE     = 0x0040
	static final char BRIDGE       = VOLATILE
	static final char TRANSIENT    = 0x0080
	static final char VARARGS      = TRANSIENT
	static final char NATIVE       = 0x0100
	static final char INTERFACE    = 0x0200
	static final char ABSTRACT     = 0x0400
	static final char STRICTFP     = 0x0800
	static final char SYNTHETIC    = 0x1000
	static final char ANNOTATION   = 0x2000
	static final char ENUM         = 0x4000

	char accessFlags = 0
	ClassDSL classDSL
	Map resolvedConstants = [:]
	List<Attribute> attributes = []

	void setProperty(String property, newValue) {
		resolvedConstants[property] = resolveConstant(newValue)
	}

	def getProperty(String property) {
		if (resolvedConstants.containsKey(property)) resolvedConstants[property]
		else throw new MissingPropertyException(property, this.class)
	}

	abstract byte[] toByteArray()
	char access(char x) { accessFlags = x }

	Constant resolveConstant(object) {
		if (object instanceof Constant) object
		else if (object instanceof String) utf8(object)
		else if (object instanceof Integer) integer(object)
		else if (object instanceof Float) 'float'(object)
		else if (object instanceof Long) 'long'(object)
		else if (object instanceof Double) 'double'(((Double) object).doubleValue())
		else throw new IllegalArgumentException('Invalid constant: ' + object)
	}

	ClassConstant 'class'(UTF8Constant u) { classDSL.constantPool.with { it << new ClassConstant(it, nextIndex, u) } }
	ClassConstant 'class'(String s) { 'class'(classUtf(s)) }
	UTF8Constant classUtf(String s) { utf8(s.endsWith(';') && s.startsWith('L') ? s : "L$s;") }
	UTF8Constant classUtf(UTF8Constant s) { classUtf(s.string) }
	UTF8Constant classUtf(ClassConstant s) { classUtf(s.name.string) }
	ClassConstant primitive(UTF8Constant u) { classDSL.constantPool.with { it << new ClassConstant(it, nextIndex, u, true) } }
	ClassConstant primitive(String s) { primitive(utf8(s)) }
	ClassConstant array(ClassConstant c) { array('[' + c.name.string) }
	ClassConstant array(UTF8Constant u) { classDSL.constantPool.with { it << new ClassConstant(it, nextIndex, u, false, true) } }
	ClassConstant array(String s) { array(arrayUtf(s)) }
	UTF8Constant arrayUtf(String s) { utf8(s.startsWith('[') ? s : '[' + s) }
	UTF8Constant arrayUtf(UTF8Constant s) { arrayUtf(s.string) }
	UTF8Constant arrayUtf(ClassConstant s) { arrayUtf(s.name.string) }

	NameAndTypeConstant nameAndType(String n, String d) { nameAndType(utf8(n), utf8(d)) }
	NameAndTypeConstant nameAndType(String n, UTF8Constant d) { nameAndType(utf8(n), d) }
	NameAndTypeConstant nameAndType(String n, ClassConstant d) { nameAndType(utf8(n), d) }
	NameAndTypeConstant nameAndType(String n, List<ClassConstant> a, ClassConstant d) { nameAndType(utf8(n), a, d) }
	NameAndTypeConstant nameAndType(UTF8Constant n, String d) { nameAndType(n, utf8(d)) }
	NameAndTypeConstant nameAndType(UTF8Constant n, ClassConstant d) { nameAndType(n, d.name) }
	NameAndTypeConstant nameAndType(UTF8Constant n, List<ClassConstant> a, ClassConstant d) {
		nameAndType(n, utf8("(${a*.name*.string.join('')})$d.name.string"))
	}
	NameAndTypeConstant nameAndType(UTF8Constant n, UTF8Constant d) {
		classDSL.constantPool.with { it << new NameAndTypeConstant(it, nextIndex, n, d) }
	}
	FieldConstant field(String s, NameAndTypeConstant nt) { field('class'(s), nt) }
	FieldConstant field(UTF8Constant u, NameAndTypeConstant nt) { field('class'(u), nt) }
	FieldConstant field(s, n, t) {
		if (s instanceof UTF8Constant || s instanceof String || s instanceof ClassConstant &&
			n instanceof String || n instanceof UTF8Constant &&
			t instanceof String || t instanceof UTF8Constant || t instanceof ClassConstant)
			(FieldConstant) invokeMethod('field', [s, invokeMethod('nameAndType', [n, t])])
		else throw new IllegalArgumentException('Wrong types for field')
	}
	FieldConstant field(ClassConstant c, NameAndTypeConstant nt) {
		classDSL.constantPool.with { it << new FieldConstant(it, nextIndex, c, nt) }
	}
	MethodConstant method(String s, NameAndTypeConstant nt) { method('class'(s), nt) }
	MethodConstant method(UTF8Constant u, NameAndTypeConstant nt) { method('class'(u), nt) }
	MethodConstant method(s, n, a = [], t) {
		if (s instanceof UTF8Constant || s instanceof String || s instanceof ClassConstant &&
				n instanceof String || n instanceof UTF8Constant &&
				a instanceof String || a instanceof UTF8Constant || a instanceof List || a instanceof ClassConstant &&
				t instanceof String || t instanceof UTF8Constant || t instanceof ClassConstant)
			(MethodConstant) invokeMethod('method', [s,
					invokeMethod('nameAndType', [n,
							a instanceof List ? a.collect(this.&class) : (ClassConstant) invokeMethod('class', a)]), t])
		else throw new IllegalArgumentException('Wrong types for method')
	}
	MethodConstant method(ClassConstant c, NameAndTypeConstant nt) {
		classDSL.constantPool.with { it << new MethodConstant(it, nextIndex, c, nt) }
	}

	StringConstant string(String s) { string(utf8(s)) }
	StringConstant string(byte[] b) { string(utf8(b)) }
	StringConstant string(UTF8Constant u8) { classDSL.constantPool.with { it << new StringConstant(it, nextIndex, u8) } }
	UTF8Constant utf8(String s) { classDSL.constantPool.with { it << new UTF8Constant(it, nextIndex, s) } }
	UTF8Constant utf8(byte[] b) { classDSL.constantPool.with { it << new UTF8Constant(it, nextIndex, b) } }
	UTF8Constant utf8(UTF8Constant c) { c }
	UTF8Constant utf8(ClassConstant c) { c.name }
	UTF8Constant utf8(StringConstant c) { c.data }
	IntegerConstant integer(int i) { classDSL.constantPool.with { it << new IntegerConstant(it, nextIndex, i) } }
	FloatConstant 'float'(float i) { classDSL.constantPool.with { it << new FloatConstant(it, nextIndex, i) } }
	LongConstant 'long'(long i) { 'long'((int) (i >> 32), (int) i) }
	LongConstant 'long'(int i1, int i2) {
		classDSL.constantPool.with {
			def f = it << new LongConstant(it, nextIndex, i1, true)
			it << new LongConstant(it, nextIndex, i2, false)
			f
		}
	}
	DoubleConstant 'double'(double i) {
		long l = Double.doubleToRawLongBits(i)
		'double'((int) (l >> 32), (int) l)
	}
	DoubleConstant 'double'(long i) { 'double'((int) (i >> 32), (int) i) }
	DoubleConstant 'double'(int i1, int i2) {
		classDSL.constantPool.with {
			def f = it << new DoubleConstant(it, nextIndex, i1, true)
			it << new DoubleConstant(it, nextIndex, i2, false)
			f
		}
	}
}
