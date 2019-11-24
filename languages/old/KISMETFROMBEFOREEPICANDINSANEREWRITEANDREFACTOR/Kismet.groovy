package hlaaftana.oldbutnotvery.kismet

import groovy.transform.CompileStatic
import hlaaftana.oldbutnotvery.kismet.call.Block
import hlaaftana.oldbutnotvery.kismet.vm.IKismetObject
import hlaaftana.oldbutnotvery.kismet.parser.Parser
import hlaaftana.oldbutnotvery.kismet.scope.Prelude
import hlaaftana.oldbutnotvery.kismet.vm.Context
import hlaaftana.oldbutnotvery.kismet.vm.KismetModels

@CompileStatic
class Kismet {
	static final IKismetObject NULL = KismetModels.KISMET_NULL
	static Context DEFAULT_CONTEXT = new Context(null, new HashMap(Prelude.defaultContext))

	static Block parse(String code, Context ctxt = new Context(DEFAULT_CONTEXT)) {
		new Block(new Parser(context: ctxt).parse(code), ctxt)
	}
	
	static IKismetObject eval(String code, Context ctxt = new Context(DEFAULT_CONTEXT)) {
		parse(code, ctxt).evaluate()
	}

	static IKismetObject model(x) { null == x ? NULL : (IKismetObject) ((Object) KismetModels).invokeMethod('model', x) }
}
