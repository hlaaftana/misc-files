package hlaaftana.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Tile implements LevelListener<TileObject> {
	public abstract String getName();
	public abstract boolean isSolid(TileObject obj);
	public abstract int getWidth(TileObject obj);
	public abstract int getHeight(TileObject obj);
	public abstract BufferedImage getImage(TileObject obj);

	public TileObject createTileObject(Level level, int x, int y) {
		return new TileObject(x, y, this, level);
	}

	public void proximityTick(TileObject obj, Game game, Graphics2D g) {
		g.drawImage(getImage(obj), (int) (obj.getX() * game.getZoom()), (int) (obj.getY() * game.getZoom()),
			(int) Math.ceil(getWidth(obj) * game.getZoom()), (int) Math.ceil(getHeight(obj) * game.getZoom()), null);
	}

	@Override
	public void tick(TileObject obj, Game game, Graphics2D g) {
		if (isOnScreen(obj, game)) {
			proximityTick(obj, game, g);
		}
	}

	public static boolean isOnScreen(TileObject obj, Game game) {
		int width = game.getPane().getWidth();
		int height = game.getPane().getHeight();
		int x11 = (int) (obj.getX() * game.getZoom());
		int x12 = (int) ((obj.getX() + obj.getListener().getWidth(obj)) * game.getZoom());
		int x21 = game.getCameraX();
		int x22 = game.getCameraX() + (int) (width * game.getZoom());
		int y11 = (int) (obj.getY() * game.getZoom());
		int y12 = (int) ((obj.getY() + obj.getListener().getHeight(obj)) * game.getZoom());
		int y21 = game.getCameraY();
		int y22 = game.getCameraY() + (int) (height * game.getZoom());
		return !(x12 <= x21 || x22 <= x11 || y12 <= y21 || y22 <= y11);
	}
}
