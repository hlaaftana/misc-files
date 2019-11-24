package hlaaftana.game;

import groovy.lang.GroovyShell;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;

@SuppressWarnings("unused")
public class LevelBuilder extends JFrame {
	private BasicLevel level;
	private Tile selectedTile;
	private Settings settings = new Settings(this);
	private BasicLevelPanel levelPanel;
	private Object scriptData;
	private JLabel coordinateLabel;
	private MouseMotionListener coordinateListener;
	private Game game;

	public LevelBuilder(Game game, BasicLevel level) {
		this.game = game;
		this.level = level;
		JMenuBar menuBar = new JMenuBar();
		JMenu editMenu = new JMenu("Edit");
		editMenu.setText("Edit");

		menuBar.add(editMenu);

		JMenu buildMenu = new JMenu("Build");
		buildMenu.setText("Build");

		JMenuItem selectTile = new JMenuItem("Select tile");
		selectTile.setText("Select tile");
		selectTile.addActionListener((e) -> {
			TileSelector tileSelector = new TileSelector(LevelBuilder.this, GameData.getTiles().toArray());
			tileSelector.setSize(500, 500);
			tileSelector.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			tileSelector.setVisible(true);
		});

		JMenuItem scriptDialog = new JMenuItem("Run Groovy script");
		scriptDialog.setText("Run Groovy script");
		scriptDialog.addActionListener((e) -> {
			ScriptDialog sd = new ScriptDialog(LevelBuilder.this);
			sd.setSize(400, 400);
			sd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			sd.setVisible(true);
		});

		JMenuItem settingsDialog = new JMenuItem("Settings");
		settingsDialog.setText("Settings");
		settingsDialog.addActionListener((e) -> {
			SettingsDialog sd = new SettingsDialog(LevelBuilder.this);
			sd.setSize(225, 225);
			sd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			sd.setVisible(true);
		});

		buildMenu.add(selectTile);
		buildMenu.add(scriptDialog);
		buildMenu.addSeparator();
		buildMenu.add(settingsDialog);

		menuBar.add(buildMenu);

		JMenu runMenu = new JMenu("Run");
		runMenu.setText("Run");

		JMenuItem startItem = new JMenuItem("Start");
		JMenuItem stopTickingItem = new JMenuItem("Stop ticking");
		JMenuItem continueTickingItem = new JMenuItem("Continue ticking");

		continueTickingItem.setText("Continue ticking");
		continueTickingItem.setEnabled(false);
		continueTickingItem.addActionListener((e) -> {
			level.startTicking(levelPanel);
			continueTickingItem.setEnabled(false);
			stopTickingItem.setEnabled(true);
			startItem.setEnabled(true);
		});

		stopTickingItem.setText("Stop ticking");
		stopTickingItem.setEnabled(false);
		stopTickingItem.addActionListener((e) -> {
			level.stopTicking(levelPanel);
			continueTickingItem.setEnabled(true);
			stopTickingItem.setEnabled(false);
			startItem.setEnabled(true);
		});

		startItem.setText("Start");
		startItem.addActionListener((e) -> {
			levelPanel.setCanInitialize(true);
			repaint();
			stopTickingItem.setEnabled(true);
			continueTickingItem.setEnabled(false);
			startItem.setEnabled(false);
		});

		JMenuItem tickOnceItem = new JMenuItem("Tick once");
		tickOnceItem.setText("Tick once");
		tickOnceItem.addActionListener((e) -> {
			boolean originalTicking = levelPanel.isTicking();
			levelPanel.setTicking(true);
			repaint();
			BufferedImage image = new BufferedImage(levelPanel.getWidth(), levelPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			level.feed(game, g);
			g.dispose();
			try {
				ImageIO.write(image, "png", new File("fuck.png"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			levelPanel.setTicking(originalTicking);
		});

		JMenuItem initOnceItem = new JMenuItem("Initialize without ticking");
		initOnceItem.setText("Initialize without ticking");
		initOnceItem.addActionListener((e) -> {
			boolean originalInit = levelPanel.canInitialize();
			boolean originalTick = levelPanel.isStartTickingWhenInitialize();
			levelPanel.setCanInitialize(true);
			levelPanel.setStartTickingWhenInitialize(false);
			repaint();
			levelPanel.setStartTickingWhenInitialize(originalTick);
			levelPanel.setCanInitialize(originalInit);
		});

		runMenu.add(startItem);
		runMenu.addSeparator();
		runMenu.add(stopTickingItem);
		runMenu.add(continueTickingItem);
		runMenu.addSeparator();
		runMenu.add(tickOnceItem);
		runMenu.add(initOnceItem);
		menuBar.add(runMenu);

		setJMenuBar(menuBar);
		getContentPane().setLayout(new BorderLayout());

		game.setPane(levelPanel = level.createPanel(game));
		levelPanel.setCanInitialize(false);
		coordinateLabel = new JLabel();
		coordinateLabel.setForeground(new Color(0));
		coordinateListener = new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				coordinateLabel.setText(String.format("X: %d, Y: %d",
						game.convertPanelX(e.getX()),
						game.convertPanelY(e.getY())));
				repaint();
				revalidate();
				System.out.println(coordinateLabel.getBounds());
			}
		};
		getContentPane().add(levelPanel);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (settings.clickMode == ClickMode.PLACE && selectedTile != null) {
					System.out.println(e.getComponent());
					Point aae = levelPanel.getLocationOnScreen();
					int x = game.convertPanelX((int) (e.getXOnScreen() - aae.getX()));
					int y = game.convertPanelX((int) (e.getYOnScreen() - aae.getY()));
					if (settings.isGridOn()) {
						x -= x % settings.getGridX();
						y -= y % settings.getGridY();
					}
					level.addObject(selectedTile.createTileObject(level, x, y));
					repaint();
				}
			}
		});
	}

	public LevelBuilder(Game game) {
		this(game, new BasicLevel());
	}
	public LevelBuilder() {
		this(new Game(), null);
		game.setLevel(this.level = new BasicLevel());
	}

	public Tile getSelectedTile() {
		return selectedTile;
	}

	public void setSelectedTile(Tile selectedTile) {
		this.selectedTile = selectedTile;
	}

	public BasicLevel getLevel() {
		return level;
	}

	public void setLevel(BasicLevel level) {
		this.level = level;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public BasicLevelPanel getLevelPanel() {
		return levelPanel;
	}

	public void setLevelPanel(BasicLevelPanel levelPanel) {
		this.levelPanel = levelPanel;
	}

	public Object getScriptData() {
		return scriptData;
	}

	public void setScriptData(Object scriptData) {
		this.scriptData = scriptData;
	}

	public MouseMotionListener getCoordinateListener() {
		return coordinateListener;
	}

	public void setCoordinateListener(MouseMotionListener coordinateListener) {
		this.coordinateListener = coordinateListener;
	}

	public JLabel getCoordinateLabel() {
		return coordinateLabel;
	}

	public void setCoordinateLabel(JLabel coordinateLabel) {
		this.coordinateLabel = coordinateLabel;
	}

	public static void main(String[] args) throws IOException {
		GameData.addTile(new BasicTile("gun", ImageIO.read(new File("resources/original.jpg")), 16, 16));
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}
		LevelBuilder frame = new LevelBuilder();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public enum ClickMode {
		PLACE("Place"),
		SELECT("Select");

		private String text;
		ClickMode(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static class TileSelector extends JFrame implements ListCellRenderer<Tile> {
		private JList list;
		private LevelBuilder levelBuilder;

		public TileSelector(LevelBuilder levelBuilder, Object[] tiles) {
			this.levelBuilder = levelBuilder;
			list = new JList<>(tiles);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setCellRenderer(this);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
			JButton ok = new JButton("OK");
			ok.addActionListener((e) -> {
				if (list.getSelectedValue() != null)
					TileSelector.this.levelBuilder.setSelectedTile((Tile) list.getSelectedValue());
				dispose();
			});
			ok.setText("OK");
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener((e) -> dispose());
			cancel.setText("Cancel");
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
			panel.add(ok);
			panel.add(cancel);
			getContentPane().add(panel, BorderLayout.SOUTH);
		}

		public JList getList() {
			return list;
		}

		public LevelBuilder getLevelBuilder() { return levelBuilder; }

		@Override
		public Component getListCellRendererComponent(JList<? extends Tile> list, Tile value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			BufferedImage image = value.getImage(null);
			BufferedImage newImage = new BufferedImage(value.getWidth(null),
					value.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(image, 0, 0, newImage.getWidth(), newImage.getHeight(), null);
			g.dispose();
			JLabel label = new JLabel(value.getName(), new ImageIcon(newImage), SwingConstants.LEADING);
			label.setOpaque(true);
			if (isSelected) label.setBackground(new Color(0x82B2FF));
			return label;
		}
	}

	public static class ScriptDialog extends JFrame {
		private LevelBuilder builder;
		private JTextArea code = new JTextArea(5, 5);
		private JTextArea console = new JTextArea(5, 5);
		private JButton run = new JButton("Run");
		private GroovyShell shell = new GroovyShell();
		private ConsoleOutputStream consoleOutputStream;

		public ScriptDialog(LevelBuilder builder) {
			this.builder = builder;
			shell.setVariable("builder", this.builder);
			JTabbedPane pane = new JTabbedPane();
			setContentPane(pane);
			Font courier = new Font("Courier New", Font.PLAIN, 16);
			code.setFont(courier);
			code.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						Object ans = shell.evaluate(code.getText());
						shell.setVariable("ans", ans);
					}
				}
			});
			console.setFont(courier);
			console.setBackground(Color.BLACK);
			console.setForeground(Color.WHITE);
			console.setEditable(false);
			run.setText("Run");
			run.addActionListener((e) -> {
				Object ans = shell.evaluate(code.getText());
				shell.setVariable("ans", ans);
			});
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					consoleOutputStream = new ConsoleOutputStream(System.out, System.err, console);
					PrintStream ps = new PrintStream(consoleOutputStream);
					System.setOut(ps);
					System.setErr(ps);
				}

				@Override
				public void windowClosing(WindowEvent e) {
					System.setOut(consoleOutputStream.out);
					System.setErr(consoleOutputStream.err);
				}
			});
			JScrollPane codeScroll = new JScrollPane(code);
			JScrollPane consoleScroll = new JScrollPane(console);
			pane.addTab("Code", codeScroll);
			pane.addTab("Console", consoleScroll);
			pane.addTab("Huge run button", run);
		}

		public LevelBuilder getBuilder() {
			return builder;
		}

		public void setBuilder(LevelBuilder builder) {
			this.builder = builder;
		}

		public JTextArea getCode() {
			return code;
		}

		public void setCode(JTextArea code) {
			this.code = code;
		}

		public JTextArea getConsole() {
			return console;
		}

		public void setConsole(JTextArea console) {
			this.console = console;
		}

		public JButton getRun() {
			return run;
		}

		public void setRun(JButton run) {
			this.run = run;
		}

		public GroovyShell getShell() {
			return shell;
		}

		public void setShell(GroovyShell shell) {
			this.shell = shell;
		}
	}

	public static class ConsoleOutputStream extends OutputStream {
		private PrintStream out;
		private PrintStream err;
		private JTextArea textArea;

		public ConsoleOutputStream(PrintStream out, PrintStream err, JTextArea textArea) {
			this.out = out;
			this.err = err;
			this.textArea = textArea;
		}

		@Override
		public void write(int b) throws IOException {
			textArea.setText(textArea.getText() + String.valueOf(Character.toChars(b)));
			textArea.setCaretPosition(textArea.getDocument().getLength());
			textArea.repaint();
		}
	}

	public static class SettingsDialog extends JFrame {
		private LevelBuilder levelBuilder;
		private final JCheckBox gridOn;
		private final JFormattedTextField gridX;
		private final JFormattedTextField gridY;
		private final JCheckBox showCoordinates;
		private final JComboBox<ClickMode> clickMode;

		public SettingsDialog(LevelBuilder b) {
			levelBuilder = b;
			getContentPane().setLayout(new GridLayout(6, 2, 6, 6));
			gridOn = new JCheckBox("", levelBuilder.getSettings().isGridOn());
			gridOn.setName("Grid on");
			NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
			formatter.setValueClass(Integer.class);
			formatter.setMinimum(0);
			formatter.setAllowsInvalid(false);
			gridX = new JFormattedTextField(formatter);
			gridX.setText("" + levelBuilder.getSettings().getGridX());
			gridX.setName("Grid X");
			gridY = new JFormattedTextField(formatter);
			gridY.setText("" + levelBuilder.getSettings().getGridY());
			gridY.setName("Grid Y");
			gridX.setEnabled(gridOn.isSelected());
			gridY.setEnabled(gridOn.isSelected());
			showCoordinates = new JCheckBox("", levelBuilder.getSettings().isShowCoordinates());
			showCoordinates.setName("Show coordinates");
			gridOn.addItemListener((e) -> {
				gridX.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				gridY.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			});
			clickMode = new JComboBox<>(ClickMode.values());
			clickMode.setName("Click mode");
			clickMode.setSelectedItem(levelBuilder.getSettings().getClickMode());
			JComponent[] array = {gridOn, gridX, gridY, showCoordinates, clickMode};
			for (JComponent anArray : array) {
				JLabel l = new JLabel("  " + anArray.getName() + ": ", JLabel.LEADING);
				getContentPane().add(l);
				l.setLabelFor(anArray);
				getContentPane().add(anArray);
			}
			JButton ok = new JButton("OK");
			ok.addActionListener((e) -> {
				levelBuilder.getSettings().setGridOn(gridOn.isSelected())
						.setShowCoordinates(showCoordinates.isSelected())
						.setClickMode((ClickMode) clickMode.getSelectedItem());
				if (levelBuilder.getSettings().isGridOn())
					levelBuilder.getSettings().setGridX((int) gridX.getValue())
							.setGridY((int) gridY.getValue());
				dispose();
			});
			ok.setText("OK");
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener((e) -> dispose());
			cancel.setText("Cancel");
			getContentPane().add(ok);
			getContentPane().add(cancel);
		}
	}

	public static class Settings {
		private LevelBuilder builder;
		private boolean gridOn;
		private int gridX;
		private int gridY;
		private boolean showCoordinates;
		private ClickMode clickMode = ClickMode.PLACE;

		public Settings(LevelBuilder builder) {
			this.builder = builder;
		}

		public boolean isGridOn() {
			return gridOn;
		}

		public int getGridX() {
			return gridX;
		}

		public int getGridY() {
			return gridY;
		}

		public boolean isShowCoordinates() {
			return showCoordinates;
		}

		public Settings setGridOn(boolean gridOn) {
			this.gridOn = gridOn;
			return this;
		}

		public Settings setGridX(int gridX) {
			this.gridX = gridX;
			return this;
		}

		public Settings setGridY(int gridY) {
			this.gridY = gridY;
			return this;
		}

		public Settings setShowCoordinates(boolean showCoordinates) {
			if (this.showCoordinates != showCoordinates) {
				if (showCoordinates) {
					builder.getLevelPanel().setBounds(0, 0,
							builder.getContentPane().getWidth(), builder.getContentPane().getHeight() - 20);
					builder.getContentPane().add(builder.getCoordinateLabel());
					builder.getCoordinateLabel().setBounds(0, builder.getContentPane().getHeight() - 20,
							builder.getContentPane().getWidth(), 20);
					builder.getLevelPanel().addMouseMotionListener(builder.getCoordinateListener());
				} else {
					builder.getLevelPanel().setBounds(0, 0,
							builder.getContentPane().getWidth(), builder.getContentPane().getHeight());
					builder.getContentPane().remove(builder.getCoordinateLabel());
					builder.getLevelPanel().removeMouseMotionListener(builder.getCoordinateListener());
				}
				builder.repaint();
				builder.revalidate();
			}
			this.showCoordinates = showCoordinates;
			return this;
		}

		public LevelBuilder getBuilder() {
			return builder;
		}

		public void setBuilder(LevelBuilder builder) {
			this.builder = builder;
		}

		public ClickMode getClickMode() {
			return clickMode;
		}

		public Settings setClickMode(ClickMode clickMode) {
			this.clickMode = clickMode;
			return this;
		}
	}
}
