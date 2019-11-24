package hlaaftana.game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LevelPanel extends JPanel {
	private Game game;
	private Level level;

	public LevelPanel(Game game, Level level) {
		this.game = game;
		this.level = level;
		setBackground(new Color(0));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedImage canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = canvas.createGraphics();
		level.feed(game, g2);
		g2.dispose();
		g.drawImage(canvas, 0, 0, this);
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}
}
