package hlaaftana.jvm

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import java.nio.ByteBuffer

abstract class Attribute {
	UTF8Constant name

	Attribute(UTF8Constant n) { name = n }

	abstract byte[] toByteArray()
}

@InheritConstructors
@CompileStatic
class SyntheticAttribute extends Attribute {
	byte[] toByteArray() {
		[name.index >> 8, name.index, 0, 0, 0, 0] as byte[]
	}
}

@CompileStatic
class SignatureAttribute extends Attribute {
	UTF8Constant signature

	SignatureAttribute(UTF8Constant n, UTF8Constant s) { super(n); signature = s }

	byte[] toByteArray() {
		[name.index >> 8, name.index, 0, 0, 0, 2, signature.index >> 8, signature.index] as byte[]
	}
}

@CompileStatic
class CodeAttribute extends Attribute {
	CodeDSL dsl

	CodeAttribute(UTF8Constant n, CodeDSL d) {
		super(n)
		dsl = d
	}

	byte[] toByteArray() { dsl.toByteArray() }
}

@CompileStatic
class ConstantValueAttribute extends Attribute {
	Constant constant

	ConstantValueAttribute(UTF8Constant n, Constant c) { super(n); constant = c }

	byte[] toByteArray() {
		[name.index >> 8, name.index, 0, 0, 0, 0, constant.index >> 8, constant.index] as byte[]
	}
}