package lang.interpreter

import groovy.transform.CompileStatic

@CompileStatic
class BreakException extends Exception {
	String name
	BreakException(String n) {
		super('Unknown scope label '.concat(n))
		name = n
	}
}
