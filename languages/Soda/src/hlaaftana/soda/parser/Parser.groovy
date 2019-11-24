package hlaaftana.soda.parser

import hlaaftana.soda.SodaFile

class Parser {
	SodaFile file

	Parser(SodaFile file) {
		this.file = file
	}

	def init() {}
	def loadSources() {}
	def finishUp() {}
	def postJava() {}
}
