package hlaaftana.karmafields.lang.interpreter

class LangException extends Exception {
	LangException(int ln, int cl, Exception parent) {
		super("Exception thrown at line $ln column $cl", parent)
	}
}
