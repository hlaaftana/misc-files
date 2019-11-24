package hlaaftana.jvm

import groovy.transform.InheritConstructors

@CompileStatic
class Bytecode {
	ClassDSL dsl
	List<Instruction> instructions = []

	char getNextIndex() { (char) instructions*.numInstructions.sum() }

	public <T extends Instruction> T leftShift(T i) { addInstruction(i) }
	public <T extends Instruction> T addInstruction(T i) { instructions << i; i }
	char getMaxStackSize() { (char) instructions*.stackIncrease.sum() }
	char getMaxLocalsSize() { (char) instructions.findAll { it instanceof StoreInstruction }
			.collect { [it.type, it.var] }.unique { it[1] }
			.inject(0) { a, b -> a + (b.type == double || b.type == long ? 2 : 1) } }
}

abstract class Instruction {
	static List<Class> typeOrder = [int, long, float, double, null, byte, char, short].asImmutable()
	Bytecode bytecode
	char index
	int numInstructions = 1
	int stackIncrease = 0

	Instruction(Bytecode b, char i) { bytecode = b; index = i }

	abstract byte[] toByteArray()
}

@CompileStatic
class PushConstantInstruction extends Instruction {
	def constant

	PushConstantInstruction(Bytecode b, char i,  c) { super(b, i); constant = c; stackIncrease = 1 }

	byte[] toByteArray() {
		if (null == constant)
			[0x1] as byte[]
		else if (constant instanceof LongConstant || constant instanceof DoubleConstant)
			[0x14, constant.index >> 8, constant.index] as byte[]
		else if (constant instanceof Constant)
			if (constant.index > 255)
				[0x13, constant.index >> 8, constant.index] as byte[]
			else
				[0x12, constant.index] as byte[]
		if (constant instanceof int && constant > -2 && constant < 6)
			[0x3 + constant] as byte[]
		else if (constant instanceof long && constant == 1 || constant == 0)
			[0x9 + constant] as byte[]
		else if (constant instanceof float && constant == 1 || constant == 0 || constant == 2)
			[0xb + constant] as byte[]
		else if (constant instanceof long && constant == 1 || constant == 0)
			[0xe + constant] as byte[]
		else if (constant instanceof byte)
			[0x10, constant] as byte[]
		else if (constant instanceof short)
			[0x11, constant >>> 8, constant] as byte[]
		else throw new IllegalArgumentException('Invalid constant type ' + constant.class)
	}
}

@CompileStatic
class LoadInstruction extends Instruction {
	Class type
	int var
	boolean forceWide

	LoadInstruction(Bytecode b, char i, Class t = null, int v, boolean f = false) {
		super(b, i); stackIncrease = 1
		type = t
		var = v
		forceWide = f
	}

	byte[] toByteArray() {
		if (var > -1 && var < 4)
			[0x1a * typeOrder.indexOf(type) + var] as byte[]
		else if (var > 255 || forceWide)
			[0xc4, 0x15 + typeOrder.indexOf(type), var >>> 8, var] as byte[]
		else
			[0x15 + typeOrder.indexOf(type), var] as byte[]
	}
}

@CompileStatic
class StoreInstruction extends Instruction {
	Class type
	int var
	boolean forceWide

	StoreInstruction(Bytecode b, char i, Class t = null, int v, boolean f = false) {
		super(b, i)
		type = t
		var = v
		forceWide = f
	}

	byte[] toByteArray() {
		if (index > -1 && index < 4)
			[0x3b * typeOrder.indexOf(type) + var] as byte[]
		else if (index > 255 || forceWide)
			[0xc4, 0x36 + typeOrder.indexOf(type), var >>> 8, var] as byte[]
		else
			[0x36 + typeOrder.indexOf(type), var] as byte[]
	}
}

@CompileStatic
class ArrayLoadInstruction extends Instruction {
	Class type

	ArrayLoadInstruction(Bytecode b, char i, Class t = null) {
		super(b, i)
		type = t
	}

	byte[] toByteArray() {
		[0x2e + typeOrder.indexOf(type)] as byte[]
	}
}

@CompileStatic
class ArrayStoreInstruction extends Instruction {
	Class type

	ArrayStoreInstruction(Bytecode b, char i, Class t = null) {
		super(b, i)
		type = t
	}

	byte[] toByteArray() {
		[0x4f + typeOrder.indexOf(type)] as byte[]
	}
}

@CompileStatic
class PopInstruction extends Instruction {
	int times

	PopInstruction(Bytecode b, char i, int times = 1) {
		super(b, i)
		this.times = times
	}

	byte[] toByteArray() {
		(([0x58] * (times / 2)) + ([0x57] * (times % 2))) as byte[]
	}
}

@CompileStatic
class DupInstruction extends Instruction {
	int times
	int type

	DupInstruction(Bytecode b, char i, int times = 1, int type = 0) {
		super(b, i); stackIncrease = times
		this.type = type
		this.times = times
	}

	byte[] toByteArray() {
		(([[0x5c, 0x5d, 0x5e][type]] * (times / 2)) + ([[0x59, 0x5a, 0x5b][type]] * (times % 2))) as byte[]
	}
}

