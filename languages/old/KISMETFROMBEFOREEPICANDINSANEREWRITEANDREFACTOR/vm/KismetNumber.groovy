package hlaaftana.oldbutnotvery.kismet.vm

import groovy.transform.CompileStatic
import hlaaftana.oldbutnotvery.kismet.exceptions.CannotOperateException

@CompileStatic
abstract class KismetNumber<T extends Number> extends Number implements IKismetObject<T>, Comparable<KismetNumber<T>> {
	IKismetObject propertyGet(String name) {
		throw new CannotOperateException("get property", "number")
	}

	IKismetObject propertySet(String name, IKismetObject value) {
		throw new CannotOperateException("set property", "number")
	}

	KismetNumber getAt(IKismetObject obj) {
		if (obj instanceof KismetNumber) plus((KismetNumber) obj)
		else this
	}

	IKismetObject putAt(IKismetObject obj, IKismetObject value) {
		if (value instanceof KismetNumber)
			if (obj instanceof KismetNumber)
				set(((KismetNumber) value).plus((KismetNumber) obj))
			else
				set((KismetNumber) value)
		else
			if (obj instanceof KismetNumber)
				set(plus((KismetNumber) obj))
		this
	}

	KismetNumber call(IKismetObject... args) {
		if (args[0] instanceof KismetNumber) multiply((KismetNumber) args[0])
		else throw new CannotOperateException("call", "number")
	}

	abstract T inner()
	abstract void set(Number value)
	abstract KismetNumber plus(KismetNumber obj)
	abstract KismetNumber minus(KismetNumber obj)
	abstract KismetNumber multiply(KismetNumber obj)
	abstract KismetNumber div(KismetNumber obj)
	abstract KismetNumber intdiv(KismetNumber obj)
	abstract KismetNumber mod(KismetNumber obj)
	abstract KismetNumber unaryPlus()
	abstract KismetNumber unaryMinus()
	abstract int compareTo(KismetNumber obj)
	abstract KismetNumber leftShift(KismetNumber obj)
	abstract KismetNumber rightShift(KismetNumber obj)
	abstract KismetNumber rightShiftUnsigned(KismetNumber obj)
	abstract KismetNumber and(KismetNumber obj)
	abstract KismetNumber or(KismetNumber obj)
	abstract KismetNumber xor(KismetNumber obj)
	abstract KismetNumberClass kismetClass()

	byte byteValue() { inner().byteValue() }
	short shortValue() { inner().shortValue() }
	int intValue() { inner().intValue() }
	long longValue() { inner().longValue() }
	float floatValue() { inner().floatValue() }
	double doubleValue() { inner().doubleValue() }

	static KismetNumber from(Number val) {
		if (val instanceof BigInteger) new KInt((BigInteger) val)
		else if (val instanceof BigDecimal) new KFloat((BigDecimal) val)
		else if (val instanceof Integer) new KInt32(val.intValue())
		else if (val instanceof Double) new KFloat64(val.doubleValue())
		else if (val instanceof Float) new KFloat32(val.floatValue())
		else if (val instanceof Long) new KInt64(val.longValue())
		else if (val instanceof Short) new KInt16(val.shortValue())
		else if (val instanceof Byte) new KInt8(val.byteValue())
		else new KNonPrimitiveNum(val)
	}

	String toString() { inner().toString() }
}

@CompileStatic
abstract class KismetNumberClass<T extends Number> implements IKismetClass<KismetNumber<T>>, IKismetObject<KismetNumberClass<T>> {
	KismetNumber<T> cast(IKismetObject object) {
		if (!(object instanceof KismetNumber)) throw new CannotOperateException("cast to non number", "number")
		instantiate(((KismetNumber) object).inner())
	}

	abstract KismetNumber<T> instantiate(Number num)
	abstract int bits()

	IKismetClass kismetClass() { MetaKismetClass.INSTANCE }
	KismetNumberClass<T> inner() { this }

