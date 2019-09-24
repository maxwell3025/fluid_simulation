package particlesource;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vectors.Point2D;

public class ParticleMode extends JPanel
		implements Runnable, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020477412984598531L;
	final static float blurriness = 1f;
	JFrame frame;
	JFrame settingsFrame;
	BufferedImage screen;
	BufferedImage comblayers;
	BufferedImage fscreen;
	Graphics2D graphics;
	Graphics2D copier;
	Graphics2D layerer;
	int screenwidth;
	int screenheight;
	int FluidParticlecount;
	int fps = 0;
	int threadnum = 0;
	int[] wait = new int[1000];
	int timemilis;
	boolean[] ispressed = new boolean[500];
	boolean[] topress = new boolean[500];
	static BufferedImage[] images;
	Point2D prevMouse = Point2D.Origin();
	Point2D Mouse = Point2D.Origin();
	boolean[] isheld = new boolean[4];
	ArrayList<FluidParticle> FluidParticles = new ArrayList<FluidParticle>();
	Point2D Camera = Point2D.Origin();
	Point2D CorrectedMouse;
	double speed = 1;
	double scale = 1;
	JSlider speedslider = new JSlider(JSlider.VERTICAL, 0, 200, 100);
	JSlider sizeslider = new JSlider(JSlider.VERTICAL, 0, 200, 100);

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ParticleMode a = new ParticleMode(1080, 720);

	}

	public ParticleMode(int width, int height) {
		loadimages();
		screenwidth = width;
		screenheight = height;
		frame = new JFrame();
		settingsFrame = new JFrame();
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
		createSettings();
		new Thread(this).start();
		new Thread(this).start();
		new Thread(this).start();
		frame.setVisible(true);
		settingsFrame.setVisible(true);
	}

	private void createSettings() {
		settingsFrame.setLayout(new GridBagLayout());
		JLabel speed = new JLabel("speed");
		JLabel size = new JLabel("size");
		speed.setPreferredSize(new Dimension(160, 200));
		speed.setPreferredSize(new Dimension(160, 200));
		settingsFrame.add(speed);
		speedslider.addChangeListener(this);
		speedslider.setMajorTickSpacing(10);
		speedslider.setMinorTickSpacing(1);
		speedslider.setPaintTicks(true);
		speedslider.setPaintLabels(true);
		speedslider.setPreferredSize(new Dimension(160, 720));
		settingsFrame.add(speedslider);
		settingsFrame.add(size);
		sizeslider.addChangeListener(this);
		sizeslider.setMajorTickSpacing(10);
		sizeslider.setMinorTickSpacing(1);
		sizeslider.setPaintTicks(true);
		sizeslider.setPaintLabels(true);
		sizeslider.setPreferredSize(new Dimension(160, 720));
		settingsFrame.add(sizeslider);
		settingsFrame.pack();
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

	}

	protected void maingraphics() {
		for (FluidParticle a : FluidParticles) {
			Point2D pos = Point2D.add(a.pos, Camera.scale(-1));
			Color col = Color.getHSBColor((float) (a.pressure*0.001), (float) (a.density * 0.125), 1);
			graphics.setColor(Color.WHITE);
			//graphics.drawLine((int) pos.x, (int) pos.y, (int) (pos.x + a.vel.x * 0.0001),
			//		(int) (pos.y + a.vel.y * 0.0001));
			graphics.setColor(col);
			if((int) pos.x>1&&(int) pos.x<screenwidth-1&&(int) pos.y>1&&(int) pos.y<screenheight-1){
				screen.setRGB((int) pos.x , (int) pos.y , col.getRGB());
				screen.setRGB((int) pos.x-1 , (int) pos.y , col.getRGB());
				screen.setRGB((int) pos.x , (int) pos.y-1 , col.getRGB());
				screen.setRGB((int) pos.x+1 , (int) pos.y , col.getRGB());
				screen.setRGB((int) pos.x , (int) pos.y+1 , col.getRGB());
			}
		}
	}

	protected void contentupdate() {
		physicsupdate();
		inputupdate();
		Arrays.fill(topress, false);
	}

	private void physicsupdate() {
		// FluidParticles.get(0).pos = Mouse;
		// FluidParticles.get(0).vel = Point2D.add(Mouse,
		// prevMouse.scale(-1)).scale(10000);
		// FluidParticles.get(0).vel = FluidParticles.get(0).vel.scale(0.99);
		ArrayList<FluidParticle> FluidParticlebuf = new ArrayList<FluidParticle>();
		for (FluidParticle a : FluidParticles) {
			FluidParticlebuf.add(a);
		}

		for (int i = 0; i < FluidParticles.size(); i++) {
			FluidParticle a = FluidParticles.get(i);
			FluidParticle bufa = FluidParticlebuf.get(i);
			for (FluidParticle b : FluidParticles) {
				Point2D positionchange = Point2D.Origin();
				if (a != b && Math.abs(a.pos.x - b.pos.x) + Math.abs(a.pos.y - b.pos.y) < 1024) {
					Point2D dif = Point2D.add(a.pos, b.pos.scale(-1));
					double dist = dif.dist() * scale;
					double effect = Math.min(10 / (dist * dist), 1);
					Point2D newposition = Point2D.add(a.vel.scale(1 - effect), b.vel.scale(effect));
					positionchange = Point2D.add(Point2D.add(newposition, a.vel.scale(-1)), positionchange);
					bufa.vel = Point2D.add(bufa.vel, positionchange.scale(speed));
				}
			}
		}
		for (FluidParticle a : FluidParticles) {
			double densitybuf = 0;
			for (FluidParticle b : FluidParticles) {
				if (a != b && Math.abs(a.pos.x - b.pos.x) + Math.abs(a.pos.y - b.pos.y) < 256) {
					Point2D dif = Point2D.add(a.pos, b.pos.scale(-1));
					double dist = dif.dist() / 64 * scale;
					a.vel = Point2D.add(a.vel, dif.scale(speed / (dist * dist * dist)));
					densitybuf += 1 / (dist * dist);
				}
			}
			a.pressure = densitybuf;
		}
		for (FluidParticle a : FluidParticles) {
			//double friction = 0;
			//friction += 128 * speed / ((screenheight - a.pos.y) * scale * (screenheight - a.pos.y) * scale);
			//friction += 128 * speed / ((screenwidth - a.pos.x) * scale * (screenwidth - a.pos.x) * scale);
			//friction += 128 * speed / ((a.pos.x) * scale * (a.pos.x) * scale);
			//friction = Math.min(friction, 1);
			//a.vel = a.vel.scale(1 - friction);
			a.vel.y -= 65536 * 64 * speed / ((screenheight - a.pos.y) * scale * (screenheight - a.pos.y) * scale);
			a.vel.x -= 65536 * 64 * speed / ((screenwidth - a.pos.x) * scale * (screenwidth - a.pos.x) * scale);
			a.vel.x += 65536 * 64 * speed / ((a.pos.x) * scale * (a.pos.x) * scale);
			a.update(0.0001 * speed);
			a.vel.y += 128 * a.density * speed * scale;
		}
		for (FluidParticle a : FluidParticles) {
			if (a.pos.x < 0) {
				a.vel.x *= 0;
				a.pos.x = Math.random() * 10;
			}
			if (a.pos.x > screenwidth) {
				a.vel.x *= 0;
				a.pos.x = screenwidth - Math.random() * 10;
			}
			if (a.pos.y > screenheight) {
				a.vel.y *= 0;
				a.pos.y = screenheight - Math.random() * 10;
			}
		}
	}

	private void inputupdate() {
		Point2D CorrectedMouse = Point2D.add(Mouse, Camera);
		if (topress[KeyEvent.VK_SPACE]) {
			final int xarr = 32;
			final int yarr = 16;
			for (int x = 1; x < xarr; x++) {
				for (int y = 1; y < yarr; y++) {
					double density = 0.1;
					if (y > yarr / 2) {
						density = 8;
					}
					Point2D pos = new Point2D((double) (screenwidth * x) / xarr, (double) (screenheight * y) / yarr);
					Point2D vel = new Point2D(0, 0);
					FluidParticles.add(new FluidParticle(pos, vel, density));
				}
			}
		}
		if (topress[KeyEvent.VK_B]) {
			for (int x = -7; x < 8; x++) {
				for (int y = -7; y < 8; y++) {
					Point2D pos = Point2D.add(CorrectedMouse, new Point2D(x, y).scale(0.1));

					FluidParticles.add(new FluidParticle(pos, Point2D.Origin(), 0.1));
				}
			}
		}
		if (topress[KeyEvent.VK_S]) {
			for (FluidParticle f : FluidParticles) {
				f.vel = Point2D.Origin();
			}
		}
		for (FluidParticle a : FluidParticles) {
			Point2D dif = Point2D.add(a.pos.scale(-1), CorrectedMouse);
			double dist = dif.dist() / 128;
			if (isheld[1] && dist < 1) {
				a.vel = Point2D.add(a.vel, dif.scale(64 / (dist)));
			}
			if (isheld[3]) {
				a.vel = Point2D.add(a.vel, dif.scale(-4 / (dist * dist * dist)));
			}
			if (isheld[2] && dist < 0.25) {
				a.vel.y -= 2048;
			}
		}
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
				for (int i = 0; i < 1000; i++) {
					wait[i]++;
				}
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
		prevMouse = Mouse;
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
		Camera.y += e.getWheelRotation() * 10;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		speed = speedslider.getValue() / 100.0;
		scale = sizeslider.getValue() / 100.0;
	}
}