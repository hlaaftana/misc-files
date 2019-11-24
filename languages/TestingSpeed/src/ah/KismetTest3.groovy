package ah

import groovy.transform.CompileStatic
import hlaaftana.kismet.call.Block
import hlaaftana.kismet.scope.Context
import hlaaftana.kismet.Kismet
import hlaaftana.kismet.scope.Prelude

@CompileStatic
class KismetTest3 {
	static Block x = Kismet.parse('[x {a b d dc ps [cd c]}]')
	static Block a

	static Block buildA() {
		a = Kismet.parse('''\
defn [fibonacci n] {
  let [f: 0, s: 1]
  &for n f = + s s = f
}

is? [size [to_set [&for 50 [fibonacci 200i32]]]] 1''',
			new Context(Kismet.DEFAULT_CONTEXT, [echo: Kismet.model(Prelude.funcc(System.out.&println))]))
		println a.expression
	}

	static boolean test() {
		a.child().evaluate() as boolean
	}
}
