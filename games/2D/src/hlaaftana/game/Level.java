package hlaaftana.game;

import java.awt.*;

public abstract class Level {
	public abstract void feed(Game game, Graphics2D g);
	public abstract LevelPanel createPanel(Game game);
}
