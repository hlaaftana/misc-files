package hlaaftana.oldbutnotvery.kismet.call

import groovy.transform.CompileStatic
import hlaaftana.oldbutnotvery.kismet.vm.IKismetObject
import hlaaftana.oldbutnotvery.kismet.vm.Context

@CompileStatic
class Block {
	Expression expression
	Context context

	Block(Expression expr, Context context = new Context()) {
		expression = expr
		this.context = context
	}

	IKismetObject evaluate() { expression.evaluate(context) }

	IKismetObject call() { evaluate() }

	Block child() {
		new Block(expression, new Context(context))
	}
}