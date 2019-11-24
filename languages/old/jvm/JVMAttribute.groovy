package hlaaftana.soda.jvm

import groovy.transform.InheritConstructors

abstract class JVMAttribute {
	JVMUTF8Constant name

	JVMAttribute(JVMUTF8Constant name) { this.name = name }

	abstract byte[] toBytes()
}

class JVMConstantValueAttribute extends JVMAttribute {
	JVMConstant constant

	JVMConstantValueAttribute(JVMUTF8Constant name, JVMConstant constant) {
		super(name)
		this.constant = constant
	}

	byte[] toBytes() { [name.index >>> 8, name.index, 0, 0, 0, 2, constant.index >>> 8, constant.index] as byte[] }
}

@InheritConstructors class JVMCodeAttribute extends JVMAttribute {
	JVMBytecode bytecode
	List<JVMAttribute> attributes = []

	@Override
	byte[] toBytes() {
		return new byte[0]
	}
}