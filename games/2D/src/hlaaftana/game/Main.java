package hlaaftana.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		Game game = new Game();
		BasicLevel level = new BasicLevel();
		LevelPanel panel = level.createPanel(game);
		game.setLevel(level);
		game.setPane(panel);
		BasicTile xdd = new BasicTile("gun", ImageIO.read(new File("resources/original.jpg")), 16, 16);
		JFrame frame = new JFrame();
		int[][] dest = new int[][] {{0, 0}, {0, 16}, {0, 32}, {16, 0}, {16, 16}, {16, 32}, {32, 0}, {32, 16}, {32, 32}};
		for (int[] asshole : dest) {
			level.addObject(new TileObject(asshole[0], asshole[1], xdd, level));
		}
		panel.setLevel(level);
		frame.add(panel);
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
