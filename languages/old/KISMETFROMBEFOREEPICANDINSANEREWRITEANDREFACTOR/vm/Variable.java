package hlaaftana.oldbutnotvery.kismet.vm;

public interface Variable {
	default String getName() { return null; }
	IKismetObject getValue();
	void setValue(IKismetObject newValue);
}
