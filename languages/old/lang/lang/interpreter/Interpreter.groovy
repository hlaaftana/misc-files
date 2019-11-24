package hlaaftana.karmafields.lang.interpreter

import groovy.transform.CompileStatic
import hlaaftana.karmafields.lang.*
import hlaaftana.karmafields.lang.interpreter.defaultscope.*

@CompileStatic
class Interpreter {
	static Scope defaultScope = new Scope()

	static {
		defaultScope.set('yes', YesValue.INSTANCE)
		defaultScope.set('no', NoValue.INSTANCE)
		defaultScope.setMacro('macroByRef', new MacroByRefMacro())
		defaultScope.setMacro('=>', new LambdaMacro())
		defaultScope.setMacro('\'=', new MacroLeftAssignMacro())
		defaultScope.setMacro('upscope', new UpscopeMacro())
		defaultScope.setMacro('if', new IfMacro())
		defaultScope.setMacro('unless', new UnlessMacro())
		defaultScope.setMacro('while', new WhileMacro())
		defaultScope.setMacro('until', new UntilMacro())
		defaultScope.setMacro('case', new CaseMacro())
		defaultScope.setMacro('try', new TryMacro())
		defaultScope.setMacro('break', new BreakMacro())
		defaultScope.setMacro('=', new AssignMacro())
		defaultScope.setMacro('*=', new AssignFindMacro())
		defaultScope.setMacro('class', new ClassMacro())
		defaultScope.setMacro('def', new DefMacro())
		defaultScope.setMacro('or', new OrMacro())
		defaultScope.setMacro('and', new AndMacro())
		defaultScope.setMacro('bor', new BorMacro())
		defaultScope.setMacro('bxor', new BxorMacro())
		defaultScope.setMacro('xor', new XorMacro())
		defaultScope.setMacro('band', new BandMacro())
		defaultScope.setMacro('eq', new EqMacro())
		defaultScope.setMacro('neq', new NeqMacro())
		defaultScope.setMacro('same', new SameMacro())
		defaultScope.setMacro('samen\'t', new SamentMacro())
		defaultScope.setMacro('cmp', new CmpMacro())
		defaultScope.setMacro('>', new GreaterMacro())
		defaultScope.setMacro('>=', new GreaterEqMacro())
		defaultScope.setMacro('<', new LessMacro())
		defaultScope.setMacro('<=', new LessEqMacro())
		defaultScope.setMacro('in', new InMacro())
		defaultScope.setMacro('is', new IsMacro())
		defaultScope.setMacro('isn\'t', new IsntMacro())
		defaultScope.setMacro('lsh', new LshMacro())
		defaultScope.setMacro('rsh', new RshMacro())
		defaultScope.setMacro('ursh', new UrshMacro())
		defaultScope.setMacro('+', new PlusMacro())
		defaultScope.setMacro('-', new MinusMacro())
		defaultScope.setMacro('*', new MultiplyMacro())
		defaultScope.setMacro('/', new DivideMacro())
		defaultScope.setMacro('div', new DivMacro())
		defaultScope.setMacro('mod', new ModMacro())
		defaultScope.setMacro('neg', new NegMacro())
		defaultScope.setMacro('^', new PowMacro())
		defaultScope.setMacro('forget', new ForgetMacro())
		defaultScope.set('Block', new ClassValue(name: 'Block'))
		defaultScope.set('hash', new HashFunction())
		defaultScope.set('new', new NewFunction())
		defaultScope.set('iterator', new IteratorFunction())
		defaultScope.set('size', new SizeFunction())
		defaultScope.set('propertyGet', new PropertyGetFunction())
		defaultScope.set('propertySet', new PropertySetFunction())
		defaultScope.set('subscriptGet', new SubscriptGetFunction())
		defaultScope.set('subscriptSet', new SubscriptSetFunction())
	}

	static Value evaluate(Scope scope, Block block) {
		List<Statement> statements = block.code
		Value a = NoValue.INSTANCE
		for (x in statements) {
			try {
				a = evaluate(scope, x)
			} catch (BreakException ex) {
				if (ex.name != scope.label) throw ex
			}
		}
		a
	}

	static Value evaluate(Scope scope, Statement line) {
		if (line.content.length == 0) return NoValue.INSTANCE
		Map<Integer, MacroValue> macros = [:]
		int i = 0
		Token[] content = line.content
		for (x in content) {
			try {
				MacroValue m = null
				if (x instanceof ReferenceToken && (m = (MacroValue) scope.macro(x.text)) && i != 0 || m.start)
					macros.put(i++, m)
			} catch (BreakException ex) {
				if (ex.name != scope.label) throw ex
			} catch (ex) {
				throw new LangException(x.ln, x.cl, ex)
			}
		}
		if (macros.isEmpty()) {
			List<Value> li = []
			for (x in content) {
				try {
					if (x instanceof ParensToken)
						li.add(evaluate(scope, new Statement(line.parent, x.tokens)))
					else if (x instanceof NumberToken)
						li.add(x.toNumberValue())
					else if (x instanceof StringToken)
						li.add(new StringValue(x.value))
					else if (x instanceof PropertyToken) {
						def y = li.pop()
						li.add(y.propertyGet(x.text))
					} else if (x instanceof SubscriptToken) {
						def y = li.pop()
						li.add(y.subscriptGet(evaluate(scope,
								new Statement(line.parent, x.tokens))))
					}
				} catch (BreakException ex) {
					if (ex.name != scope.label) throw ex
				} catch (ex) {
					throw new LangException(x.ln, x.cl, ex)
				}
				Value first = li.get(0)
				if (li.size() == 1)
					first
				else if (first instanceof FunctionValue)
					((FunctionValue) first).call(li.drop(1).toArray() as Value[])
				else
					throw new ArgumentException("Tried to call a non-function value at ${content[0].ln}")
			}
		} else {
			int winnIndex = -1
			MacroValue winner = null
			Integer winP = null
			for (e in macros) {
				MacroValue m = e.value
				if (null == winP || (m.alignment ? m.precedence <= winP : m.precedence < winP)) {
					winner = m
					winnIndex = e.key
					winP = m.precedence
				}
			}
			if (winner)
				try {
					winner.call(scope,
							new Statement(line.parent, content.take(winnIndex)),
							new Statement(line.parent, content.drop(winnIndex + 1)),
							line.block)
				} catch (BreakException ex) {
					if (ex.name != scope.label) throw ex
				}
			else
				throw new IllegalStateException("Macros exist but no macros on top?? ${content[0].ln}")
		}
		throw new IllegalStateException('What the fuck')
	}
}
