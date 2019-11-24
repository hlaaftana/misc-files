package hlaaftana.lip

import groovy.transform.CompileStatic

@CompileStatic
abstract class Expression {}

@CompileStatic
class CallExpression extends Expression {
	Expression callee
	Expression arguments

	CallExpression(Expression callee, Expression arguments) {
		this.callee = callee
		this.arguments = arguments
	}
}

@CompileStatic
class NameExpression extends Expression {
	String name

	NameExpression(String name) {
		this.name = name
	}
}