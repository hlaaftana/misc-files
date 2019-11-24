package hlaaftana.soda.jvm

import groovy.transform.CompileStatic

@CompileStatic
class JVMClass {
	int accessFlags
	private Set<JVMConstant> constants = []
	private Map<String, JVMUTF8Constant> utf8s = [:]
	List<JVMField> fields = []
	List<JVMMethod> methods = []
	JVMClassConstant itself
	JVMClassConstant superclass
	List<JVMClassConstant> interfaces = []
	List<JVMAttribute> attributes = []

	String getJavaName() {
		itself.name.utf8.string.replace('/' as char, '.' as char)
			.replaceAll(/^L/, '').replaceAll(/;$/, '')
	}

	public <T extends JVMConstant> T addConstant(Class<T> clazz, ...args) {
		T x = clazz.newInstance(constantPoolLength, args)
		boolean y = this.@constants.add(x)
		if (!y) return (T) this.@constants.find { it == x }
		if (x instanceof JVMUTF8Constant) utf8s[((JVMUTF8Constant) x).utf8.string] = (JVMUTF8Constant) x
		x
	}

	boolean removeConstant(JVMConstant c) {
		List<JVMConstant> a = new ArrayList<>(this.@constants)
		int index = a.indexOf(c)
		if (index == -1) return false
		if (c instanceof JVMUTF8Constant) utf8s.values().remove(c)
		a.remove(index)
		for (int i = index; i < this.@constants.size(); i += 1)
			a[i].index -= c.cardinal
		this.@constants = new HashSet<>(a)
		true
	}

	JVMUTF8Constant utf(String s) {
		utf8s[s] ?: addConstant(JVMUTF8Constant, s)
	}

	Set<JVMConstant> getConstants() { new HashSet<JVMConstant>(this.@constants) }

	int getConstantPoolLength() { goodSum(this.@constants.collect { it.cardinal }) + 1 }

	private static int goodSum(List<Integer> list) {
		int a = 0
		for (int i = 0; i < list.size(); i += 1) {
			a += list[i]
		}
		a
	}
}
