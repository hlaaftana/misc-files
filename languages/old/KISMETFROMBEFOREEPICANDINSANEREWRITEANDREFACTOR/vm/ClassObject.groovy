package hlaaftana.oldbutnotvery.kismet.vm

import groovy.transform.CompileStatic
import hlaaftana.oldbutnotvery.kismet.exceptions.UnexpectedValueException
import hlaaftana.oldbutnotvery.kismet.Kismet
import hlaaftana.oldbutnotvery.kismet.exceptions.CannotOperateException

@CompileStatic
class ClassObject<T extends IKismetClass> implements IKismetObject<T> {
	T it

	ClassObject(T it) { this.it = it }

	MetaKismetClass kismetClass() { MetaKismetClass.INSTANCE }
	T inner() { it }

	IKismetObject propertyGet(String name) {
		if (name == "name") Kismet.model(it.name)
		else throw new UnexpectedValueException('Unknown property ' + name + ' for class object')
	}

	IKismetObject propertySet(String name, IKismetObject value) {
		throw new CannotOperateException('set property', 'class object')
	}

	IKismetObject getAt(IKismetObject obj) {
		if (obj.toString() == "name") Kismet.model(it)
		else throw new CannotOperateException('subscript get', 'class object')
	}

	IKismetObject putAt(IKismetObject obj, IKismetObject value) {
		throw new CannotOperateException('subscript set', 'class object')
	}

	IKismetObject call(IKismetObject... args) {
		throw new CannotOperateException('call', 'class object')
	}
}