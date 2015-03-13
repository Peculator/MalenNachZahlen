package com.pecdev;

//Import the basic graphics classes.
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.w3c.dom.css.RGBColor;

public class ShowImage extends JPanel {
	BufferedImage image;
	// colors per r,g,b
	static int numColors = 4;
	static int tolerance = 10;
	static int colorDistance = 30;
	static String src = "";
	int[][] colors;
	static boolean greycheck = false;
	boolean showFrame = false;
	static int screenwidth = 1000;
	static int screenheight = 700;

	// Create a constructor method
	public ShowImage(String arg, String arg2) {
		super();
		// Load an image file into our Image object.
		// This file has to be in the same
		// directory as ShowImage.class.
		src = arg;
		numColors = Integer.parseInt(arg2);
		
		loadImage();
		//generateColors();
		//setColors();
		findColors(image);
		filter(image);
		clean(image);
		// blackAndWhite(image);
		//borders(image);

	}

	private void setColors() {
		int[] white = { 0xFF, 0xFF, 0xFF };
		int[] black = { 0x00, 0x00, 0x00 };
		int[] blue = { 0x00, 0x00, 0xFF };
		int[] lightblue = { 0x87, 0xCE, 0xFA };
		int[] red = { 0xFF, 0x00, 0x00 };
		int[] green = { 0x00, 0xFF, 0x00 };
		int[] yellow = { 0xFF, 0xFF, 0x00 };
		int[] darkgrey = { 0x88, 0x88, 0x88 };
		int[] lightgrey = { 0xBB, 0xBB, 0xBB };
		int[] skin = { 239, 208, 207 };

		int[][] tmpcolors = { skin, lightblue, red, green, blue, yellow, black,
				darkgrey, lightgrey, white };
		this.colors = tmpcolors;
		numColors = this.colors.length;
	}

	public void borders() {
		BufferedImage myImage = deepCopy(image);
		System.out.println("Start generating borders...");
		BufferedImage temp = deepCopy(image);

		int grey = 0XFFEEEEEE;
		int white = 0XFFFFFFFF;

		for (int l = 0; l < temp.getHeight() - 1; l++) {
			for (int k = 0; k < temp.getWidth() - 1; k++) {
				// right or down or right-down
				if (myImage.getRGB(k, l) != myImage.getRGB(k + 1, l)
						|| myImage.getRGB(k, l) != myImage.getRGB(k, l + 1)
						|| myImage.getRGB(k, l) != myImage.getRGB(k + 1, l + 1)) {
					temp.setRGB(k, l, grey);

				}

				else {
					temp.setRGB(k, l, white);
				}
			}
		}
		System.out.println("Generated Borders");
		image = temp;

	}

	private void blackAndWhite(BufferedImage myImage) {
		System.out.println("Black & White");
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		image = op.filter(myImage, null);
	}