	IKismetObject propertyGet(String name) {
		switch (name) {
		case "getBits": return new KInt32(bits())
		case "name": return new KismetString(name)
		default: throw new CannotOperateException("get property $name", "number class ${this.name}")
		}
	}

	IKismetObject propertySet(String name, IKismetObject value) {
		throw new CannotOperateException("set property $name", "number class ${this.name}")
	}

	@Override
	IKismetObject getAt(IKismetObject obj) {
		if (obj.inner() instanceof String) propertyGet((String) obj.inner())
		else throw new CannotOperateException("get at", "number class ${this.name}")
	}

	@Override
	IKismetObject putAt(IKismetObject obj, IKismetObject value) {
		throw new CannotOperateException("set at", "number class ${this.name}")
	}

	@Override
	IKismetObject call(IKismetObject[] args) {
		instantiate(args[0].inner() as Number)
	}
}

@CompileStatic
class NonPrimitiveNumClass extends KismetNumberClass {
	static final NonPrimitiveNumClass INSTANCE = new NonPrimitiveNumClass()

	private NonPrimitiveNumClass() {}

	KismetNumber instantiate(Number num) { new KNonPrimitiveNum(num) }

	String getName() { 'UnknownNumber' }

	boolean isInstance(IKismetObject object) {
		object instanceof KNonPrimitiveNum
	}

	int bits() { 0 }
}

@CompileStatic
final class KNonPrimitiveNum extends KismetNumber {
	Number inner

	KNonPrimitiveNum(Number inner) {
		this.inner = inner
	}

	NonPrimitiveNumClass kismetClass() { NonPrimitiveNumClass.INSTANCE }
	Number inner() { inner }
	void set(Number value) { inner = value }

	KNonPrimitiveNum plus(KismetNumber obj) { new KNonPrimitiveNum(inner + obj.inner()) }
	KNonPrimitiveNum minus(KismetNumber obj) { new KNonPrimitiveNum(inner - obj.inner()) }
	KNonPrimitiveNum multiply(KismetNumber obj) { new KNonPrimitiveNum(inner * obj.inner()) }
	KNonPrimitiveNum div(KismetNumber obj) { new KNonPrimitiveNum(inner / obj.inner()) }
	KNonPrimitiveNum intdiv(KismetNumber obj) { new KNonPrimitiveNum(inner.intdiv(obj.inner())) }
	KNonPrimitiveNum mod(KismetNumber obj) { new KNonPrimitiveNum(inner % obj.inner()) }
	KNonPrimitiveNum unaryPlus() { new KNonPrimitiveNum(inner) }
	KNonPrimitiveNum unaryMinus() { new KNonPrimitiveNum(inner.unaryMinus()) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KNonPrimitiveNum leftShift(KismetNumber obj) { new KNonPrimitiveNum(inner << obj.inner()) }
	KNonPrimitiveNum rightShift(KismetNumber obj) { new KNonPrimitiveNum(inner >> obj.inner()) }
	KNonPrimitiveNum rightShiftUnsigned(KismetNumber obj) { new KNonPrimitiveNum(inner >>> obj.inner()) }
	KNonPrimitiveNum and(KismetNumber obj) { new KNonPrimitiveNum(inner & obj.inner()) }
	KNonPrimitiveNum or(KismetNumber obj) { new KNonPrimitiveNum(inner | obj.inner()) }
	KNonPrimitiveNum xor(KismetNumber obj) { new KNonPrimitiveNum(inner ^ obj.inner()) }
}

@CompileStatic
class IntClass extends KismetNumberClass<BigInteger> {
	static final IntClass INSTANCE = new IntClass()

	private IntClass() {}

	KismetNumber<BigInteger> instantiate(Number num) {
		new KInt(toBigInt(num))
	}

	static BigInteger toBigInt(Number num) {
		if (num instanceof BigInteger) (BigInteger) num
		else if (num instanceof BigDecimal) ((BigDecimal) num).toBigInteger()
		else BigInteger.valueOf(num.longValue())
	}

