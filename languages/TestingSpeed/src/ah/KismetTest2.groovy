package ah

import groovy.transform.CompileStatic
import hlaaftana.kismet.call.Block
import hlaaftana.kismet.Kismet

@CompileStatic
class KismetTest2 {
	static Block x = Kismet.parse('[x {a b d dc ps [cd c]}]')
	static Block a

	static Block buildA() {
		a = Kismet.parse('''\
don't %"optimize"

;; 200 and 50 not being passed as arguments does not affect performance

static {
  timesdo = [tmpl
    [+ [quote [&]]
       [&for [eval $0] $1]]]
}

defn fibonacci [let [f 0 s 1] [timesdo 200 f = + s s = f]]

timesdo(50, fibonacci()).to_set().size().is?(1)''')
	}

	static boolean test() {
		a.child().evaluate() as boolean
	}
}
