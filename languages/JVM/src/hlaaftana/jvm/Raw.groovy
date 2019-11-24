package hlaaftana.jvm

import groovy.transform.CompileStatic

@CompileStatic
class RawAttribute extends Attribute {
	byte[] data

	RawAttribute(UTF8Constant n, byte[] d) {
		super(n)
		data = d
	}

	byte[] toByteArray() { data }
}

@CompileStatic
class RawInstruction extends Instruction {
	byte[] data

	RawInstruction(Bytecode b, char i, int n, byte[] d) {
		super(b, i)
		numInstructions = n
		data = d
	}

	byte[] toByteArray() { data }
}

@CompileStatic
class RawConstant extends Constant {
	byte[] data

	RawConstant(ConstantPool p, char i, int n, byte[] d) {
		super(p, i)
		numConstants = n
		data = d
	}

	byte[] toByteArray() { data }
}