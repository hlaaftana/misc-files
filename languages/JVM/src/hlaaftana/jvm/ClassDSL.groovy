package hlaaftana.jvm

import groovy.transform.CompileStatic

@CompileStatic
class ClassDSL extends BaseDSL {
	static DEFAULT_STACK_SIZE = 512

	ConstantPool constantPool
	ClassConstant thisClass
	ClassConstant superClass
	List<ClassConstant> interfaces = []

	ClassConstant become(ClassConstant c) { thisClass = c }
	ClassConstant extend(ClassConstant c) { superClass = c }
	List<ClassConstant> implement(ClassConstant...c) { interfaces += c.toList() }



	ClassDSL getClassDSL() { this }
	void setClassDSL(c) { throw new IllegalArgumentException('Cannot change class DSL of class DSL') }
}