	private void clean(BufferedImage myImage) {
		System.out.println("Start cleaning up");
		for (int i = 1; i < myImage.getWidth() - 1; i++) {
			for (int j = 1; j < myImage.getHeight() - 1; j++) {
				try {
					// If right and down are the same
					if (myImage.getRGB(i + 1, j) == myImage
							.getRGB(i + 1, j + 1)
							&& myImage.getRGB(i + 1, j) == myImage.getRGB(i,
									j + 1)
							&& myImage.getRGB(i, j + 1) != myImage.getRGB(i, j)) {
						myImage.setRGB(i, j, myImage.getRGB(i, j + 1));
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		System.out.println("Finished cleaning up");
	}

	private void findColors(BufferedImage myImage) {
		System.out.println("Searching for colors...");
		colors = new int[numColors][];

		Map<Integer, Integer> m = new<Integer, Integer[]> HashMap<Integer, Integer>();
		for (int i = 0; i < myImage.getWidth(); i++) {
			for (int j = 0; j < myImage.getHeight(); j++) {
				int rgb = image.getRGB(i, j);
				int[] rgbArr = getRGBArr(rgb);
				// Filter out grays....
				if (!isGray(rgbArr)) {
					Integer counter = (Integer) m.get(rgb);
					if (counter == null)
						counter = 0;
					counter++;
					m.put(rgb, counter);
				}
			}
		}

		LinkedList list = new LinkedList(m.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		int ctrColors = 0;
		int ctrList = 1;

		while (true) {
			Map.Entry me = (Map.Entry) list.get(list.size() - ctrList);
			int[] rgb = getRGBArr((Integer) me.getKey());

			if (keepsDistance(rgb, colors)) {
				colors[ctrColors] = rgb;
				ctrColors++;
			}
			ctrList++;

			if (ctrColors == numColors)
				break;
		}

		System.out.println(colors.length + " colors generated");
		for (int[] col : colors) {
			System.out.println(col[0] + " - " + col[1] + " - " + col[2]);
		}
	}

	private boolean keepsDistance(int[] rgbArr, int[][] colors) {

		for (int i = 0; i < colors.length; i++) {
			if (colors[i] != null) {
				int dis = Math.abs(colors[i][0] - rgbArr[0]);
				dis += Math.abs(colors[i][1] - rgbArr[1]);
				dis += Math.abs(colors[i][2] - rgbArr[2]);

				if (dis < colorDistance) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isGray(int[] rgbArr) {
		if (!greycheck)
			return false;

		int rgDiff = rgbArr[0] - rgbArr[1];
		int rbDiff = rgbArr[0] - rgbArr[2];
		// Filter out black, white and grays...... (tolerance within 10 pixels)

		if (rgDiff > tolerance || rgDiff < -tolerance)
			if (rbDiff > tolerance || rbDiff < -tolerance) {
				return false;
			}
		return true;
	}

	public static int[] getRGBArr(int pixel) {
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;
		return new int[] { red, green, blue };

	}

	private void generateColors() {
		System.out.println("Generating colors...");
		colors = new int[(int) Math.pow(numColors, 3)][];
		int ctr = 0;
		int border = (255 / numColors) * numColors;
		for (int r = 0; r < border; r += 255 / numColors) {
			for (int g = 0; g < border; g += 255 / numColors) {
				for (int b = 0; b < border; b += 255 / numColors) {
					// System.out.println(b);
					colors[ctr] = new int[] { r, g, b };
					ctr++;
				}
			}
		}

		System.out.println(colors.length + " colors generated");
	}

	public void loadImage() {
		try {

			image = ImageIO.read(new File(src));// CAM00450.jpg
												// //234759_1255512677_large.jpg
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// The following methods are instance methods.

	/*
	 * Create a paintComponent() method to override the one in JPanel. This is
	 * where the drawing happens. We don't have to call it in our program, it
	 * gets called automatically whenever the panel needs to be redrawn, like
	 * when it it made visible or moved or whatever.
	 */
	public void paintComponent(Graphics g) {
		

		// Draw our Image object.
		g.drawImage(image, 0, 0, image.getWidth() ,
				image.getHeight(), this);
	}

	public int getIntFromColor(int Red, int Green, int Blue) {
		Red = (Red << 16) & 0x00FF0000; // Shift red 16-bits and mask out other
										// stuff
		Green = (Green << 8) & 0x0000FF00; // Shift Green 8-bits and mask out
											// other stuff
		Blue = Blue & 0x000000FF; // Mask out anything not blue.

		return 0xFF000000 | Red | Green | Blue; // 0xFF000000 for 100% Alpha.
												// Bitwise OR everything
												// together.
	}

	private int getIntFromColor(Color color) {

		return getIntFromColor(color.getRed(), color.getGreen(),
				color.getBlue());
	}

	private void filter(BufferedImage myImage) {
		System.out.println("Filtering...");
		for (int i = 0; i < myImage.getWidth(); i++) {
			for (int k = 0; k < myImage.getHeight(); k++) {
				int col = myImage.getRGB(i, k);
				myImage.setRGB(i, k, findClosestColor(col));
			}
		}
		System.out.println("Filtering finished");
	}

	private int findClosestColor(int col) {
		int closestIndex = 0;
		int distance = Integer.MAX_VALUE;
		for (int i = 0; i < colors.length; i++) {
			int tmpDis = 0;
			tmpDis += Math.abs((col >> 16 & 0xFF) - colors[i][0]);
			tmpDis += Math.abs((col >> 8 & 0xFF) - colors[i][1]);
			tmpDis += Math.abs((col >> 0 & 0xFF) - colors[i][2]);

			if (tmpDis < distance) {
				distance = tmpDis;
				closestIndex = i;
			}
		}

		return getIntFromColor(colors[closestIndex][0],
				colors[closestIndex][1], colors[closestIndex][2]);
	}

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	protected void generate() {

		BufferedImage temp = deepCopy(image);

	}

	public static void main(String arg[]) {
		JFrame frame = new JFrame("ShowImage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(screenwidth, screenheight);

		final ShowImage panel = new ShowImage(arg[0],arg[1]);
		frame.setContentPane(panel);
		frame.setVisible(true);

		panel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg01000) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent arg0) {

				if (arg0.getKeyChar() == 's') {
					File outputfile_gen = new File(panel.src + "-"
							+ panel.numColors);

					try {
						ImageIO.write(panel.image, "jpg", outputfile_gen);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (arg0.getKeyChar() == 'b') {
					panel.borders();
					panel.invalidate();
				}
			}
		});
		panel.setFocusable(true);
		panel.requestFocusInWindow();
	}

}
