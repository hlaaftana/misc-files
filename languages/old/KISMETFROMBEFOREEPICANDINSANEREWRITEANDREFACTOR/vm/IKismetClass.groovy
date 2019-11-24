package hlaaftana.oldbutnotvery.kismet.vm

import groovy.transform.CompileStatic

@CompileStatic
interface IKismetClass<T extends IKismetObject> {
	boolean isInstance(IKismetObject object)
	String getName()
	T cast(IKismetObject object)
}