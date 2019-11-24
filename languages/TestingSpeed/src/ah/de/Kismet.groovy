package ah.de

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
class Kismet {
	static Block parse(String code, Map ctxt = new HashMap(KismetInner.defaultContext)){
		Block b = new Block()
		b.raw = code
		b.context = new Context(b, ctxt)
		Expression x = KismetInner.compile(code, b)
		if (x instanceof Block) (Block) x else {
			b.expressions = [x]
			b
		}
	}
	
	static eval(String code, Map ctxt = new HashMap(KismetInner.defaultContext)){
		parse(code, ctxt).evaluate()
	}

	@CompileDynamic
	static KismetObject model(x){ KismetModels.model(x) }
}
