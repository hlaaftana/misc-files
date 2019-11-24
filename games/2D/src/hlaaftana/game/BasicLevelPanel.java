package hlaaftana.game;

import java.util.Timer;

public class BasicLevelPanel extends LevelPanel {
	private boolean ticking;
	private boolean canInitialize = true;
	private boolean startTickingWhenInitialize = true;
	private Timer tickTimer;

	public BasicLevelPanel(Game game, Level level) {
		super(game, level);
	}

	public boolean isTicking() {
		return ticking;
	}

	public void setTicking(boolean ticking) {
		this.ticking = ticking;
	}

	public boolean canInitialize() {
		return canInitialize;
	}

	public void setCanInitialize(boolean canInitialize) {
		this.canInitialize = canInitialize;
	}

	public boolean isStartTickingWhenInitialize() {
		return startTickingWhenInitialize;
	}

	public void setStartTickingWhenInitialize(boolean startTickingWhenInitialize) {
		this.startTickingWhenInitialize = startTickingWhenInitialize;
	}

	public Timer getTickTimer() {
		return tickTimer;
	}

	public void setTickTimer(Timer tickTimer) {
		this.tickTimer = tickTimer;
	}
}