@CompileStatic
class NumberOperatorInstruction extends Instruction {
	static List<String> operators = ['+', '-', '*', '/', '%', '#-', '<<', '>>', '>>>', '&', '|', '^'].asImmutable()
	String operator
	Class type

	NumberOperatorInstruction(Bytecode b, char i, Class t, String op) {
		super(b, i); stackIncrease = operator == '#-' ? 0 : 1
		operator = op
		type = t
	}

	byte[] toByteArray() {
		[0x60 + operators.indexOf(operator) * 4 + typeOrder.indexOf(type)] as byte[]
	}
}

@CompileStatic
class NegateInstruction extends Instruction {
	Class type

	NegateInstruction(Bytecode b, char i, Class t) {
		super(b, i)
		type = t
	}

	byte[] toByteArray() {
		[0x74 + typeOrder.indexOf(type)] as byte[]
	}
}

@CompileStatic
class IncrementInstruction extends Instruction {
	int var
	int count
	boolean forceWide

	IncrementInstruction(Bytecode b, char i, int v, int count, boolean forceWide = false) {
		super(b, i)
		this.var = v
		this.count = count
		this.forceWide = forceWide
	}

	byte[] toByteArray() {
		if (var > 255 || count > 255 || forceWide)
			[0xc4, 0x84, var >>> 8, var, count >>> 8, count] as byte[]
		else
			[0x84, var, count] as byte[]
	}
}

@CompileStatic
class ConvertInstruction extends Instruction {
	Class type1
	Class type2

	ConvertInstruction(Bytecode b, char i, Class type1, Class type2) {
		super(b, i)
		this.type1 = type1
		this.type2 = type2
	}

	byte[] toByteArray() {
		if (type2 == short || type2 == byte || type2 == char)
			[0x8c + typeOrder.indexOf(type2)] as byte[]
		else {
			def (index1, index2) = [typeOrder.indexOf(type1), typeOrder.indexOf(type2)]
			[0x85 + index1 * 3 + index2 - (index1 > index2 ? 0 : 1)] as byte[]
		}
	}
}

@CompileStatic
class CompareInstruction extends Instruction {
	Class type
	boolean great

	CompareInstruction(Bytecode b, char i, Class t, boolean g = false) {
		super(b, i)
		type = t
		great = g
	}

	byte[] toByteArray() {
		if (type == long) [0x94] as byte[]
		else if (type == double) [great ? 0x98 : 0x97] as byte[]
		else if (type == float) [great ? 0x96 : 0x95] as byte[]
	}
}

@CompileStatic
class IfCompareInstruction extends Instruction {
	static List<String> operators = ['==', '!=', '<', '>=', '>', '<=']
	String operator
	char branch
	boolean zero
	boolean reference

	IfCompareInstruction(Bytecode b, char i, String op, char br, boolean zero = false, boolean reference = false) {
		super(b, i)
		operator = op
		branch = br
		this.zero = zero
		this.reference = reference
	}

	byte[] toByteArray() {
		[(zero ? 0x99 : (reference ? 0xa5 : 0x9f)) + operators.indexOf(operator), branch >> 8, branch] as byte[]
	}
}

@CompileStatic
class GoToInstruction extends Instruction {
	char branch

	GoToInstruction(Bytecode b, char i, char branch) {
		super(b, i)
		this.branch = branch
	}

	byte[] toByteArray() {
		[0xa7, branch >> 8, branch] as byte[]
	}
}

@CompileStatic
class WideGoToInstruction extends Instruction {
	long branch

	WideGoToInstruction(Bytecode b, char i, long branch) {
		super(b, i)
		this.branch = branch
	}

	byte[] toByteArray() {
		[0xc8, branch >>> 24, branch >>> 16, branch >>> 8, branch] as byte[]
	}
}

@CompileStatic
class JumpToSubroutineInstruction extends Instruction {
	char branch

	JumpToSubroutineInstruction(Bytecode b, char i, char branch) {
		super(b, i); stackIncrease = 1
		this.branch = branch
	}

	byte[] toByteArray() {
		[0xa8, branch >> 8, branch] as byte[]
	}
}

@CompileStatic
class WideJumpToSubroutineInstruction extends Instruction {
	long branch

	WideJumpToSubroutineInstruction(Bytecode b, char i, long branch) {
		super(b, i); stackIncrease = 1
		this.branch = branch
	}

	byte[] toByteArray() {
		[0xc9, branch >>> 24, branch >>> 16, branch >>> 8, branch] as byte[]
	}
}

@CompileStatic
class ReturnAddressInstruction extends Instruction {
	char var
	boolean forceWide

	ReturnAddressInstruction(Bytecode b, char i, char v, boolean forceWide = false) {
		super(b, i)
		this.var = v
		this.forceWide = forceWide
	}

	byte[] toByteArray() {
		if (var > 255 || forceWide)
			[0xc4, 0xa9, var >> 8, var] as byte[]
		else
			[0xa9, var] as byte[]
	}
}

@CompileStatic
class ReturnInstruction extends Instruction {
	Class type

