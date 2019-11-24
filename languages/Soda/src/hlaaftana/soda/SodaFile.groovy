package hlaaftana.soda

import groovy.transform.CompileStatic
import hlaaftana.soda.constants.ClassConstant
import hlaaftana.soda.constants.Constant
import hlaaftana.soda.constants.PackageConstant
import hlaaftana.soda.source.ClassSource

import java.nio.file.Path

@CompileStatic
class SodaFile {
	SodaCompiler compiler
	List<Declaration> declarations

	Path file

	SodaFile(SodaCompiler compiler, Path file) {
		this.compiler = compiler
		this.file = file
	}
}
