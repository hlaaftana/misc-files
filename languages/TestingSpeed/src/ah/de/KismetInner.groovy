package ah.de

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
class KismetInner {
	static Map<String, KismetObject> defaultContext = [
			Class: KismetClass.meta.object,
			Null: new KismetClass(null, 'Null').object,
			Integer: new KismetClass(BigInteger, 'Integer').object,
			Decimal: new KismetClass(BigDecimal, 'Decimal').object,
			String: new KismetClass(String, 'String').object,
			Boolean: new KismetClass(boolean, 'Boolean').object,
			Int8: new KismetClass(byte, 'Int8').object,
			Int16: new KismetClass(short, 'Int16').object,
			Int32: new KismetClass(int, 'Int32').object,
			Int64: new KismetClass(long, 'Int64').object,
			Dec32: new KismetClass(float, 'Dec32').object,
			Dec64: new KismetClass(double, 'Dec64').object,
			Character: new KismetClass(char, 'Character').object,
			Path: new KismetClass(Path, 'Path').object,
			List: new KismetClass(List, 'List').object,
			Map: new KismetClass(Map, 'Map').object,
			Expression: new KismetClass(Expression, 'Expression').object,
			Function: new KismetClass(Function, 'Function').object,
			Macro: new KismetClass(Macro, 'Macro').object,
			Native: new KismetClass(Object, 'Native').object]

