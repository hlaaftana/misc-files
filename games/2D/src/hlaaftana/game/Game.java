package hlaaftana.game;

public class Game {
	private LevelPanel pane;
	private int cameraX;
	private int cameraY;
	private Level level;
	private double zoom = 1.0;

	public int convertPanelX(int x) {
		return cameraX + (int) (x / zoom);
	}

	public int convertPanelY(int y) {
		return cameraY + (int) (y / zoom);
	}

	public LevelPanel getPane() {
		return pane;
	}

	public void setPane(LevelPanel pane) {
		this.pane = pane;
	}

	public int getCameraX() {
		return cameraX;
	}

	public void setCameraX(int cameraX) {
		this.cameraX = cameraX;
	}

	public int getCameraY() {
		return cameraY;
	}

	public void setCameraY(int cameraY) {
		this.cameraY = cameraY;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
}