	boolean isInstance(IKismetObject object) {
		object instanceof KInt
	}

	int bits() { 0 }
	String getName() { 'Integer' }
}

@CompileStatic
final class KInt extends KismetNumber<BigInteger> {
	BigInteger inner

	KInt(BigInteger inner) { this.inner = inner }
	KInt(Number inner) { set(inner) }

	IntClass kismetClass() { IntClass.INSTANCE }
	BigInteger inner() { inner }
	void set(Number value) { inner = IntClass.toBigInt(value) }

	KInt plus(KismetNumber obj) { new KInt(inner.add(IntClass.toBigInt(obj.inner()))) }
	KInt minus(KismetNumber obj) { new KInt(inner.subtract(IntClass.toBigInt(obj.inner()))) }
	KInt multiply(KismetNumber obj) { new KInt(inner.multiply(IntClass.toBigInt(obj.inner()))) }
	KInt div(KismetNumber obj) { new KInt(inner.divide(IntClass.toBigInt(obj.inner()))) }
	KInt intdiv(KismetNumber obj) { new KInt(inner.intdiv(IntClass.toBigInt(obj.inner()))) }
	KInt mod(KismetNumber obj) { new KInt(inner.mod(IntClass.toBigInt(obj.inner()))) }
	KInt unaryPlus() { new KInt(inner) }
	KInt unaryMinus() { new KInt(inner.negate()) }

	int compareTo(KismetNumber obj) { inner.compareTo(IntClass.toBigInt(obj.inner())) }

	KInt leftShift(KismetNumber obj) { new KInt(inner.shiftLeft(obj.intValue())) }
	KInt rightShift(KismetNumber obj) { new KInt(inner.shiftRight(obj.intValue())) }
	KInt rightShiftUnsigned(KismetNumber obj) { throw new CannotOperateException("right shift unsigned", "big integer") }
	KInt and(KismetNumber obj) { new KInt(inner.and(IntClass.toBigInt(obj.inner()))) }
	KInt or(KismetNumber obj) { new KInt(inner.or(IntClass.toBigInt(obj.inner()))) }
	KInt xor(KismetNumber obj) { new KInt(inner.xor(IntClass.toBigInt(obj.inner()))) }
}


@CompileStatic
class FloatClass extends KismetNumberClass<BigDecimal> {
	static final FloatClass INSTANCE = new FloatClass()

	private FloatClass() {}

	KismetNumber<BigDecimal> instantiate(Number num) { new KFloat(num.doubleValue()) }

	static BigDecimal toBigDec(Number num) {
		if (num instanceof BigDecimal) (BigDecimal) num
		else if (num instanceof BigInteger) ((BigInteger) num).toBigDecimal()
		else BigDecimal.valueOf(num.doubleValue())
	}

	boolean isInstance(IKismetObject object) {
		object instanceof KFloat
	}

	int bits() { 0 }
	String getName() { 'Float' }
}

@CompileStatic
final class KFloat extends KismetNumber<BigDecimal> {
	BigDecimal inner

	KFloat(BigDecimal inner) { this.inner = inner }
	KFloat(Number inner) { set(inner) }

	FloatClass kismetClass() { FloatClass.INSTANCE }
	BigDecimal inner() { inner }
	void set(Number value) { inner = FloatClass.toBigDec(value) }

