package hlaaftana.game;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BasicTile extends Tile {
	private final String name;
	private Texture texture;
	private int width;
	private int height;
	private boolean solid;

	public BasicTile(String name, BufferedImage image) {
		this.name = name;
		this.texture = new Texture(Collections.singletonList(new Texture.Frame(image, 0)));
	}

	public BasicTile(String name, BufferedImage image, int width, int height) {
		this(name, image);
		texture.fitTo(width, height);
		this.width = width;
		this.height = height;
	}

	public BasicTile(String name, Texture texture) {
		this.name = name;
		if (texture.getFrames().size() < 1) {
			throw new IllegalArgumentException("Can't have less than 1 frame");
		}
		this.texture = texture;
	}

	public BasicTile(String name, Texture image, int width, int height) {
		this(name, image);
		texture.fitTo(width, height);
		this.width = width;
		this.height = height;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSolid(TileObject til) {
		return solid;
	}

	public void setSolid(boolean solid) {
		this.solid = solid;
	}

	@Override
	public int getWidth(TileObject til) {
		return width != 0 ? width : getImage(til).getWidth();
	}

	@Override
	public int getHeight(TileObject til) {
		return height != 0 ? height : getImage(til).getHeight();
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	private Map<TileObject, FrameInfo> frames_ = new HashMap<>();

	@Override
	public BufferedImage getImage(TileObject til) {
		if (getTexture().isAnimated()) {
			if (frames_.containsKey(til)) {
				FrameInfo aa = frames_.get(til);
				if ((System.currentTimeMillis() - aa.startTime) > getTexture().getFrames().get(aa.frameNum).getWaitTime()) {
					aa.frameNum = (aa.frameNum + 1) % getTexture().getFrames().size();
					aa.startTime = System.currentTimeMillis();
				}
			} else {
				FrameInfo aa = new FrameInfo();
				aa.startTime = System.currentTimeMillis();
				frames_.put(til, aa);
			}
			return getTexture().getFrames().get(frames_.get(til).frameNum).getImage();
		} else {
			return getTexture().getFrames().get(0).getImage();
		}
	}

	protected static class FrameInfo {
		protected long startTime;
		protected int frameNum;
	}
}
