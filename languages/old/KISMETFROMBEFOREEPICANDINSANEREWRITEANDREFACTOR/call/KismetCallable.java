package hlaaftana.oldbutnotvery.kismet.call;

import groovy.transform.CompileStatic;
import hlaaftana.oldbutnotvery.kismet.vm.Context;
import hlaaftana.oldbutnotvery.kismet.vm.IKismetObject;

@CompileStatic
public interface KismetCallable {
	default boolean isPure() { return false; }

	default int getPrecedence() { return 0; }

	IKismetObject call(Context c, Expression... args);
}
