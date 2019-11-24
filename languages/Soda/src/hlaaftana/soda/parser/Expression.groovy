package hlaaftana.soda.parser

import groovy.transform.CompileStatic

import hlaaftana.soda.SodaFile

@CompileStatic
class Expression {
	Parser parser
	Expression parent
	String content
	List<Expression> indentedExprs

	SodaFile getFile() { parser.file }
}
