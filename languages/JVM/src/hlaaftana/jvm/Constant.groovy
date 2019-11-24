package hlaaftana.jvm

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

abstract class Constant {
	ConstantPool pool
	char index
	int numConstants = 1

	Constant(ConstantPool p, char i) { pool = p; index = i }

	Constant next() { pool.constants[index + 1] }
	Constant prev() { pool.constants[index - 1] }

	abstract byte[] toByteArray()
}

@CompileStatic
class UTF8Constant extends Constant {
	byte[] data

	UTF8Constant(ConstantPool p, char i, byte[] b) { super(p, i); data = b }

	UTF8Constant(ConstantPool p, char i, String s) { this(p, i, toUTF(s)) }

	String getString() { fromUTF(data) }

	byte[] toByteArray() {
		([1, data.length >>> 8, data.length] + data.toList()) as byte[]
	}

	static ByteArrayOutputStream baos = new ByteArrayOutputStream()
	static DataOutputStream dos = new DataOutputStream(baos)

	static byte[] toUTF(String raw) {
		dos.writeUTF(raw)
		byte[] a = baos.toByteArray()
		dos.flush()
		a
	}

	static String fromUTF(byte[] utf) {
		new DataInputStream(new ByteArrayInputStream(utf)).readUTF()
	}
}

@CompileStatic
class ClassConstant extends Constant {
	UTF8Constant name
	boolean primitive
	boolean array

	ClassConstant(ConstantPool p, char i, UTF8Constant n, boolean p1 = false, boolean a = false) {
		super(p, i); name = n; primitive = p1; array = a
	}

	byte[] toByteArray() {
		[7, name.index >> 8, name.index] as byte[]
	}
}

@CompileStatic
class NameAndTypeConstant extends Constant {
	UTF8Constant name
	UTF8Constant descriptor

	NameAndTypeConstant(ConstantPool p, char i, UTF8Constant n, UTF8Constant d){ super(p, i); name = n; descriptor = d }

	byte[] toByteArray() {
		[12, name.index >> 8, name.index, descriptor.index >> 8, descriptor.index] as byte[]
	}
}

@CompileStatic
class ClassCompConstant extends Constant {
	protected static byte tag
	ClassConstant source
	NameAndTypeConstant nameAndType

	ClassCompConstant(ConstantPool p, char i, ClassConstant s, NameAndTypeConstant n) {
		super(p, i); source = s; nameAndType = n
	}

	byte[] toByteArray() {
		[tag, source.index >> 8, source.index, nameAndType.index >> 8, nameAndType.index] as byte[]
	}
}

@InheritConstructors class FieldConstant extends ClassCompConstant { static { tag = 9 } }
@InheritConstructors class MethodConstant extends ClassCompConstant { static { tag = 10 } }
@InheritConstructors class InterfaceMethodConstant extends ClassCompConstant { static { tag = 11 } }

@CompileStatic
class StringConstant extends Constant {
	UTF8Constant data

	StringConstant(ConstantPool p, char i, UTF8Constant d) { super(p, i); data = d }

	String getString() { data.string }

	byte[] toByteArray() {
		[8, data.index >> 8, data.index] as byte[]
	}
}

@CompileStatic
class IntegerConstant extends Constant {
	int data

	IntegerConstant(ConstantPool p, char i, int d) { super(p, i); data = d }

	byte[] toByteArray() {
		[3, data >>> 24, data >>> 16, data >>> 8, data] as byte[]
	}
}

@CompileStatic
class FloatConstant extends Constant {
	float data

	FloatConstant(ConstantPool p, char i, float d) { super(p, i); data = d }

	byte[] toByteArray() {
		int y = Float.floatToRawIntBits(data)
		[4, y >>> 24, y >>> 16, y >>> 8, y] as byte[]
	}
}

@CompileStatic
class LongConstant extends Constant {
	int data
	boolean first

	LongConstant(ConstantPool p, char i, int d, boolean f) { super(p, i); data = d; first = f }

	long getLong() {
		if (first) ((long) next().data << 32) | data
		else ((long) data << 32) | prev().data
	}

	byte[] toByteArray() {
		[5, data >>> 24, data >>> 16, data >>> 8, data] as byte[]
	}
}

@CompileStatic
class DoubleConstant extends Constant {
	int data
	boolean first

	DoubleConstant(ConstantPool p, char i, int d, boolean f) { super(p, i); data = d; first = f }

	double getDouble() {
		long x
		if (first) x = ((long) next().data << 32) | data
		else x = ((long) data << 32) | prev().data
		Double.longBitsToDouble(x)
	}

	byte[] toByteArray() {
		[6, data >>> 24, data >>> 16, data >>> 8, data] as byte[]
	}
}

@CompileStatic
class MethodHandleConstant extends Constant {
	ClassCompConstant reference
	Behaviour behaviour

	MethodHandleConstant(ConstantPool p, char i, ClassCompConstant r, Behaviour b) {
		super(p, i); reference = r; behaviour = b
	}

	byte[] toByteArray() {
		[15, behaviour.ordinal() + 1, reference.index >> 8, reference.index] as byte[]
	}

	enum Behaviour {
		GET_FIELD,
		GET_STATIC,
		PUT_FIELD,
		PUT_STATIC,
		INVOKE_VIRTUAL,
		INVOKE_STATIC,
		INVOKE_SPECIAL,
		NEW_INVOKE_SPECIAL,
		INVOKE_INTERFACE
	}
}

@CompileStatic
class MethodTypeConstant extends Constant {
	UTF8Constant descriptor

	MethodTypeConstant(ConstantPool p, char i, UTF8Constant d) { super(p, i); descriptor = d }

	byte[] toByteArray() {
		[16, descriptor.index >> 8, descriptor.index] as byte[]
	}
}

@CompileStatic
class InvokeDynamicConstant extends Constant {
	char bootstrapMethod
	NameAndTypeConstant nameAndType

	InvokeDynamicConstant(ConstantPool p, char i, char b, NameAndTypeConstant n) {
		super(p, i); bootstrapMethod = b; nameAndType = n
	}

	byte[] toByteArray() {
		[18, bootstrapMethod >> 8, bootstrapMethod, nameAndType.index >> 8, nameAndType.index] as byte[]
	}
}