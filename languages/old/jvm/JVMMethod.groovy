package hlaaftana.soda.jvm

class JVMMethod {
	JVMClass clazz
	int accessFlags
	JVMUTF8Constant name
	JVMUTF8Constant descriptor
	List<JVMAttribute> attributes = []
}
