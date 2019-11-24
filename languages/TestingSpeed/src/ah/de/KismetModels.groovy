package ah.de

import groovy.transform.CompileStatic

@CompileStatic
class KismetModels {
	static Map<Class, KismetObject<KismetClass>> defaultConversions = (Map<Class, KismetObject<KismetClass>>) [
			(Macro)     : 'Macro', (Function): 'Function', (Character): 'Character',
			(Byte)      : 'Int8', (Short): 'Int16', (Integer): 'Int32', (Long): 'Int64',
			(BigInteger): 'Integer', (BigDecimal): 'Decimal', (Float): 'Dec32',
			(Double)    : 'Dec64', (Expression): 'Expression', (String): 'String',
			(Path)      : 'Path', (List): 'List', (Map): 'Map',
			(Boolean)   : 'Boolean', (KismetClass): 'Class'
	].collectEntries { Class k, String v -> [(k): KismetInner.defaultContext[v]] }

	static KismetObject model(Class c){ defaultConversions[c] ?:
		KismetInner.defaultContext.Native }

	static KismetObject model(KismetObject obj){ obj }

	static KismetObject model(Closure c){ model(new GroovyFunction(c)) }

	static KismetObject model(File f){
		model(new Expando(name: f.name) )
	}

	static KismetObject model(obj){
		null == obj ? new KismetObject(null, KismetInner.defaultContext.Null) :
			new KismetObject(obj, (KismetObject<KismetClass>) (defaultConversions.find { k, v -> obj in k }?.value ?:
				KismetInner.defaultContext.Native))
	}
}