	static {
		Map<String, Object> toConvert = [
			true: true, false: false, null: null,
			class: func { KismetObject... a -> a[0].kclass() },
			class_for_name: funcc { ...a -> KismetClass.instances.groupBy { it.name }[a[0].toString()] },
			// TODO: add converters
			as: func { KismetObject... a -> a[0].as((Class) a[1].inner()) },
			eq: funcc { ...args -> args.inject { a, b -> a == b } },
			is: funcc { ...a -> a[0].is(a[1]) },
			in: funcc { ...a -> a[0] in a[1] },
			not: funcc { ...a -> !(a[0]) },
			and: macr { Expression... exprs ->
				for (it in exprs) if (!it()) return false; true
			},
			or: macr { Expression... exprs ->
				for (it in exprs) if (it()) return true; false
			},
			elvis: macr { Expression... exprs ->
				KismetObject x = Kismet.model(null)
				for (it in exprs) if ((x = it())) return x; x
			},
			xor: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'xor', b } },
			bool: funcc { ...a -> a[0] as boolean },
			bnot: funcc { ...a -> a[0].invokeMethod 'bitwiseNegate', null },
			band: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'and', b } },
			bor: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'or', b } },
			bxor: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'xor', b } },
			lsh: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'leftShift', b } },
			rsh: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'rightShift', b } },
			ursh: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'rightShiftUnsigned', b } },
			lt: funcc { ...args -> args.inject { a, b -> ((int) a.invokeMethod('compareTo', b)) < 0 } },
			gt: funcc { ...args -> args.inject { a, b -> ((int) a.invokeMethod('compareTo', b)) > 0 } },
			lte: funcc { ...args -> args.inject { a, b -> ((int) a.invokeMethod('compareTo', b)) <= 0 } },
			gte: funcc { ...args -> args.inject { a, b -> ((int) a.invokeMethod('compareTo', b)) >= 0 } },
			pos: funcc { ...a -> a[0].invokeMethod 'positive', null },
			neg: funcc { ...a -> a[0].invokeMethod 'negative', null },
			abs: funcc { ...a -> Math.invokeMethod('abs', a[0]) },
			plus: funcc { ...args -> args.sum() },
			minus: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'minus', b } },
			multiply: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'multiply', b } },
			div: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'div', b } },
			mod: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'mod', b } },
			pow: funcc { ...args -> args.inject { a, b -> a.invokeMethod 'power', b } },
			sum: funcc { ...args -> args[0].invokeMethod('sum', null) },
			product: funcc { ...args -> args[0].inject { a, b -> a.invokeMethod 'multiply', b } },
			regex: funcc { ...a -> a[0].invokeMethod 'bitwiseNegate', null },
			set: funcc { ...a -> a[0].invokeMethod('putAt', [a[1], a[2]]) },
			get: funcc { ...a -> a[0].invokeMethod('getAt', a[1]) },
			string: func { KismetObject... a -> a[0].as String },
			int: func { KismetObject... a -> a[0].as BigInteger },
			int8: func { KismetObject... a -> a[0].as byte },
			int16: func { KismetObject... a -> a[0].as short },
			int32: func { KismetObject... a -> a[0].as int },
			int64: func { KismetObject... a -> a[0].as long },
			char: func { KismetObject... a -> a[0].as char },
			decimal: func { KismetObject... a -> a[0].as BigDecimal },
			decimal32: func { KismetObject... a -> a[0].as float },
			decimal64: func { KismetObject... a -> a[0].as double },
			list: funcc { ...args -> args.toList() },
			map: funcc { ...args -> args.toList().collate(2).collectEntries {
				it.size() == 2 ? [(it[0]): it[1]] : [:] } },
			size: funcc { ...a -> a[0].invokeMethod('size', null) },
			keys: funcc { ...a -> a[0].invokeMethod('keySet', null).invokeMethod('toList', null) },
			values: funcc { ...a -> a[0].invokeMethod('values', null) },
			reverse: funcc { ...a -> a[0].invokeMethod('reverse', null) },
			format: funcc { ...args -> String.invokeMethod('format', args) },
			apply_path: macr { Expression... x ->
				((Path) (x[0] instanceof ValueExpression && ((ValueExpression) x[0]).value instanceof Path ?
						((ValueExpression) x[0]).value : x[0]().inner())).apply(x[1]())
			},
			define: macr { Expression... x ->
				x[0].block.context.define(x[0] instanceof ValueExpression && ((ValueExpression) x[0]).value
						instanceof Path ? ((ValueExpression) x[0]).raw : ((ValueExpression) x[0]).value.toString(),
						x[1]())
			},
			change: macr { Expression... x ->
				x[0].block.context.change(x[0] instanceof ValueExpression && ((ValueExpression) x[0]).value
						instanceof Path ? ((ValueExpression) x[0]).raw : ((ValueExpression) x[0]).value.toString(),
						x[1]())
			},
			function: macr { Expression... exprs ->
				Block b = new Block()
				b.expressions = exprs.toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				new KismetFunction(Kismet.model(b))
			},
			macro: macr { Expression... exprs ->
				Block b = new Block()
				b.expressions = exprs.toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				new KismetMacro(Kismet.model(b))
			},
			block: macr { Expression... exprs ->
				Block b = new Block()
				b.expressions = exprs.toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				b
			},
			let: macr { Expression... exprs ->
				def m = (Map<String, KismetObject>) exprs[0]().inner()
				Block b = new Block()
				b.expressions = exprs.drop(1).toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b, m)
				b
			},
			eval: macr { Expression... a -> evaluate(a[0]().toString(), a[0].block) },
			quote: macr { Expression... exprs -> exprs.toList() },
			if: macr { Expression... exprs ->
				Block b = new Block()
				b.expressions = exprs.drop(1).toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				KismetObject j = Kismet.model(null)
				if (exprs[0]()) j = b()
				j
			},
			ifelseif: macr { Expression... ab ->
				for (x in ab.toList().collate(2)) {
					if (x.size() == 1) return x[0]()
					else if (x[0]()) return x[1]()
				}
			},
			ifelse: macr { Expression... x -> x[0]() ? x[1]() : x[2]() },
			while: macr { Expression... exprs ->
				Block b = new Block()
				b.expressions = exprs.drop(1).toList()
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				KismetObject j = Kismet.model(null)
				while (exprs[0]()){
					j = b()
				}
				j
			},
			foreach: macr { Expression... exprs ->
				String n = ((ValueExpression) exprs[0]).value instanceof Path ?
						((ValueExpression) exprs[0]).raw : ((ValueExpression) exprs[0]).value.toString()
				Block b = new Block()
				b.expressions = exprs.toList().drop(2)
				b.block = exprs[0].block
				Block.changeBlock(b.expressions, b)
				b.context = new Context(b)
				KismetObject a = Kismet.model(null)
				for (x in exprs[1]().inner()){
					Block y = b.anonymousClone()
					y.context.directSet(n, Kismet.model(x))
					a = y()
				}
				a
			},
			each: func { KismetObject... args -> args[0].inner().each(args[1].&call) },
			collect: func { KismetObject... args -> args[0].inner().collect(args[1].&call) },
			any: func { KismetObject... args -> args[0].inner().any(args[1].&call) },
			every: func { KismetObject... args -> args[0].inner().every(args[1].&call) },
			find: func { KismetObject... args -> args[0].inner().find(args[1].&call) },
			find_all: func { KismetObject... args -> args[0].inner().findAll(args[1].&call) },
			join: funcc { ...args -> args[0].invokeMethod('join', args[1].toString()) },
			inject: func { KismetObject... args -> args[0].inner().inject(args[1].&call) },
			collate: funcc { ...args -> args[0].invokeMethod('collate', args[1]) },
			pop: funcc { ...args -> args[0].invokeMethod('pop', null) },
			add: funcc { ...args -> args[0].invokeMethod('add', args[1]) },
			add_all: funcc { ...args -> args[0].invokeMethod('addAll', args[1]) },
			remove: funcc { ...args -> args[0].invokeMethod('remove', args[1]) },
			remove_all: funcc { ...args -> args[0].invokeMethod('removeAll', args[1]) },
			put: funcc { ...args -> args[0].invokeMethod('put', [args[1], args[2]]) },
			put_all: funcc { ...args -> args[0].invokeMethod('putAll', args[1]) },
			retain_all: funcc { ...args -> args[0].invokeMethod('retainAll', args[1]) },
			call: macr { Expression... args ->
				def x = args[0]()
				def a = args.toList().drop(1)
				if (!(x.inner() instanceof Macro)) a = a*.evaluate()
				((KismetObject) x).call(a as Object[])
			},
			range: funcc { ...args -> args[0]..args[1] }
		]
		for (e in toConvert) defaultContext.put(e.key, Kismet.model(e.value))
		defaultContext = defaultContext.asImmutable()
	}

	static List<String> separateLines(String code){
		int currentQuote = 0
		boolean escaped = false
		int parentheses = 0
		List<StringBuilder> a = [new StringBuilder()]
		int len = code.codePointCount(0, code.length())
		for (int i = 0; i < len; ++i){
			int x = code.codePointAt(i)
			if (0 == currentQuote)
				if (x == 39 || x == 34) { currentQuote = x; a.last() appendCodePoint x } // ' or "
				else if (x == 40) { ++parentheses; a.last() appendCodePoint x } // (
				else if (x == 41) { --parentheses; a.last() appendCodePoint x } // )
				else if (parentheses == 0)
					if (x == 92) { escaped = true; a.last() appendCodePoint x }
					else if (x == 59 || x == 13 || x == 10) { // ; \r \n
						if (!escaped)
							if (len <= i + 1) ++i
							else {
								if (code.codePointAt(i + 1) in [11, 13]) // \r \n
									++i
								a.add new StringBuilder()
							}
						else {
							int w = 0
							if (len <= i + 1 || (w = code.codePointAt(i + 1))
									== 10 || w == 13) ++i // \r \n
							escaped = false
						}
					} else {
						a.last() appendCodePoint x
						escaped = false
					}
				else a.last() appendCodePoint x
			else {
				if (x == 92) escaped = true
				else if (!escaped && x == currentQuote) currentQuote = 0
				else escaped = false
				a.last() appendCodePoint x
			}
		}
		List<String> liens = []
		for (sb in a) {
			String r = sb.toString()
			if (!r.empty) liens.add(r)
		}
		liens
	}

	static Expression compile(String code, Block source = null){
		def lines = separateLines(code)
		if (lines.size() <= 1)
			parseExpression(source, lines[0])
		else {
			Block b = new Block()
			b.raw = code
			b.context = new Context(b)
			if (null != source) b.block = source
			b.expressions = []
			for (it in lines) b.expressions.add parseExpression(b, it)
			b
		}
	}

	static evaluate(String code, Block source = null){
		compile(code, source)()
	}
	
	static Expression parseExpression(Block block, String code){
		Expression ex
		if (null == code || (code = code.trim()).empty)
			ex = new NoExpression(block)
		else if (code.isBigInteger())
			ex = new ValueExpression(block, code,
					ValueExpression.Type.INTEGER)
		else if (code.isBigDecimal())
			ex = new ValueExpression(block, code,
					ValueExpression.Type.DECIMAL)
		else if (code.length() >= 2 && code[0] in ['\'', '"'] && code[0] == code[-1])
			ex = new ValueExpression(block, code,
				ValueExpression.Type.STRING)
		else if ((code.contains('(') && code.contains(')')) ||
				code.codePoints().toArray().any(Character.&isWhitespace))
			ex = new CallExpression(block, code)
		else4j
			ex = new ValueExpression(block, code,
				ValueExpression.Type.PATH)
		ex.parse()
		ex
	}
	
	static GroovyMacro macr(Closure c){
		new GroovyMacro(c)
	}

	static GroovyFunction func(Closure c){
		new GroovyFunction(false, c)
	}
	
	static GroovyFunction funcc(Closure c) {
		new GroovyFunction(true, c)
	}
}


