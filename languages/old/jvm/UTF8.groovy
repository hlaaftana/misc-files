package hlaaftana.soda.jvm

class UTF8 {
	byte[] data

	UTF8(String r) { this(toUTF(r)) }
	UTF8(byte[] d) { data = d }

	String getString() { fromUTF(data) }
	String toString() { string }

	int size() { data.length }

	static ByteArrayOutputStream baos = new ByteArrayOutputStream()
	static DataOutputStream dos = new DataOutputStream(baos)

	static byte[] toUTF(String raw) {
		dos.writeUTF(raw)
		byte[] a = baos.toByteArray()
		dos.flush()
		a
	}

	static String fromUTF(byte[] utf) {
		new DataInputStream(new ByteArrayInputStream(utf)).readUTF()
	}
}
