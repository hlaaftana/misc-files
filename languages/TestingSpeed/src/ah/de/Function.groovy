package ah.de

import groovy.transform.CompileStatic

abstract class Function implements KismetCallable {}

@CompileStatic
class KismetFunction extends Function {
	KismetObject<Block> b

	KismetFunction(KismetObject<Block> b) {
		this.b = b
	}

	KismetObject call(KismetObject... args){
		def c = b.inner().anonymousClone()
		args.length.times {
			c.context.directSet("\$$it", args[it])
		}
		c.context.directSet('$all', Kismet.model(args.toList()))
		Block.changeBlock(c.expressions, c)
		c()
	}
}

class GroovyFunction extends Function {
	boolean convert = true
	Closure x

	GroovyFunction(boolean convert = true, Closure x) {
		this.convert = convert
		this.x = x
	}

	KismetObject call(KismetObject... args){
		Kismet.model(args ? x(*(convert ? args*.inner() : args)) : x())
	}
}
