package hlaaftana.lip

import groovy.transform.CompileStatic

@CompileStatic
interface Declaration {
	String getName()
	LipCallable getCallable()
	int getPrecedence()
	Match match(CallExpression expr)

	static interface Match {
		Declaration getDeclaration()
		CallExpression getExpression()
		boolean isMatched()
		boolean matchesLess(Match other)
	}
}
