package hlaaftana.jvm

import groovy.transform.CompileStatic

import java.nio.ByteBuffer

@CompileStatic
class CodeDSL extends BaseDSL {
	UTF8Constant name
	Bytecode bytecode = new Bytecode()
	List<ExceptionHandler> exceptionHandlers = []
	char maxStack
	char maxLocals

	UTF8Constant name(UTF8Constant u) { name = u }
	Bytecode bytecode(Bytecode b) { bytecode = b }
	char maxStack(char m) { maxStack = m }
	char maxLocals(char l) { maxLocals = l }
	List<ExceptionHandler> exceptionHandlers(List x) { exceptionHandlers = x }
	ExceptionHandler handler(Instruction start, Instruction handler, Instruction end,
	                         ClassConstant type = null) {
		new ExceptionHandler(start: start, handlerStart: handler, end: end, type: type).with { exceptionHandlers << it; it }
	}

	PushConstantInstruction push(x) {
		def a = new PushConstantInstruction(bytecode, bytecode.nextIndex, x)
		try {
			a.toByteArray()
		} catch(e) {
			a.constant = resolveConstant(x)
		}
		bytecode.addInstruction(a)
	}

	CreateArrayInstruction newArray(ClassConstant x) {
		bytecode.addInstruction(new CreateArrayInstruction(bytecode, bytecode.nextIndex, x))
	}

	CreatePrimitiveArrayInstruction newPrimitiveArray(Class x) {
		bytecode.addInstruction(new CreatePrimitiveArrayInstruction(bytecode, bytecode.nextIndex, x))
	}



	ArrayLengthInstruction arrayLength() {
		bytecode.addInstruction(new ArrayLengthInstruction(bytecode, bytecode.nextIndex))
	}

	byte[] toByteArray() {
		if (maxStack == (char) 0) maxStack = bytecode.maxStackSize
		if (maxLocals == (char) 0) maxLocals = bytecode.maxLocalsSize
		if (null == name) name = utf8('Code')
		def code = bytecode.instructions*.toByteArray().flatten() as byte[]
		def attrs = attributes*.toByteArray().flatten() as byte[]
		def excs = exceptionHandlers*.toByteArray().flatten() as byte[]
		ByteBuffer buf = ByteBuffer.allocate(18 + code.length + exceptionHandlers.size() * 8 + attrs.length)
		buf.putChar(name.index).putInt(buf.capacity() - 6).putChar(maxStack).putChar(maxLocals)
				.putInt(code.length).put(code).putChar((char) exceptionHandlers.size()).put(excs)
				.putChar((char) attributes.size()).put(attrs).array()
	}
}

@CompileStatic
class ExceptionHandler {
	Instruction start
	Instruction handlerStart
	Instruction end
	ClassConstant type

	byte[] toByteArray() {
		[start.index >> 8, start.index, end.index >> 8, end.index, handlerStart.index >> 8, handlerStart.index,
		 type ? type.index >> 8 : 0, type ? type.index : 0] as byte[]
	}
}