package hlaaftana.game;

import java.util.*;

public class GameData {
	private static Map<String, Tile> tiles = new HashMap<>();

	public static Collection<Tile> getTiles() {
		return tiles.values();
	}

	public static void addTile(Tile tile) {
		tiles.put(tile.getName(), tile);
	}

	public static void removeTile(String name) {
		tiles.remove(name);
	}

	public static void removeTile(Tile tile) {
		tiles.remove(tile.getName(), tile);
	}
}
