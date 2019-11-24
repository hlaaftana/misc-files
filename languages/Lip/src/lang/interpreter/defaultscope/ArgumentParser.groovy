package lang.interpreter.defaultscope

import groovy.transform.CompileStatic
import lang.ParensToken
import lang.ReferenceToken
import lang.Statement
import lang.Token
import lang.interpreter.Arguments
import lang.interpreter.Arguments.Parameter
import lang.interpreter.Scope

@CompileStatic
class ArgumentParser {
	static Arguments parse(Scope scope, List<Token> tokens) {
		List<Parameter> list = []
		List<Token> isCheck
		for (t in tokens) {
			if (t.text == 'is!') {
				break
			} else if (null == isCheck) {
				if (t.text == 'is') isCheck = []
				else if (t instanceof ParensToken)
					list.add(parseSingle(scope, (ParensToken) t))
				else list.add(new Parameter(name: t.text))
			} else isCheck.add(t)
		}
		def result = new Arguments(list as Parameter[])
		if (null != isCheck) result.check = Checker.parse(scope, isCheck)
		result
	}

	static Parameter parseSingle(Scope scope, ParensToken token) {
		def result = new Parameter()
		List<Token> isCheck
		for (t in token.tokens) {
			if (t.text == 'is!') {
				break
			} else if (null == isCheck) {
				if (t.text == 'varargs')
					result.varargs = true
				else if (t.text == 'is') isCheck = []
				else result.name = t instanceof ReferenceToken ? t.text : scope.eval(new Statement([t])).toString()
			} else isCheck.add(t)
		}
		if (null != isCheck) result.check = Checker.parse(scope, isCheck)
		result
	}
}
