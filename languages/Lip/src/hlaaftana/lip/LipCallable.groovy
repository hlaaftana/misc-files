package hlaaftana.lip

import groovy.transform.CompileStatic

@CompileStatic
interface LipCallable {
	Expression transform(Expression expr)
}
