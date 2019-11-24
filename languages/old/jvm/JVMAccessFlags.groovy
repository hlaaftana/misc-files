package hlaaftana.soda.jvm

interface JVMAccessFlags {
	static final int PUBLIC       = 0x0001
	static final int PRIVATE      = 0x0002
	static final int PROTECTED    = 0x0004
	static final int STATIC       = 0x0008
	static final int FINAL        = 0x0010
	static final int SUPER        = 0x0020
	static final int SYNCHRONIZED = SUPER
	static final int VOLATILE     = 0x0040
	static final int BRIDGE       = VOLATILE
	static final int TRANSIENT    = 0x0080
	static final int VARARGS      = TRANSIENT
	static final int NATIVE       = 0x0100
	static final int INTERFACE    = 0x0200
	static final int ABSTRACT     = 0x0400
	static final int STRICTFP     = 0x0800
	static final int SYNTHETIC    = 0x1000
	static final int ANNOTATION   = 0x2000
	static final int ENUM         = 0x4000
}
