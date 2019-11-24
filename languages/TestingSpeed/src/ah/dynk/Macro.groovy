package ah.dynk

abstract class Macro {
	abstract KismetObject call(KismetObject<Expression>... expressions)
}

class KismetMacro extends Macro {
	KismetObject<Block> b

	KismetObject call(KismetObject<Expression>... args){
		def c = b.inner().anonymousClone()
		args.length.times {
			c.context.set("\$$it", args[it])
		}
		c.context.set('$all', Kismet.model(args.toList()))
		Block.changeBlock(c.expressions, c)
		c()
	}
}

class GroovyMacro extends Macro {
	boolean convert = true
	Closure x

	KismetObject call(KismetObject<Expression>... expressions){
		Kismet.model(expressions ? x(*(convert ? expressions*.inner() : expressions)) : x())
	}
}