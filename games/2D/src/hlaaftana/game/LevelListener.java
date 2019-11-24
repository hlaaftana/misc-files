package hlaaftana.game;

import java.awt.*;

public interface LevelListener<T extends LevelObject> {
	default void initialize(T obj, Game game, Graphics2D g) {}
	default void tick(T obj, Game game, Graphics2D g) {}
}
