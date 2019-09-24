package gridsource;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import vectors.Point2D;

public class GridMode extends JPanel
		implements Runnable, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020477412984598531L;
	JFrame frame;
	BufferedImage screen;
	BufferedImage comblayers;
	BufferedImage fscreen;
	Graphics2D graphics;
	Graphics2D copier;
	Graphics2D layerer;
	int screenwidth;
	int screenheight;
	int screenarea;
	int fps = 0;
	int threadnum = 0;
	int[] wait = new int[1000];
	int timemilis;
	boolean[] ispressed = new boolean[500];
	boolean[] topress = new boolean[500];
	static BufferedImage[] images;
	Point2D Mouse = Point2D.Origin();
	boolean[] isheld = new boolean[4];
	Cell[] fluid1;
	Cell[] fluid2;
	Cell[] buf;
	boolean swap;
	double brushsize = 8;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		GridMode a = new GridMode(720, 720);
	}

	public GridMode(int width, int height) {
		loadimages();
		screenwidth = width;
		screenheight = height;
		screenarea = screenwidth * screenheight;
		fluid1 = new Cell[screenarea];
		fluid2 = new Cell[screenarea];
		frame = new JFrame();
		frame.setDefaultCloseOperation(3);
		frame.setResizable(false);
		frame.add(this);
		this.setPreferredSize(new Dimension(screenwidth, screenheight));
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		frame.addKeyListener(this);
		frame.addMouseMotionListener(this);
		frame.addMouseListener(this);
		frame.addMouseWheelListener(this);
		setSize(screenwidth, screenheight);
		frame.pack();
		frame.setLocationRelativeTo(null);
		screen = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		fscreen = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		comblayers = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		graphics = screen.createGraphics();
		copier = fscreen.createGraphics();
		layerer = comblayers.createGraphics();
		init();
		new Thread(this).start();
		new Thread(this).start();
		new Thread(this).start();
		frame.setVisible(true);
	}

	protected void init() {
		for (int i = 0; i < screenarea; i++) {
			fluid1[i] = new Cell(0);
			fluid2[i] = new Cell(0);
		}
	}

	private void loadimages() {
		URL txt = getClass().getClassLoader().getResource("images/meta.txt");
		int imagecount = 0;
		try {
			Scanner in = new Scanner(txt.openStream());
			imagecount = Integer.parseInt(in.nextLine());
			in.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		images = new BufferedImage[imagecount];
		for (int i = 0; i < imagecount; i++) {
			URL image = getClass().getClassLoader().getResource("images/" + i + ".png");
			try {
				images[i] = ImageIO.read(image);
				System.out.println("images/" + i + ".png");
			} catch (IOException e) {
			}
		}

	}

	public synchronized void paint(Graphics g) {
		g.drawImage(fscreen, 0, 0, null);
	}

	protected void graphicsupdate() throws ConcurrentModificationException {
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, screenwidth, screenheight);
		maingraphics();
		copier.drawImage(screen, 0, 0, null);
		copier.drawString(String.valueOf(fps), 0, 20);
	}

	protected void maingraphics() {
		for (int i = 0; i < screenarea; i++) {
			int x = i % screenwidth;
			int y = i / screenwidth;
			double shade = Math.abs(fluid1[i].pres);
			Color col = Color.getHSBColor((float) shade, 1, 1 - (float) shade * 0.0625f);
			screen.setRGB(x, y, col.getRGB());
		}
		graphics.setColor(Color.white);
		graphics.drawOval((int) (Mouse.x - brushsize), (int) (Mouse.y - brushsize), (int) (brushsize * 2),
				(int) (brushsize * 2));

	}

	private Cell get(int x, int y) {
		return fluid2[x + screenwidth * y];
	}

	protected void contentupdate() {
		for (int i = 0; i < screenarea; i++) {
			int x = i % screenwidth;
			int y = i / screenwidth;
			if ((Mouse.x - x) * (Mouse.x - x) + (Mouse.y - y) * (Mouse.y - y) < brushsize*brushsize) {
				if (isheld[1]) {
					fluid1[i].pres = 4;
					fluid2[i].pres = 4;
				}
				if (isheld[3]) {
					fluid1[i].pres = 0;
					fluid2[i].pres = 0;
				}
			}
		}
		for (int i = 0; i < screenarea; i++) {
			int x = i % screenwidth;
			int y = i / screenwidth;
			double preschange = 0;
			if (x != 0) {
				preschange += get(x - 1, y).pres * 0.25;
				preschange -= fluid1[i].pres * 0.25;
			}
			if (y != 0) {
				preschange += get(x, y - 1).pres * 0.25;
				preschange -= fluid1[i].pres * 0.25;
			}
			if (x != screenwidth - 1) {
				preschange += get(x + 1, y).pres * 0.25;
				preschange -= fluid1[i].pres * 0.25;
			}
			if (y != screenheight - 1) {
				preschange += get(x, y + 1).pres * 0.25;
				preschange -= fluid1[i].pres * 0.25;
			}
			fluid1[i].pres += preschange;
		}
		swap();
		Arrays.fill(topress, false);

	}

	protected void swap() {
		buf = fluid1.clone();
		fluid1 = fluid2.clone();
		fluid2 = buf.clone();
		swap = !swap;
	}

	public void run() {
		threadnum++;
		if (threadnum == 1) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				try {
					graphicsupdate();
					repaint();
					for (int i = 0; i < 1000; i++) {
						wait[i]++;
					}
				} catch (ConcurrentModificationException e) {
				}

			}
		}
		if (threadnum == 2) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				contentupdate();
			}
		}
		if (threadnum == 3) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				fps = wait[1];
				for (int i = 1; i < 1000; i++) {
					wait[i - 1] = wait[i];
				}
				wait[999] = 0;
				timemilis++;
			}

		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		ispressed[e.getKeyCode()] = true;
		topress[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		Mouse = new Point2D(e.getX(), e.getY());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		isheld[e.getButton()] = true;
	}

	public void mouseReleased(MouseEvent e) {
		isheld[e.getButton()] = false;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		brushsize-=e.getWheelRotation()*0.5;
	}
}