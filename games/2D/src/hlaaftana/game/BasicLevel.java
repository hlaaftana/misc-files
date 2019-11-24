package hlaaftana.game;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BasicLevel extends Level {
	private List<LevelObject> objects = new ArrayList<>();

	public List<LevelObject> getObjects() {
		return objects;
	}

	public void setObjects(List<LevelObject> objects) {
		this.objects = objects;
	}

	public void addObject(LevelObject object) {
		objects.add(object);
	}

	@Override
	public void feed(Game game, Graphics2D g) {
		BasicLevelPanel panel = (BasicLevelPanel) game.getPane();
		if (panel.isTicking()) tick(game, g);
		else if (panel.canInitialize()) initialize(game, g);
	}

	@Override
	public BasicLevelPanel createPanel(Game game) {
		return new BasicLevelPanel(game, this);
	}

	@SuppressWarnings("unchecked")
	public void initialize(Game game, Graphics2D g) {
		BasicLevelPanel panel = (BasicLevelPanel) game.getPane();
		panel.setCanInitialize(false);
		for (LevelObject o : getObjects()) o.getListener().initialize(o, game, g);
		if (panel.isStartTickingWhenInitialize()) startTicking(panel);
	}

	public void startTicking(BasicLevelPanel panel) {
		Timer t = new Timer(true);
		panel.setTickTimer(t);
		panel.setTicking(true);
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				panel.repaint();
			}
		}, 0, 16);
	}

	public void stopTicking(BasicLevelPanel panel, boolean canInitialize) {
		panel.getTickTimer().cancel();
		panel.setTickTimer(null);
		panel.setTicking(false);
		panel.setCanInitialize(canInitialize);
	}

	public void stopTicking(BasicLevelPanel panel) {
		stopTicking(panel, false);
	}

	@SuppressWarnings("unchecked")
	public void tick(Game game, Graphics2D g) {
		for (LevelObject o : getObjects()) o.getListener().tick(o, game, g);
	}
}
