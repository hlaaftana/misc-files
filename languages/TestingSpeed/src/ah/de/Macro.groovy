package ah.de

abstract class Macro implements KismetCallable {
	KismetObject call(KismetObject... args) {
		doCall(args)
	}

	abstract KismetObject doCall(KismetObject<Expression>... expressions)
}

class KismetMacro extends Macro {
	KismetObject<Block> b

	KismetMacro(KismetObject<Block> b) {
		this.b = b
	}

	KismetObject doCall(KismetObject<Expression>... args){
		def c = b.inner().anonymousClone()
		args.length.times {
			c.context.directSet("\$$it", args[it])
		}
		c.context.directSet('$all', Kismet.model(args.toList()))
		Block.changeBlock(c.expressions, c)
		c()
	}
}

class GroovyMacro extends Macro {
	boolean convert = true
	Closure x

	GroovyMacro(boolean convert = true, Closure x) {
		this.convert = convert
		this.x = x
	}

	KismetObject doCall(KismetObject<Expression>... expressions){
		Kismet.model(expressions ? x(*(convert ? expressions*.inner() : expressions)) : x())
	}
}