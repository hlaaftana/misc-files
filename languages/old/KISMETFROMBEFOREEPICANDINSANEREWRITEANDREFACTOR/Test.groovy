package hlaaftana.oldbutnotvery.kismet

import groovy.transform.CompileStatic
import hlaaftana.oldbutnotvery.kismet.parser.Parser
import hlaaftana.oldbutnotvery.kismet.scope.Prelude
import hlaaftana.oldbutnotvery.kismet.vm.Context

@CompileStatic
class Test {
	static main(args) {
		/*final text = new File('test.ksmt').text
		def p = parser.parse(text)
		println p.repr()
		println p.evaluate(parser.context)*/
		def parser = new Parser()
		parser.context = new Context(Kismet.DEFAULT_CONTEXT, [echo: Kismet.model(Prelude.funcc(System.out.&println))])
		for (f in ['binarysearch', 'compareignorecase', 'factorial', 'fibonacci', 'fizzbuzz', 'memoize']) {
			println "file: $f"
			final file = new File("Kismet/examples/${f}.ksmt")
			parser.parse(file.text).evaluate(parser.context.child())
		}
	}
}
