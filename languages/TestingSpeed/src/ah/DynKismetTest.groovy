package ah

import ah.dynk.Block
import ah.dynk.Kismet
import groovy.transform.CompileStatic

@CompileStatic
class DynKismetTest {
	static Block x = Kismet.parse('[x {a b d dc ps [cd c]}]')
	static Block a

	static Block buildA(){
		a = Kismet.parse('''\
define unique (function (
	define x (map ())
	foreach a $0 (
		set x a a
	)
	keys x
))

define fibonacci (function (
	define flast 0
	define slast 1
	collect (range 1 $0) (function (
		change slast flast
		change flast (plus flast slast)
		flast
	))
))

define l (list ())
foreach i (range 1 50) (
	add l (fibonacci 20)
)
eq (size (unique l)) 1''')
	}

	static boolean test(){
		a.anonymousClone().evaluate().inner() as boolean
	}
}
