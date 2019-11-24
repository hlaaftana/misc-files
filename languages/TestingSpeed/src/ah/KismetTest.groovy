package ah

import hlaaftana.kismet.call.Block
import hlaaftana.kismet.Kismet
import groovy.transform.CompileStatic

@CompileStatic
class KismetTest {
	static Block x = Kismet.parse('[x {a b d dc ps [cd c]}]')
	static Block a

	static Block buildA() {
		a = Kismet.parse('''\
don't %"optimize"
defn [fibonacci n] {
  f = 0
  s = 1
  &for n f = + s s = f
}

is? [size [to_set [&for 50 [fibonacci 200]]]] 1''')
	}

	static boolean test() {
		a.child().evaluate() as boolean
	}
}
