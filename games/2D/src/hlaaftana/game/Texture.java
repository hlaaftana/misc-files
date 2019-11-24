package hlaaftana.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Texture {
	private List<Frame> frames;

	public void fitTo(int width, int height) {
		for (Frame frame : frames) frame.fitTo(width, height);
	}

	public Texture(List<Frame> frames) {
		this.frames = frames;
	}

	public boolean isAnimated() {
		return frames.size() == 1;
	}

	public List<Frame> getFrames() {
		return frames;
	}

	public void setFrames(List<Frame> frames) {
		this.frames = frames;
	}

	public static class Frame {
		private BufferedImage image;
		private long waitTime;

		public Frame(BufferedImage image, long waitTime) {
			this.image = image;
			this.waitTime = waitTime;
		}

		public long getWaitTime() {
			return waitTime;
		}

		public void setWaitTime(long waitTime) {
			this.waitTime = waitTime;
		}

		public BufferedImage getImage() {
			return image;
		}

		public void setImage(BufferedImage image) {
			this.image = image;
		}

		public void fitTo(int width, int height) {
			BufferedImage newImage = new BufferedImage(width, height, image.getType());
			Graphics g = newImage.createGraphics();
			g.drawImage(image, 0, 0, width, height, null);
			g.dispose();
			image = newImage;
		}
	}
}
