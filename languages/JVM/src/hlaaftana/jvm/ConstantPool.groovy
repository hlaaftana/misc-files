package hlaaftana.jvm

import groovy.transform.CompileStatic

@CompileStatic
class ConstantPool {
	List<Constant> constants = []

	char getNextIndex() { (char) constants.collect { it.numConstants }.sum() }

	def <T extends Constant> T leftShift(T c) { addConstant(c) }
	def <T extends Constant> T addConstant(T c) { constants << c; c }
}
