package lang.interpreter

import groovy.transform.CompileStatic
import lang.*
import lang.interpreter.defaultscope.*

@CompileStatic
class Interpreter {
	static Scope defaultScope = new Scope()

	static {
		defaultScope.set('no', NoValue.INSTANCE)
		defaultScope.set('yes', YesValue.INSTANCE)
		defaultScope.setMacro('macroByRef', new MacroByRefMacro())
		//defaultScope.setMacro('=>', new LambdaMacro())
		defaultScope.setMacro('\'=', new MacroLeftAssignMacro())
		defaultScope.setMacro('upscope', new UpscopeMacro())
		defaultScope.setMacro('if', new IfMacro())
		defaultScope.setMacro('unless', new UnlessMacro())
		defaultScope.setMacro('while', new WhileMacro())
		defaultScope.setMacro('until', new UntilMacro())
		//defaultScope.setMacro('case', new CaseMacro())
		//defaultScope.setMacro('try', new TryMacro())
		defaultScope.setMacro('break', new BreakMacro())
		defaultScope.setMacro(':=', new AssignMacro())
		defaultScope.setMacro('do=', new DoAssignMacro())
		defaultScope.setMacro('<<:=', new EachAssignMacro())
		defaultScope.setMacro('=', new AssignFindMacro())
		//defaultScope.setMacro('class', new ClassMacro())
		//defaultScope.setMacro('def', new DefMacro())
		defaultScope.setMacro('incr', new IncrementMacro())
		defaultScope.setMacro('decr', new DecrementMacro())
		defaultScope.setMacro('or', new OrMacro())
		defaultScope.setMacro('and', new AndMacro())
		defaultScope.setMacro('bitor', new BitorMacro())
		defaultScope.setMacro('bitxor', new BitxorMacro())
		defaultScope.setMacro('xor', new XorMacro())
		defaultScope.setMacro('bitand', new BitandMacro())
		defaultScope.setMacro('eq', new EqMacro())
		defaultScope.setMacro('neq', new NeqMacro())
		defaultScope.setMacro('same', new SameMacro())
		defaultScope.setMacro('samen\'t', new SamentMacro())
		defaultScope.setMacro('cmp', new CmpMacro())
		defaultScope.setMacro('>', new GreaterMacro())
		defaultScope.setMacro('>=', new GreaterEqMacro())
		defaultScope.setMacro('<', new LessMacro())
		defaultScope.setMacro('<=', new LessEqMacro())
		defaultScope.setMacro('<<', new AppendMacro())
		defaultScope.setMacro('over', defaultScope.setMacro('<<:', new OverMacro()))
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
		defaultScope.set('hash', new HashFunction())
		defaultScope.set('new', new NewFunction())
		defaultScope.set('iterator', new IteratorFunction())
		defaultScope.set('next', new NextFunction())
		defaultScope.set('hasNext', new HasNextFunction())
		defaultScope.set('removeIter', new RemoveIterFunction())
		defaultScope.set('size', new SizeFunction())
		defaultScope.set('propertyGet', new PropertyGetFunction())
		defaultScope.set('propertySet', new PropertySetFunction())
		defaultScope.set('subscriptGet', new SubscriptGetFunction())
		defaultScope.set('subscriptSet', new SubscriptSetFunction())
	}

	static Value evaluate(Scope scope, Block block) {
		List<Statement> statements = block.code
		Value a = null
		for (x in statements) {
			try {
				a = evaluate(scope, x)
			} catch (BreakException ex) {
				if (ex.name != scope.label) throw ex else break
			}
		}
		null == a ? NoValue.INSTANCE : a
	}

	static Value evaluate(Scope scope, Statement line) {
		if (line.content.empty) return NoValue.INSTANCE
		Map<Integer, MacroValue> macros = [:]
		int i = 0
		List<Token> content = line.content
		for (x in content) {
			try {
				MacroValue m
				if (x instanceof ReferenceToken && null != (m = (MacroValue) scope.macro(x.text))) {
					println m
					if (i != 0 || m.start) macros.put(i++, m)
				}
			} catch (BreakException ex) {
				if (ex.name != scope.label) throw ex else return
			} catch (ex) {
				throw new LangException(x.ln, x.cl, ex)
			}
			++i
		}
		if (macros.isEmpty()) {
			List<Value> li = []
			for (x in content) {
				try {
					if (x instanceof ParensToken)
						li.add(evaluate(scope, new Statement(line.parent, ((ParensToken) x).tokens)))
					else if (x instanceof NumberToken)
						li.add(((NumberToken) x).toNumberValue())
					else if (x instanceof StringToken)
						li.add(new StringValue(((StringToken) x).value))
					else if (x instanceof PropertyToken) {
						def y = li.pop()
						li.add(y.propertyGet(x.text))
					} else if (x instanceof SubscriptToken) {
						def y = li.pop()
						li.add(y.subscriptGet(evaluate(scope,
								new Statement(line.parent, ((SubscriptToken) x).tokens))))
					}
				} catch (BreakException ex) {
					if (ex.name != scope.label) throw ex
				} catch (ex) {
					throw new LangException(x.ln, x.cl, ex)
				}
			}
			if (li.empty) return NoValue.INSTANCE
			Value first = li.get(0)
			if (li.size() == 1)
				first
			else if (first instanceof FunctionValue)
				((FunctionValue) first).call(li.drop(1).toArray() as Value[])
			else
				throw new ArgumentException("Tried to call a non-function value at ${content[0].ln}")
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
					NoValue.INSTANCE
				}
			else
				throw new IllegalStateException("Macros exist but no macros on top?? ${content[0].ln}")
		}
	}

	static void main(args) {
		println evaluate(defaultScope.child(), new Parser(filename: 'test').parse(new File('old/lang/test.lip').text))
	}
}
