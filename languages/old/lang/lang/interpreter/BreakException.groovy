package hlaaftana.karmafields.lang.interpreter

class BreakException extends Exception {
	boolean done
	String name
	BreakException(String n) {
		super('Unknown scope label '.concat(n))
		name = n
	}
}
