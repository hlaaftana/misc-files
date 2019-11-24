package ah

import groovy.transform.CompileStatic
import hlaaftana.kismet.Kismet
import hlaaftana.kismet.call.Block
import hlaaftana.kismet.call.Instruction
import hlaaftana.kismet.parser.Parser
import hlaaftana.kismet.scope.Context
import hlaaftana.kismet.scope.Prelude
import hlaaftana.kismet.scope.TypedContext
import hlaaftana.kismet.vm.IKismetObject
import hlaaftana.kismet.vm.Memory
import hlaaftana.kismet.vm.RuntimeMemory

@CompileStatic
class VMKismetTest {
	static Instruction a
	static TypedContext tc

	static void buildA() {
		def parser = new Parser()
		parser.context = new Context(Kismet.DEFAULT_CONTEXT, [echo: (IKismetObject) Prelude.funcc(System.out.&println)])
		tc = Prelude.typed.child()
		a = parser.parse('''\
defn [fibonacci n] {
  let [f: 0, s: 1]
  &for n f = + s s = f
}

is? [size [to_set [&for 50 [fibonacci 200i32]]]] 1''').type(tc).instruction
	}

	static boolean test() {
		def rm = new RuntimeMemory([Prelude.typed] as Memory[], tc.size())
		a.evaluate(rm) as boolean
	}
}
