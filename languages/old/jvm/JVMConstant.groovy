package hlaaftana.soda.jvm

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.InheritConstructors

@CompileStatic
abstract class JVMConstant {
	int cardinal = 1
	Object[] arguments
	int index

	JVMConstant(int index, ... arguments) {
		this.index = index
		this.arguments = arguments
	}

	abstract init(... args)

	abstract byte[] toBytes()

	protected static hasToBe(thing, Class c) {
		c.isAssignableFrom(thing.class)
	}
}

@EqualsAndHashCode
@EqualsAndHashCode
@InheritConstructors
class JVMUTF8Constant extends JVMConstant {
	UTF8 utf8

	@Override def init(...args) { utf8 = args[0] instanceof String ? new UTF8((String) args[0]) : (UTF8) args[0] }
	
	@Override
	byte[] toBytes() {
		([1, utf8.size() >>> 8, utf8.size()] + utf8.data.toList()) as byte[]
	}
}

@EqualsAndHashCode
@EqualsAndHashCode
@InheritConstructors
class JVMIntConstant extends JVMConstant {
	int value
	
	@Override def init(...args) { value = (int) args[0] }
	
	@Override
	byte[] toBytes() {
		[3, value >>> 24, value >>> 16, value >>> 8, value] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMFloatConstant extends JVMConstant {
	float value

	@Override def init(...args) { value = (float) args[0] }

	@Override
	byte[] toBytes() {
		int x = Float.floatToRawIntBits((float) arguments[0])
		[4, x >>> 24, x >>> 16, x >>> 8, x] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMLongConstant extends JVMConstant {
	long value
	
	{ cardinal = 2 }

	@Override def init(...args) { value = (long) args[0] }

	@Override
	byte[] toBytes() {
		[5, value >>> 56, value >>> 48, value >>> 40, value >>> 32,
		 5, value >>> 24, value >>> 16, value >>> 8, value] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMDoubleConstant extends JVMConstant {
	double value

	{ cardinal = 2 }

	@Override def init(...args) { value = (double) args[0] }

	@Override
	byte[] toBytes() {
		long x = Double.doubleToRawLongBits((double) arguments[0])
		[6, x >>> 56, x >>> 48, x >>> 40, x >>> 32,
		 6, x >>> 24, x >>> 16, x >>> 8, x] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMClassConstant extends JVMConstant {
	JVMUTF8Constant name

	@Override def init(...args) { name = (JVMUTF8Constant) args[0] }

	@Override
	byte[] toBytes() {
		[7, name.index >>> 8, name.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMStringConstant extends JVMConstant {
	JVMUTF8Constant utf8

	@Override def init(...args) { utf8 = (JVMUTF8Constant) args[0] }

	@Override
	byte[] toBytes() {
		[8, utf8.index >>> 8, utf8.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMFieldConstant extends JVMConstant {
	JVMClassConstant clazz
	JVMNameAndTypeConstant nameAndType

	@Override def init(...args) { clazz = (JVMClassConstant) args[0]; nameAndType = (JVMNameAndTypeConstant) args[1] }

	@Override
	byte[] toBytes() {
		[9, clazz.index >>> 8, clazz.index, nameAndType.index >>> 8, nameAndType.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMMethodConstant extends JVMConstant {
	JVMClassConstant clazz
	JVMNameAndTypeConstant nameAndType

	@Override def init(...args) { clazz = (JVMClassConstant) args[0]; nameAndType = (JVMNameAndTypeConstant) args[1] }

	@Override
	byte[] toBytes() {
		[10, clazz.index >>> 8, clazz.index, nameAndType.index >>> 8, nameAndType.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMInterfaceMethodConstant extends JVMConstant {
	JVMClassConstant clazz
	JVMNameAndTypeConstant nameAndType

	@Override def init(...args) { clazz = (JVMClassConstant) args[0]; nameAndType = (JVMNameAndTypeConstant) args[1] }

	@Override
	byte[] toBytes() {
		[11, clazz.index >>> 8, clazz.index, nameAndType.index >>> 8, nameAndType.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMNameAndTypeConstant extends JVMConstant {
	JVMUTF8Constant name
	JVMUTF8Constant descriptor

	@Override def init(...args) { name = (JVMUTF8Constant) args[0]; descriptor = (JVMUTF8Constant) args[1] }

	@Override
	byte[] toBytes() {
		[12, name.index >>> 8, name.index, descriptor.index >>> 8, descriptor.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMMethodHandleConstant extends JVMConstant {
	byte kind
	JVMConstant reference

	@Override def init(...args) {
		kind = (byte) args[0]
		if (kind < 1 || kind > 10)
			throw new IllegalArgumentException('Method handle reference kind ' + kind + ' doesn\'t exist')
		reference = (JVMConstant) args[1]
		if ((kind > 0 && kind < 5 ? JVMFieldConstant : (kind > 5 && kind < 9 ? JVMMethodConstant
				: JVMInterfaceMethodConstant)).isAssignableFrom(reference.class))
			throw new IllegalArgumentException('Wrong type ' + reference.class + ' for reference kind ' + kind)
	}

	@Override
	byte[] toBytes() {
		[15, kind, reference.index >>> 8, reference.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMMethodTypeConstant extends JVMConstant {
	JVMMethodConstant method

	@Override def init(...args) { method = (JVMMethodConstant) args[0] }

	@Override
	byte[] toBytes() {
		[16, method.index >>> 8, method.index] as byte[]
	}
}

@EqualsAndHashCode
@InheritConstructors
class JVMInvokeDynamicConstant extends JVMConstant {
	int bootstrapIndex
	JVMNameAndTypeConstant nameAndType

	@Override def init(...args) { bootstrapIndex = (int) args[0]; nameAndType = (JVMNameAndTypeConstant) args[1] }

	@Override
	byte[] toBytes() {
		[16, bootstrapIndex >>> 8, bootstrapIndex, nameAndType.index >>> 8, nameAndType.index] as byte[]
	}
}