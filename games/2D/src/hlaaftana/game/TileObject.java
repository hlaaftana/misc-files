package hlaaftana.game;

public class TileObject implements LevelObject<Tile> {
	private int x;
	private int y;
	private Tile tile;
	private Level level;

	public TileObject(int x, int y, Tile tile, Level level) {
		this.x = x;
		this.y = y;
		this.tile = tile;
		this.level = level;
	}

	public boolean intersectsRect(int x1, int y1, int w1, int h1) {
		int w = tile.getWidth(this);
		int h = tile.getHeight(this);
		return !((x + w) < x1 || (x1 + w1) < x || (y + h) < y1 || (y1 + h1) < y);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	public Tile getListener() { return tile; }

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}
}
