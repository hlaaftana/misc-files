package lang.interpreter.defaultscope

import groovy.transform.CompileStatic
import lang.*
import lang.interpreter.*

@CompileStatic
class Assigner {
	static List<List<Token>> split(Statement left) {
		List<List<Token>> assignments = []
		for (t in left.content)
			if (t instanceof ReferenceToken || t instanceof ParensToken) assignments.add([t])
			else if (assignments.empty) throw new ArgumentException('Weird start to assignment in do=')
			else assignments.last().add(t)
		assignments
	}

	static Value assign(Scope scope, Statement left, Block block, Value value, String oper = "assignment") {
		def cont = left.content
		Token first
		if (cont.size() == 1)
			if ((first = cont[0]) instanceof ReferenceToken) scope.set(first.text, value)
			else if (first instanceof StringToken) scope.set(((StringToken) first).value, value)
			else if (first instanceof ParensToken) {
				Value x = scope.eval(new Statement(left.parent, ((ParensToken) first).tokens))
				if (x instanceof StringValue) scope.set(((StringValue) x).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of ' + oper + ' is not a string')
			}
			else throw new ArgumentException(oper + ' with invalid left side')
		else {
			Value x = scope.eval(new Statement(left.parent, cont.init(), block))
			Token last = cont.last()
			if (last instanceof SubscriptToken)
				x.subscriptSet(scope.eval(new Statement(left.parent, ((SubscriptToken) last).tokens)), value)
			else if (last instanceof ReferenceToken || last instanceof NumberToken)
				x.propertySet(last.text, value)
			else if (last instanceof StringToken)
				x.propertySet(((StringToken) last).value, value)
			else if (last instanceof ParensToken) {
				Value y = scope.eval(new Statement(left.parent, ((ParensToken) last).tokens))
				if (y instanceof StringValue) x.propertySet(((StringValue) y).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of ' + oper + ' with multiple tokens not string')
			} else throw new ArgumentException(oper + ' left side none of subscript, reference, number, string or parens')
		}
		value
	}

	static Value assignFind(Scope scope, Statement left, Block block, Value value, String oper = "finding assignment") {
		def cont = left.content
		Token first
		if (cont.size() == 1)
			if ((first = cont[0]) instanceof ReferenceToken) scope.findSet(first.text, value)
			else if (first instanceof StringToken) scope.findSet(((StringToken) first).value, value)
			else if (first instanceof ParensToken) {
				Value x = scope.eval(new Statement(left.parent, ((ParensToken) first).tokens))
				if (x instanceof StringValue) scope.findSet(((StringValue) x).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of ' + oper + ' is not a string')
			}
			else throw new ArgumentException(oper + ' with invalid left side')
		else {
			Value x = scope.eval(new Statement(left.parent, cont.init(), block))
			Token last = cont.last()
			if (last instanceof SubscriptToken)
				x.subscriptSet(scope.eval(new Statement(left.parent, ((SubscriptToken) last).tokens)), value)
			else if (last instanceof ReferenceToken || last instanceof NumberToken)
				x.propertySet(last.text, value)
			else if (last instanceof StringToken)
				x.propertySet(((StringToken) last).value, value)
			else if (last instanceof ParensToken) {
				Value y = scope.eval(new Statement(left.parent, ((ParensToken) last).tokens))
				if (y instanceof StringValue) x.propertySet(((StringValue) y).inner, value)
				else throw new ArgumentException('Evaluated parentheses on left side of ' + oper + ' with multiple tokens not string')
			} else throw new ArgumentException(oper + ' left side none of subscript, reference, number, string or parens')
		}
		value
	}
}