	ReturnInstruction(Bytecode b, char i, Class type = null) {
		super(b, i)
		this.type = type
	}

	byte[] toByteArray() {
		(type == void ? [0xb1] : [0xac + typeOrder.indexOf(type)]) as byte[]
	}
}

@CompileStatic
class ConstantArgumentInstruction<T extends Constant> extends Instruction {
	static int code
	T constant

	ConstantArgumentInstruction(Bytecode b, char i, T c) {
		super(b, i)
		constant = c
	}

	byte[] toByteArray() {
		[code, constant.index >> 8, constant.index] as byte[]
	}
}

@InheritConstructors
@CompileStatic
class GetStaticInstruction extends ConstantArgumentInstruction<FieldConstant> { static { code = 0xb2 }; { stackIncrease = 1 }}
@InheritConstructors
@CompileStatic
class PutStaticInstruction extends ConstantArgumentInstruction<FieldConstant> { static { code = 0xb3 } }
@InheritConstructors
@CompileStatic
class GetFieldInstruction extends ConstantArgumentInstruction<FieldConstant> { static { code = 0xb4 } }
@InheritConstructors
@CompileStatic
class PutFieldInstruction extends ConstantArgumentInstruction<FieldConstant> { static { code = 0xb5 } }
@InheritConstructors
@CompileStatic
class InvokeVirtualInstruction extends ConstantArgumentInstruction<MethodConstant> { static { code = 0xb6 }; { stackIncrease = 1 }}
@InheritConstructors
@CompileStatic
class InvokeSpecialInstruction extends ConstantArgumentInstruction<MethodConstant> { static { code = 0xb7 } }
@InheritConstructors
@CompileStatic
class InvokeStaticInstruction extends ConstantArgumentInstruction<MethodConstant> { static { code = 0xb8 } }
@CompileStatic
class InvokeInterfaceInstruction extends ConstantArgumentInstruction<InterfaceMethodConstant> {
	static { code = 0xb9 }
	int count

	InvokeInterfaceInstruction(Bytecode b, char i, InterfaceMethodConstant c, int count) {
		super(b, i, c)
		this.count = count
	}

	byte[] toByteArray() { [code, constant.index >> 8, constant.index, count, 0] as byte[] }
}
@InheritConstructors
@CompileStatic
class InvokeDynamicInstruction extends ConstantArgumentInstruction<MethodHandleConstant> {
	static { code = 0xba }
	byte[] toByteArray() { [code, constant.index >> 8, constant.index, 0, 0] as byte[] }
}
@InheritConstructors
@CompileStatic
class InstantiateInstruction extends ConstantArgumentInstruction<ClassConstant> { static { code = 0xbb }
	int getStackDifference(int stackCount) { 1 } }
@InheritConstructors
@CompileStatic
class CreateArrayInstruction extends ConstantArgumentInstruction<ClassConstant> { static { code = 0xbd } }
@InheritConstructors
@CompileStatic
class CastInstruction extends ConstantArgumentInstruction<ClassConstant> { static { code = 0xc0 } }
@InheritConstructors
@CompileStatic
class InstanceOfInstruction extends ConstantArgumentInstruction<ClassConstant> { static { code = 0xc1 } }

@CompileStatic
class CreatePrimitiveArrayInstruction extends Instruction {
	static Map<Class, Byte> primitives = [(boolean): 4, (char): 5, (float): 6, (double): 7,
	                                      (byte): 8, (short): 9, (int): 10, (long): 11]
	Class type

	CreatePrimitiveArrayInstruction(Bytecode b, char i, Class type) {
		super(b, i)
		this.type = type
	}

	byte[] toByteArray() {
		[0xbc, primitives[type]] as byte[]
	}
}

@CompileStatic
class CreateMultidimensionalArray extends ConstantArgumentInstruction<ClassConstant> {
	static { code = 0xc5 }

	int dimensions

	CreateMultidimensionalArray(Bytecode b, char i, ClassConstant c, int dimensions) {
		super(b, i, c)
		this.dimensions = dimensions
	}

	byte[] toByteArray() {
		(super.toByteArray().toList() << dimensions) as byte[]
	}
}

@InheritConstructors class NoOpInstruction extends Instruction { byte[] toByteArray() { [0x00] as byte[] } }
@InheritConstructors class SwapInstruction extends Instruction { byte[] toByteArray() { [0x5f] as byte[] } }
@InheritConstructors class ArrayLengthInstruction extends Instruction { byte[] toByteArray() { [0xbe] as byte[] } }
@InheritConstructors class ThrowInstruction extends Instruction { byte[] toByteArray() { [0xbf] as byte[] } }
@InheritConstructors class MonitorEnterInstruction extends Instruction { byte[] toByteArray() { [0xc2] as byte[] } }
@InheritConstructors class MonitorExitInstruction extends Instruction { byte[] toByteArray() { [0xc3] as byte[] } }
@InheritConstructors class IfNullInstruction extends Instruction { byte[] toByteArray() { [0xc6] as byte[] } }
@InheritConstructors class IfNotNullInstruction extends Instruction { byte[] toByteArray() { [0xc7] as byte[] } }