	KFloat plus(KismetNumber obj) { new KFloat(inner.add(FloatClass.toBigDec(obj.inner()))) }
	KFloat minus(KismetNumber obj) { new KFloat(inner.subtract(FloatClass.toBigDec(obj.inner()))) }
	KFloat multiply(KismetNumber obj) { new KFloat(inner.multiply(FloatClass.toBigDec(obj.inner()))) }
	KFloat div(KismetNumber obj) { new KFloat(inner.divide(FloatClass.toBigDec(obj.inner()))) }
	KFloat intdiv(KismetNumber obj) { new KFloat(inner.intdiv(FloatClass.toBigDec(obj.inner()))) }
	KFloat mod(KismetNumber obj) { new KFloat(inner.mod(FloatClass.toBigDec(obj.inner()))) }
	KFloat unaryPlus() { new KFloat(inner) }
	KFloat unaryMinus() { new KFloat(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KFloat leftShift(KismetNumber obj) { throw new CannotOperateException("bitwise (left shift)", "big float") }
	KFloat rightShift(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift)", "big float") }
	KFloat rightShiftUnsigned(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift unsigned)", "big float") }
	KFloat and(KismetNumber obj) { throw new CannotOperateException("bitwise (and)", "big float") }
	KFloat or(KismetNumber obj) { throw new CannotOperateException("bitwise (or)", "big float") }
	KFloat xor(KismetNumber obj) { throw new CannotOperateException("bitwise (xor)", "big float") }
}

@CompileStatic
class Float64Class extends KismetNumberClass<Double> {
	static final Float64Class INSTANCE = new Float64Class()

	private Float64Class() {}

	KismetNumber<Double> instantiate(Number num) { new KFloat64(num.doubleValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KFloat64
	}

	int bits() { 64 }
	String getName() { 'Float64' }
}

@CompileStatic
final class KFloat64 extends KismetNumber<Double> {
	double inner

	KFloat64(double inner) { this.inner = inner }

	Float64Class kismetClass() { Float64Class.INSTANCE }
	Double inner() { inner }
	void set(Number value) { inner = value.doubleValue() }

	KFloat64 plus(KismetNumber obj) { new KFloat64(inner + obj.doubleValue()) }
	KFloat64 minus(KismetNumber obj) { new KFloat64(inner - obj.doubleValue()) }
	KFloat64 multiply(KismetNumber obj) { new KFloat64(inner * obj.doubleValue()) }
	KFloat64 div(KismetNumber obj) { new KFloat64(inner / obj.doubleValue()) }
	KFloat64 intdiv(KismetNumber obj) { new KFloat64(inner.intdiv(obj.doubleValue()).doubleValue()) }
	KFloat64 mod(KismetNumber obj) { new KFloat64(inner % obj.doubleValue()) }
	KFloat64 unaryPlus() { new KFloat64(inner) }
	KFloat64 unaryMinus() { new KFloat64(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KFloat64 leftShift(KismetNumber obj) { throw new CannotOperateException("bitwise (left shift)", "float64") }
	KFloat64 rightShift(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift)", "float64") }
	KFloat64 rightShiftUnsigned(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift unsigned)", "float64") }
	KFloat64 and(KismetNumber obj) { throw new CannotOperateException("bitwise (and)", "float64") }
	KFloat64 or(KismetNumber obj) { throw new CannotOperateException("bitwise (or)", "float64") }
	KFloat64 xor(KismetNumber obj) { throw new CannotOperateException("bitwise (xor)", "float64") }
}

@CompileStatic
class Float32Class extends KismetNumberClass<Float> {
	static final Float32Class INSTANCE = new Float32Class()

	private Float32Class() {}

	KismetNumber<Float> instantiate(Number num) { new KFloat32(num.floatValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KFloat32
	}

	int bits() { 32 }
	String getName() { 'Float32' }
}

@CompileStatic
final class KFloat32 extends KismetNumber<Float> {
	float inner

	KFloat32(float inner) { this.inner = inner }

	Float32Class kismetClass() { Float32Class.INSTANCE }
	Float inner() { inner }
	void set(Number value) { inner = value.floatValue() }

	KFloat32 plus(KismetNumber obj) { new KFloat32((float) (inner + obj.floatValue())) }
	KFloat32 minus(KismetNumber obj) { new KFloat32((float) (inner - obj.floatValue())) }
	KFloat32 multiply(KismetNumber obj) { new KFloat32((float) (inner * obj.floatValue())) }
	KFloat32 div(KismetNumber obj) { new KFloat32((float) (inner / obj.floatValue())) }
	KFloat32 intdiv(KismetNumber obj) { new KFloat32(inner.intdiv(obj.floatValue()).floatValue()) }
	KFloat32 mod(KismetNumber obj) { new KFloat32(inner % obj.floatValue()) }
	KFloat32 unaryPlus() { new KFloat32(inner) }
	KFloat32 unaryMinus() { new KFloat32(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KFloat32 leftShift(KismetNumber obj) { throw new CannotOperateException("bitwise (left shift)", "float32") }
	KFloat32 rightShift(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift)", "float32") }
	KFloat32 rightShiftUnsigned(KismetNumber obj) { throw new CannotOperateException("bitwise (right shift unsigned)", "float32") }
	KFloat32 and(KismetNumber obj) { throw new CannotOperateException("bitwise (and)", "float32") }
	KFloat32 or(KismetNumber obj) { throw new CannotOperateException("bitwise (or)", "float32") }
	KFloat32 xor(KismetNumber obj) { throw new CannotOperateException("bitwise (xor)", "float32") }
}

@CompileStatic
class Int64Class extends KismetNumberClass<Long> {
	static final Int64Class INSTANCE = new Int64Class()

	private Int64Class() {}

	KismetNumber<Long> instantiate(Number num) { new KInt64(num.longValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KInt64
	}

	int bits() { 64 }
	String getName() { 'Int64' }
}

@CompileStatic
final class KInt64 extends KismetNumber<Long> {
	long inner

	KInt64(long inner) { this.inner = inner }

	Int64Class kismetClass() { Int64Class.INSTANCE }
	Long inner() { inner }
	void set(Number value) { inner = value.longValue() }

	KInt64 plus(KismetNumber obj) { new KInt64(inner + obj.longValue()) }
	KInt64 minus(KismetNumber obj) { new KInt64(inner - obj.longValue()) }
	KInt64 multiply(KismetNumber obj) { new KInt64(inner * obj.longValue()) }
	KInt64 div(KismetNumber obj) { new KInt64((long) (inner / obj.longValue())) }
	KInt64 intdiv(KismetNumber obj) { new KInt64(inner.intdiv(obj.longValue()).longValue()) }
	KInt64 mod(KismetNumber obj) { new KInt64(inner % obj.longValue()) }
	KInt64 unaryPlus() { new KInt64(inner) }
	KInt64 unaryMinus() { new KInt64(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KInt64 leftShift(KismetNumber obj) { new KInt64(inner << obj.longValue()) }
	KInt64 rightShift(KismetNumber obj) { new KInt64(inner >> obj.longValue()) }
	KInt64 rightShiftUnsigned(KismetNumber obj) { new KInt64(inner >>> obj.longValue()) }
	KInt64 and(KismetNumber obj) { new KInt64(inner & obj.longValue()) }
	KInt64 or(KismetNumber obj) { new KInt64(inner | obj.longValue()) }
	KInt64 xor(KismetNumber obj) { new KInt64(inner ^ obj.longValue()) }
}

@CompileStatic
class Int32Class extends KismetNumberClass<Integer> {
	static final Int32Class INSTANCE = new Int32Class()

	private Int32Class() {}

	KismetNumber<Integer> instantiate(Number num) { new KInt32(num.intValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KInt32
	}

	int bits() { 32 }
	String getName() { 'Int32' }
}

@CompileStatic
final class KInt32 extends KismetNumber<Integer> {
	int inner

	KInt32(int inner) { this.inner = inner }

	Int32Class kismetClass() { Int32Class.INSTANCE }
	Integer inner() { inner }
	void set(Number value) { inner = value.intValue() }

	KInt32 plus(KismetNumber obj) { new KInt32(inner + obj.intValue()) }
	KInt32 minus(KismetNumber obj) { new KInt32(inner - obj.intValue()) }
	KInt32 multiply(KismetNumber obj) { new KInt32(inner * obj.intValue()) }
	KInt32 div(KismetNumber obj) { new KInt32((int) (inner / obj.intValue())) }
	KInt32 intdiv(KismetNumber obj) { new KInt32(inner.intdiv(obj.intValue()).intValue()) }
	KInt32 mod(KismetNumber obj) { new KInt32(inner % obj.intValue()) }
	KInt32 unaryPlus() { new KInt32(inner) }
	KInt32 unaryMinus() { new KInt32(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KInt32 leftShift(KismetNumber obj) { new KInt32(inner << obj.intValue()) }
	KInt32 rightShift(KismetNumber obj) { new KInt32(inner >> obj.intValue()) }
	KInt32 rightShiftUnsigned(KismetNumber obj) { new KInt32(inner >>> obj.intValue()) }
	KInt32 and(KismetNumber obj) { new KInt32(inner & obj.intValue()) }
	KInt32 or(KismetNumber obj) { new KInt32(inner | obj.intValue()) }
	KInt32 xor(KismetNumber obj) { new KInt32(inner ^ obj.intValue()) }
}

@CompileStatic
class Int16Class extends KismetNumberClass<Short> {
	static final Int16Class INSTANCE = new Int16Class()

	private Int16Class() {}

	KismetNumber<Short> instantiate(Number num) { new KInt16(num.shortValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KInt16
	}

	int bits() { 16 }
	String getName() { 'Int16' }
}

@CompileStatic
final class KInt16 extends KismetNumber<Short> {
	short inner

	KInt16(short inner) { this.inner = inner }

	Int16Class kismetClass() { Int16Class.INSTANCE }
	Short inner() { inner }
	void set(Number value) { inner = value.shortValue() }

	KInt16 plus(KismetNumber obj) { new KInt16((short) (inner + obj.shortValue())) }
	KInt16 minus(KismetNumber obj) { new KInt16((short) (inner - obj.shortValue())) }
	KInt16 multiply(KismetNumber obj) { new KInt16((short) (inner * obj.shortValue())) }
	KInt16 div(KismetNumber obj) { new KInt16((short) (inner / obj.shortValue())) }
	KInt16 intdiv(KismetNumber obj) { new KInt16(inner.intdiv(obj.shortValue()).shortValue()) }
	KInt16 mod(KismetNumber obj) { new KInt16((short) (inner % obj.shortValue())) }
	KInt16 unaryPlus() { new KInt16(inner) }
	KInt16 unaryMinus() { new KInt16(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KInt16 leftShift(KismetNumber obj) { new KInt16((short) (inner << obj.shortValue())) }
	KInt16 rightShift(KismetNumber obj) { new KInt16((short) (inner >> obj.shortValue())) }
	KInt16 rightShiftUnsigned(KismetNumber obj) { new KInt16((short) (inner >>> obj.shortValue())) }
	KInt16 and(KismetNumber obj) { new KInt16((short) (inner & obj.shortValue())) }
	KInt16 or(KismetNumber obj) { new KInt16((short) (inner | obj.shortValue())) }
	KInt16 xor(KismetNumber obj) { new KInt16((short) (inner ^ obj.shortValue())) }
}

@CompileStatic
class CharClass extends KismetNumberClass<Integer> {
	static final CharClass INSTANCE = new CharClass()

	private CharClass() {}

	KismetNumber<Integer> instantiate(Number num) { new KChar((char) num.intValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KChar
	}

	int bits() { 16 }
	String getName() { 'Character' }
}

@CompileStatic
final class KChar extends KismetNumber<Integer> {
	char inner

	KChar(char inner) { this.inner = inner }

	CharClass kismetClass() { CharClass.INSTANCE }
	Integer inner() { Integer.valueOf((int) inner) }
	void set(Number value) { inner = (char) value.intValue() }

	KChar plus(KismetNumber obj) { new KChar((char) (((int) inner) + obj.intValue()).intValue()) }
	KChar minus(KismetNumber obj) { new KChar((char) (((int) inner) - obj.intValue()).intValue()) }
	KChar multiply(KismetNumber obj) { new KChar((char) (((int) inner) * obj.intValue()).intValue()) }
	KChar div(KismetNumber obj) { new KChar((char) (((int) inner) / obj.intValue()).intValue()) }
	KChar intdiv(KismetNumber obj) { new KChar((char) ((int) inner).intdiv(obj.intValue()).intValue()) }
	KChar mod(KismetNumber obj) { new KChar((char) (((int) inner) % obj.intValue()).intValue()) }
	KChar unaryPlus() { new KChar(inner) }
	KChar unaryMinus() { new KChar(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KChar leftShift(KismetNumber obj) { new KChar((char) (((int) inner) << obj.intValue()).intValue()) }
	KChar rightShift(KismetNumber obj) { new KChar((char) (((int) inner) >> obj.intValue()).intValue()) }
	KChar rightShiftUnsigned(KismetNumber obj) { new KChar((char) (((int) inner) >>> obj.intValue()).intValue()) }
	KChar and(KismetNumber obj) { new KChar((char) (((int) inner) & obj.intValue()).intValue()) }
	KChar or(KismetNumber obj) { new KChar((char) (((int) inner) | obj.intValue()).intValue()) }
	KChar xor(KismetNumber obj) { new KChar((char) (((int) inner) ^ obj.intValue()).intValue()) }
}

@CompileStatic
class Int8Class extends KismetNumberClass<Byte> {
	static final Int8Class INSTANCE = new Int8Class()

	private Int8Class() {}

	KismetNumber<Byte> instantiate(Number num) { new KInt8(num.byteValue()) }

	boolean isInstance(IKismetObject object) {
		object instanceof KInt8
	}

	int bits() { 8 }
	String getName() { 'Int8' }
}

@CompileStatic
final class KInt8 extends KismetNumber<Byte> {
	byte inner

	KInt8(byte inner) { this.inner = inner }

	Int8Class kismetClass() { Int8Class.INSTANCE }
	Byte inner() { inner }
	void set(Number value) { inner = value.byteValue() }

	KInt8 plus(KismetNumber obj) { new KInt8((byte) (inner + obj.byteValue())) }
	KInt8 minus(KismetNumber obj) { new KInt8((byte) (inner - obj.byteValue())) }
	KInt8 multiply(KismetNumber obj) { new KInt8((byte) (inner * obj.byteValue())) }
	KInt8 div(KismetNumber obj) { new KInt8((byte) (inner / obj.byteValue())) }
	KInt8 intdiv(KismetNumber obj) { new KInt8(inner.intdiv(obj.byteValue()).byteValue()) }
	KInt8 mod(KismetNumber obj) { new KInt8((byte) (inner % obj.byteValue())) }
	KInt8 unaryPlus() { new KInt8(inner) }
	KInt8 unaryMinus() { new KInt8(-inner) }

	int compareTo(KismetNumber obj) { inner.compareTo(obj.inner()) }

	KInt8 leftShift(KismetNumber obj) { new KInt8((byte) (inner << obj.byteValue())) }
	KInt8 rightShift(KismetNumber obj) { new KInt8((byte) (inner >> obj.byteValue())) }
	KInt8 rightShiftUnsigned(KismetNumber obj) { new KInt8((byte) (inner >>> obj.byteValue())) }
	KInt8 and(KismetNumber obj) { new KInt8((byte) (inner & obj.byteValue())) }
	KInt8 or(KismetNumber obj) { new KInt8((byte) (inner | obj.byteValue())) }
	KInt8 xor(KismetNumber obj) { new KInt8((byte) (inner ^ obj.byteValue())) }
}