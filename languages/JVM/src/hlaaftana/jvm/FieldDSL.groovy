package hlaaftana.jvm

import groovy.transform.CompileStatic

@CompileStatic
class FieldDSL extends BaseDSL {
	char accessFlags = 0
	UTF8Constant name
	UTF8Constant descriptor

	FieldConstant getConstant() {
		classDSL.constantPool.with { it << new FieldConstant(it, nextIndex,
			classDSL.thisClass, nameAndType(name, descriptor)) }
	}

	UTF8Constant name(thing) {
		name = (UTF8Constant) invokeMethod('utf8', thing)
	}

	UTF8Constant type(Map options = [array: false, primitive: false], thing) {
		if (thing instanceof String)
			descriptor = options.primitive ? utf8((String) thing) :
					(options.array ? arrayUtf(thing) : classUtf(thing))
		else descriptor = (UTF8Constant) invokeMethod('utf8', thing)
	}

	@Override
	byte[] toByteArray() {
		return new byte[0]
	}
}
