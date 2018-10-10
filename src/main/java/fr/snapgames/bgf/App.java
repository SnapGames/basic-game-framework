package fr.snapgames.bgf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * a Simple Application as a Basic Game framework.
 * 
 * @author Frédéric Delorme.
 * @year 2018
 * @see https://github.com/snapgames/basic-game-framwork/wiki
 */
public class App extends JPanel implements Runnable, KeyListener {

	private static final long serialVersionUID = 2924281870738631982L;

	private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

	/**
	 * default path to store image captures.
	 */
	private static String path = System.getProperty("user.home");

	/**
	 * Game display size and scale.
	 */
	public static int WIDTH = 320;
	public static int HEIGHT = 240;
	public static float SCALE = 2;

	/**
	 * title of the application;
	 */
	private String title = "NoName";

	/**
	 * internal tread.
	 */
	private Thread thread = null;

	/**
	 * internal flags.
	 */
	private boolean exit = false;
	private boolean pause = false;
	private int debug = 0;
	private Font dbgFont;

	/**
	 * Rendering pipeline
	 */
	private BufferedImage buffer;
	private Graphics2D g;
	private Rectangle viewport;

	private long FPS = 60;
	private long timeFrame = (long) (1000 / FPS);
	private long realFPS = 0;

	/**
	 * Translated Messages
	 */
	private ResourceBundle msg = ResourceBundle.getBundle("messages");

	/**
	 * Key processing arrays.
	 */
	private boolean[] keys = new boolean[65536];
	private boolean[] prevKeys = new boolean[65536];
	private Queue<KeyEvent> keyQueue = new ConcurrentLinkedQueue<KeyEvent>();

	/**
	 * GameObject list managed by the game.
	 */
	private Map<String, GameObject> objects = new ConcurrentHashMap<String, GameObject>(50);
	/**
	 * List of object to be rendered.
	 */
	private List<GameObject> renderingList = new ArrayList<GameObject>();

	/**
	 * Craet a new App with <code>title</code> as main title.
	 * 
	 * @param title the title of this app.
	 */
	public App(String title) {
		super();
		this.title = title;
		this.addKeyListener(this);

		this.thread = new Thread(this);

	}

	/**
	 * Return the title for this app.
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Initialize rendering pipeline and other stuff.
	 */
	public void initialize() {
		buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) buffer.getGraphics();
		viewport = new Rectangle(WIDTH, HEIGHT);

		dbgFont = g.getFont().deriveFont(9.0f);
		this.addKeyListener(this);

		GameObject player = GameObject.builder("player")
				.setSize(24, 24)
				.setPosition(0, 0)
				.setColor(Color.GREEN)
				.setVelocity(0.2f, 0.2f)
				.setLayer(2)
				.setPriority(1);

		add(player);