@CompileStatic
class Block extends Expression {
	List<Expression> expressions
	Context context

	KismetObject evaluate(){
		KismetObject x = Kismet.model null
		for (e in expressions) x = e()
		x
	}
	
	Block anonymousClone(){
		Block b = new Block()
		b.expressions = expressions
		b.block = block
		b.context = new Context(b, new HashMap<>(context.getData()))
		changeBlock(b.expressions, b)
		b
	}
	
	static changeBlock(List<Expression> exprs, Block block){
		for (x in exprs) {
			if (x instanceof CallExpression)
				changeBlock(((CallExpression) x).expressions, block)
			if (x instanceof Block)
				changeBlock(((Block) x).expressions, block)
			x.block = block
		}
	}
}

@CompileStatic
class Context {
	Block block
	Map<String, KismetObject> data = [:]

	Context(Block block = null, Map<String, KismetObject> data = [:]) {
		this.block = block
		setData data
	}

	def getProperty(String name){
		if (getData().containsKey(name)) getData()[name]
		else if (null != block?.block)
			block.block.context.getProperty(name)
		else throw new UndefinedVariableException(name)
	}
	
	void directSet(String name, KismetObject value){
		getData()[name] = value
	}
	
	void define(String name, KismetObject value){
		if (getData().containsKey(name))
			throw new VariableExistsException("Variable $name already exists")
		getData()[name] = value
	}
	
