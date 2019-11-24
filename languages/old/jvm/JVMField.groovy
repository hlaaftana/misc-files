package hlaaftana.soda.jvm

class JVMField {
	JVMClass clazz
	int accessFlags
	JVMUTF8Constant name
	JVMUTF8Constant descriptor
	List<JVMAttribute> attributes = []

	private static final Map<String, Class> constantClasses = [L                   : JVMLongConstant, D: JVMDoubleConstant,
	                                                           'Ljava/lang/String;': JVMStringConstant, I: JVMIntConstant, B: JVMIntConstant,
	                                                           S                   : JVMIntConstant, C: JVMIntConstant, Z: JVMIntConstant]

	JVMConstantValueAttribute setConstantValue(value) {
		String type = descriptor.utf8.string
		JVMConstant constant
		if (!constantClasses.containsKey(type)) throw new IllegalArgumentException('No constants for type ' + type)
		else if (constantClasses[type].isAssignableFrom(value.class)) constant = value
		else if (type == 'L') constant = clazz.addConstant(JVMLongConstant, (long) value)
		else if (type == 'D') constant = clazz.addConstant(JVMDoubleConstant, (double) value)
		else if (type == 'Ljava/lang/String;')
			constant = clazz.addConstant(JVMStringConstant, value instanceof JVMUTF8Constant ? value :
				clazz.addConstant(JVMUTF8Constant, value))
		else if (type.length() == 1 && 'SZCIB'.contains(type))
			constant = clazz.addConstant(JVMIntConstant, (int) value)
		else throw IllegalStateException('Something fucked up with constant value ' + value)
		JVMConstantValueAttribute attr = new JVMConstantValueAttribute(clazz.utf('ConstantValue'), constant)
		attributes << attr
		attr
	}
}