		for (int i = 0; i < 10; i++) {
			GameObject enemy = GameObject.builder("enemy_" + i)
					.setSize(16, 16)
					.setPosition((int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT))
					.setColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()))
					.setVelocity(0.1f, -0.12f)
					.setLayer(1)
					.setPriority(i);

			add(enemy);
		}

	}

	/**
	 * The execution entry point for this Runnable thread.
	 */
	public void run() {
		long elapsed = 0;
		long current = System.currentTimeMillis();
		long previous = current;

		long cumulation = 0, frames = 0;
		initialize();

		while (!exit) {
			current = System.currentTimeMillis();
			if (!pause) {
				update(elapsed);
			}
			clearRenderBuffer(g);
			render(g);
			drawToScreen();
			elapsed = System.currentTimeMillis() - previous;
			cumulation += elapsed;
			frames++;
			if (cumulation > 1000) {
				cumulation = 0;
				realFPS = frames;
				frames = 0;
			}
			if (elapsed < timeFrame) {
				try {
					Thread.sleep(timeFrame - elapsed);
				} catch (InterruptedException ie) {
					System.err.printf("unable to wait some millis: %s", ie.getMessage());
				}
			}
			previous = current;
		}
	}

	/**
	 * Update the game mechanism.
	 * 
	 * @param elapsed
	 */
	private void update(long elapsed) {
		for (Entry<String, GameObject> entry : objects.entrySet()) {
			entry.getValue().update(elapsed);
			constrains(entry.getValue());
		}
	}

	/**
	 * Contained object to App viewport display.
	 * 
	 * @param o
	 */
	private void constrains(GameObject o) {
		if (!this.viewport.contains(o.boundingBox)) {
			if (o.x + o.width > viewport.width || o.x < 0) {
				o.dx = -o.dx * o.friction * o.elasticity;
			}
			if (o.y + o.height > viewport.height || o.y < 0) {
				o.dy = -o.dy * o.friction * o.elasticity;
			}
			if (Math.abs(o.dx) < 0.01f) {
				o.dx = 0.0f;
			}
			if (Math.abs(o.dy) < 0.01f) {
				o.dy = 0.0f;
			}

		}
	}

	/**
	 * clear the graphic buffer.
	 * 
	 * @param g
	 */
	private void clearRenderBuffer(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
	}

	/**
	 * Render the game screen.
	 * 
	 * @param g
	 */
	private void render(Graphics2D g) {

		// prepare pipeline antialiasing.
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// render anything game ?
		for (GameObject o : renderingList) {
			o.render(g);
			if (debug > 0) {
				renderObjectDebugInfo(g, o);
			}
		}

		// render pause status
		if (pause) {
			String pauseLabel = getLabel("app.pause");
			g.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
			g.fillRect(0, HEIGHT / 2, WIDTH, 32);
			g.drawString(pauseLabel, (WIDTH - g.getFontMetrics().stringWidth(pauseLabel)) / 2,
					(HEIGHT - g.getFontMetrics().getHeight()) / 2);
		}

		// render debug information
		drawDebugInformation(g);
	}

	private void renderObjectDebugInfo(Graphics2D g, GameObject o) {
		g.setFont(dbgFont);
		g.setColor(new Color(0.1f,0.1f,0.1f,0.80f));
		g.fillRect(o.x+o.width+2, o.y, 80, 60);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(o.x+o.width+2, o.y, 80, 60);
		g.setColor(Color.GREEN);
		g.drawString(String.format("name:%s", o.name), o.x + o.width + 4, o.y+(12*1));
		g.drawString(String.format("pos:%03d,%03d", o.x, o.y), o.x + o.width + 4, o.y+(12*2));
		g.drawString(String.format("size:%03d,%03d", o.width, o.height), o.x + o.width + 4, o.y + (12*3));
		g.drawString(String.format("vel:%03.2f,%03.2f", o.dx, o.dy), o.x + o.width + 4, o.y + (12*4));
		g.drawString(String.format("L/P:%d/%d", o.layer, o.priority), o.x + o.width + 4, o.y + (12*5));
	}

	/**
	 * Draw debug information.
	 * 
	 * @param g
	 */
	private void drawDebugInformation(Graphics2D g) {
		g.setFont(dbgFont);
		String debugString = String.format("dbg:%s | FPS:%d | Objects:%d | Rendered:%d",
				(debug == 0 ? "off" : "" + debug), realFPS, objects.size(), renderingList.size());
		// int dbgStringWidth = g.getFontMetrics().stringWidth(debugString);
		int dbgStringHeight = g.getFontMetrics().getHeight();
		g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.8f));
		g.fillRect(0, HEIGHT - 32, WIDTH, 32);
		g.setColor(Color.ORANGE);
		g.drawString(debugString, 4, HEIGHT - 32 + dbgStringHeight);
	}

	/**
	 * Draw graphic Buffer to screen.
	 */
	private void drawToScreen() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(buffer, 0, 0, (int)(WIDTH * SCALE), (int)(HEIGHT * SCALE), null);
		g2.dispose();
	}

	/**
	 * Retrieve the label from messages.properties file correspondong to
	 * <code>key</code> value.
	 * 
	 * @param key the key of the label from messages.properties.
	 * @return the value corresponding to the key.
	 */
	public String getLabel(String key) {
		assert (key != null);
		assert (!key.equals(""));
		assert (msg != null);
		return msg.getString(key);
	}

	/**
	 * Process key pressed event.
	 * 
	 * @param e
	 */
	public void keyPressed(KeyEvent e) {
		prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
		keys[e.getKeyCode()] = true;
		logger.fine(e.getKeyCode() + " has been pressed");
	}

	/**
	 * process the key released event.
	 * 
	 * @param e
	 */
	public void keyReleased(KeyEvent e) {
		prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
		keys[e.getKeyCode()] = false;
		logger.fine(e.getKeyCode() + " has been released");

		/**
		 * process the exit request.
		 */
		if (keys[KeyEvent.VK_ESCAPE]) {
			this.exit = !exit;
			logger.fine("Request exiting");
		}
		/**
		 * process the pause request.
		 */
		if (keys[KeyEvent.VK_PAUSE] || keys[KeyEvent.VK_P]) {
			this.pause = !pause;
			logger.fine(String.format("Pause reuqest %b", this.pause));
		}
		
		/**
		 * Write a screenshot to User home folder.
		 */
		if(keys[KeyEvent.VK_S]) {
			pause=true;
			screenshot(buffer);
			pause=false;
		}
		
	}

	/**
	 * process the key typed event.
	 * 
	 * @param e
	 */
	public void keyTyped(KeyEvent e) {
		keyQueue.add(e);
	}

	/**
	 * retrieve status of a specific key.
	 * 
	 * @param keyCode
	 * @return
	 */
	public boolean getKey(int keyCode) {
		return keys[keyCode];
	}

	/**
	 * get the last event from the KeyEvent queue.
	 * 
	 * @return KeyEvent
	 */
	public KeyEvent getKey() {
		return keyQueue.poll();
	}

	/**
	 * Add a GameObject to the game.
	 * 
	 * @param go
	 */
	public void add(GameObject go) {
		objects.put(go.name, go);
		renderingList.add(go);
		Collections.sort(renderingList, new Comparator<GameObject>() {
			public int compare(GameObject o1, GameObject o2) {
				return (o1.layer < o2.layer ? -1 : (o1.priority < o2.priority ? -1 : 1));
			}
		});
	}

	/**
	 * remove GameObject from management.
	 * 
	 * @param go
	 */
	public void remove(GameObject go) {
		objects.remove(go.name);
		renderingList.remove(go);
	}

	/**
	 * Take a screenshot from the image to the default `user.dir`.
	 * 
	 * @param image image to be saved to disk.
	 */
	public static void screenshot(BufferedImage image) {
		try {
			java.io.File out = new java.io.File(path + File.separator + "screenshot " + System.nanoTime() + ".jpg");
			javax.imageio.ImageIO.write(image.getSubimage(0, 0, App.WIDTH, App.HEIGHT), "JPG", out);
		} catch (Exception e) {
			System.err.println("Unable to write screenshot to user.dir: " + path);
		}
	}

	private void parseArgs(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.contains("=")) {
					String[] argSplit = arg.split("=");
					switch (argSplit[0].toLowerCase()) {
					case "w":
					case "width":
						WIDTH = Integer.parseInt(argSplit[1]);
						break;
					case "h":
					case "height":
						HEIGHT = Integer.parseInt(argSplit[1]);
						break;
					case "d":
					case "debug":
						debug = Integer.parseInt(argSplit[1]);
						break;
					case "s":
					case "scale":
						SCALE = Float.parseFloat(argSplit[1]);
						break;
					}
				}
			}
		}
	}

	/**
	 * App execution EntryPoint.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		App app = new App("MyApp");

		app.parseArgs(args);
		Dimension dim = new Dimension(
				(int)(App.WIDTH * App.SCALE), 
				(int)(App.HEIGHT * App.SCALE));

		JFrame frame = new JFrame(app.getTitle());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(app);
		frame.setLayout(new BorderLayout());

		frame.setSize(dim);
		frame.setPreferredSize(dim);
		frame.setMaximumSize(dim);
		frame.setMinimumSize(dim);
		frame.setResizable(false);
		frame.addKeyListener(app);
		frame.pack();

		frame.setVisible(true);

		app.run();
	}

}