	void change(String name, KismetObject value){
		if (getData().containsKey(name))
			getData()[name] = value
		else if (null != block?.block)
			block.block.context.change(name, value)
		else throw new UndefinedVariableException(name)
	}
	
	def clone(){
		new Context(block, new HashMap<>(getData()))
	}
}

@InheritConstructors class KismetException extends Exception {}
@InheritConstructors class InvalidSyntaxException extends KismetException {}
@InheritConstructors class UndefinedVariableException extends KismetException {}
@InheritConstructors class VariableExistsException extends KismetException {}

@CompileStatic
abstract class Expression {
	Block block
	String raw

	def parse() {}
	abstract KismetObject evaluate()

	KismetObject call(){ evaluate() }
}

@CompileStatic
class NoExpression extends Expression {
	NoExpression(Block block) { this.block = block }

	KismetObject evaluate(){
		Kismet.model(null)
	}
}

@CompileStatic
class ValueExpression extends Expression {
	Type type
	def value

	ValueExpression(Block block, String raw, Type type) {
		this.block = block
		this.raw = raw
		this.type = type
	}
	
	def parse(){
		if (null == raw || raw.empty) value = null
		else if (type == Type.INTEGER) value = new BigInteger(raw)
		else if (type == Type.DECIMAL) value = new BigDecimal(raw)
		else if (type == Type.STRING){
			StringBuilder x = new StringBuilder()
			boolean escaped = false
			int usize = 0
			StringBuilder u = new StringBuilder()
			for (b in raw.trim()[1..-2].codePoints().toArray()) {
				int a = (int) b
				if (!escaped) {
					if (a == 92) escaped = true
					else if (a == raw.codePointAt(0)) throw new InvalidSyntaxException('Unescaped quote\n' + raw)
					else x.appendCodePoint a
				} else {
					if (0 != usize) {
						u.appendCodePoint a
						if (u.length() == usize) {
							escaped = false
							usize = 0
							x.appendCodePoint Integer.parseInt(u.toString(), 16)
							u = new StringBuilder()
						}
						continue
					} else if (a == 117) {
						usize = 4; continue
					} else if (a == 85) {
						usize = 8; continue
					} else if (a == 92 || a == 39 || a == 34 || a == 47) x.appendCodePoint a
					else if (a == 114) x.appendCodePoint 13
					else if (a == 110) x.appendCodePoint 10
					else if (a == 116) x.appendCodePoint 9
					else if (a == 102) x.appendCodePoint 12
					else if (a == 98) x.appendCodePoint 8
					else x.appendCodePoint 92 appendCodePoint a
					escaped = false
				}
			}
			value = x
		} else if (type == Type.PATH) value = Path.parse(raw)
		else throw new InvalidSyntaxException('Unknown callValue type')
	}
	
