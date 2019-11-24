package hlaaftana.jvm

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

@CompileStatic
class JVMMain {
	static main(String[] args) {
		CliBuilder builder = new CliBuilder()
		builder.option('d', [longOpt: 'output', args: 1, argName: 'file'], 'File to output class to')
		ImportCustomizer imports = new ImportCustomizer()
		imports.addStaticStars('hlaaftana.jvm.AccessFlags')
		CompilerConfiguration cc = new CompilerConfiguration()
		cc.addCompilationCustomizers(imports)
		cc.scriptBaseClass = DelegatingScript.name
		GroovyShell sh = new GroovyShell(new Binding(), cc)
		DelegatingScript script = (DelegatingScript) sh.parse(new File(args[0]))
		script.delegate = new ClassDSL()
		script.run()
	}
}
