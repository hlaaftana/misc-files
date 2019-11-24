package hlaaftana.game;

public interface LevelObject<T extends LevelListener> {
	Level getLevel();
	T getListener();
}