	KismetObject evaluate(){
		Kismet.model(value instanceof Path ? ((Path) value).apply(block.context) : value)
	}

	enum Type {
		STRING,
		INTEGER,
		DECIMAL,
		PATH
	}
}

@CompileStatic
class CallExpression extends Expression {
	boolean forceCall = false
	List<Expression> expressions

	CallExpression(Block block, String raw) {
		this.block = block
		this.raw = raw
	}

	def parse() {
		expressions = []
		StringBuilder last = new StringBuilder()
		int parens = 0
		boolean escaped = false
		int currentQuote = 0
		for (x in raw.codePoints().toArray()) {
			if (parens == 0)
				if (currentQuote != 0) {
					last.appendCodePoint(x)
					if (!escaped) {
						if (x == 92)
							escaped = true
						else if (x == currentQuote) {
							currentQuote = 0
							expressions.add KismetInner.parseExpression(block, last.toString())
						}
					}
				} else if (x == 39 || x == 34) {
					expressions.add KismetInner.parseExpression(block, last.toString())
					last = new StringBuilder()
					last.appendCodePoint(x)
					currentQuote = x
				} else if (x == 40) {
					expressions.add KismetInner.parseExpression(block, last.toString())
					last = new StringBuilder()
					++parens
				} else if (Character.isWhitespace(x)) {
					if (last.length() != 0) {
						expressions.add KismetInner.parseExpression(block, last.toString())
						last = new StringBuilder()
					}
				} else last.appendCodePoint(x)
			else
				if (currentQuote != 0) {
					if (!escaped) if (x == 92) escaped = true
					else if (x == currentQuote) currentQuote = 0
					last.appendCodePoint(x)
				} else if (x == 40) {
					++parens
					last.appendCodePoint(x)
				} else if (x == 41) {
					--parens
					if (parens == 0) {
						expressions.add KismetInner.compile(last.toString(), block)
						last = new StringBuilder()
					} else last.appendCodePoint(x)
				} else last.appendCodePoint(x)
		}
		if (parens != 0) throw new InvalidSyntaxException('Forgot to close parentheses')
		else expressions.add KismetInner.parseExpression(block, last.toString())
		Iterator iter = expressions.iterator()
		while (iter.hasNext())
			if (iter.next() instanceof NoExpression) { forceCall = true; iter.remove() }
	}
	
	KismetObject evaluate(){
		KismetObject x = expressions[0]()
		if (!forceCall && expressions.size() == 1) return x
		List<Expression> args = expressions.drop(1)
		boolean y = args
		List<KismetObject> argobjs
		if (!(x.inner() instanceof Macro)) argobjs = args*.evaluate()
		else if (y) argobjs = args.collect(Kismet.&model)
		else argobjs = []
		if (y) Kismet.model(x.call(argobjs as KismetObject[]))
		else Kismet.model(x(new KismetObject[0]))
	}
}