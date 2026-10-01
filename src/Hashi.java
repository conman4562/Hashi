import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class Hashi extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
	
	public HashSet<Island> islands;
	public HashSet<Island> islandsCopy;
	
	public ArrayList<Button> buttons;
	
	public ArrayList<Island> unsolvedIslands;
	
	public ArrayList<HashSet<Island>> groups;
	
	public int x;
	public int y;
	public int spacing;
	public int mx;
	public int my;
	public Island currentSelected0;
	public Island currentSelected1;
	public Island currentSelected2;
	boolean darkMode;
	boolean instructions;
	boolean notSolvedMessage;
	boolean solvedMessage;
	boolean solved;
	boolean undoable;
	boolean solvable;
	boolean solvableMessage;
	boolean unSolvableMessage;
	boolean solving;
	boolean concurrentModificationErrorFix;
	double animationSpeed = 0.1;
	static final int ANIMATION_TIME = 1000;
	static Color GOOD_COLOR = new Color(31, 100, 20);
	static Color BAD_COLOR = new Color(105, 11, 34);
	static Color BOARD_BG_COLOR = new Color(222, 222, 222);
	static Color BG_COLOR = new Color(236, 223, 204);
	// caved and used a 2d array. can't see a better way to detect for lines crossing.
	public char[][] board;
	public char[][] boardCopy;
	
	public Hashi(List<String> input) {
		x = 100;
		y = 150;
		spacing = 90;
		currentSelected0 = null;
		currentSelected1 = null;
		currentSelected2 = null;
		buttons = new ArrayList<Button>();
		buttons.add(new Button(300, 125, 200, 50, 20, 40, "Instructions (I)"));
		buttons.add(new Button(300, 195, 200, 50, 20, 40, "Re-Center (R)"));
		buttons.add(new Button(300, 265, 200, 50, 20, 40, "Clear Bridges (C)"));
		buttons.add(new Button(300, 335, 200, 50, 20, 40, "Import (J)"));
		buttons.add(new Button(300, 405, 200, 50, 20, 40, "Export (E)"));
		buttons.add(new Button(300, 475, 200, 50, 20, 40, "Check Solution (V)"));
		buttons.add(new Button(300, 545, 200, 50, 20, 40, "Solve Puzzle (S)"));
		buttons.add(new Button(400, 265, 90, 50, 20, 40, "Undo (Z)"));
		buttons.add(new Button(400, 615, 100, 50, 20, 40, "Slow"));
		buttons.add(new Button(300, 615, 100, 50, 20, 40, "Med"));
		buttons.add(new Button(200, 615, 100, 50, 20, 40, "Fast"));
		buttons.add(new Button(100, 615, 100, 50, 20, 40, "Instant"));
		setFocusable(true);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addMouseListener(this);
		init(input);
		x += board[0].length/2 * spacing;
		y += board.length/2 * spacing;
	}
	
	private void init(List<String> input) {
		if (input.size() == 0) {
			JOptionPane.showMessageDialog(this, "File is empty.");
			return;
		}
		board = new char[input.size()][];
		initBoard(input);
		initIslands(input);
	}
	
	private void initBoard(List<String> input) {
		int maxLength = input.get(0).length();
		for (int i = 1; i < input.size(); i++) {
			if (input.get(i).length() > maxLength) {
				maxLength = input.get(i).length();
			}
		}
		String validChars = "12345678-=#| ";
		for (int row = 0; row < input.size(); row++) {
			board[row] = new char[maxLength];
			for (int col = 0; col < maxLength; col++) {
				if (input.get(row).length() <= col) {
					board[row][col] = ' ';
					continue;
				} else {
					if (validChars.contains("" + input.get(row).charAt(col))) {
						board[row][col] = input.get(row).charAt(col);
					}
				}
			}
		}
		// System.out.println(Arrays.deepToString(board));
	}
	
	private void initIslands(List<String> input) {
		islands = new HashSet<Island>();
		String n = "12345678";
		String v = "#|";
		String h = "-=";
		for (int row = 0; row < input.size(); row++) {
			String line = input.get(row);
			for (int col = 0; col < line.length(); col++) {
				String c = "" + line.charAt(col);
				if (n.contains(c)) {
					Island island = new Island(row, col, Integer.parseInt(c));
					//up
					int tempRow = row - 1;
					int tempCol = col;
					while (tempRow >= 0 && input.get(tempRow).length() > tempCol && v.contains("" + input.get(tempRow).charAt(tempCol))) {
						tempRow = tempRow - 1;
					}
					if (tempRow >= 0 && row - 1 != tempRow && n.contains("" + input.get(tempRow).charAt(tempCol))) {
						Island tempIsland = new Island(tempRow, tempCol, Integer.parseInt("" + input.get(tempRow).charAt(tempCol)));
						if (input.get(tempRow + 1).charAt(tempCol) == '#') {
							island.nb.put(tempIsland, 2);
						} else {
							island.nb.put(tempIsland, 1);
						}
					}
					//down
					tempRow = row + 1;
					tempCol = col;
					while (tempRow < input.size() && input.get(tempRow).length() > tempCol && v.contains("" + input.get(tempRow).charAt(tempCol))) {
						tempRow = tempRow + 1;
					}
					if (tempRow < input.size() && row + 1 != tempRow && n.contains("" + input.get(tempRow).charAt(tempCol))) {
						Island tempIsland = (new Island(tempRow, tempCol, Integer.parseInt("" + input.get(tempRow).charAt(tempCol))));
						if (input.get(tempRow - 1).charAt(tempCol) == '#') {
							island.nb.put(tempIsland, 2);
						} else {
							island.nb.put(tempIsland, 1);
						}
					}
					//left
					tempRow = row;
					tempCol = col - 1;
					while (tempCol >= 0 && h.contains("" + input.get(tempRow).charAt(tempCol))) {
						tempCol = tempCol - 1;
					}
					if (tempCol >= 0 && col - 1 != tempCol && n.contains("" + input.get(tempRow).charAt(tempCol))) {
						Island tempIsland = new Island(tempRow, tempCol, Integer.parseInt("" + input.get(tempRow).charAt(tempCol)));
						if (input.get(tempRow).charAt(tempCol + 1) == '=') {
							island.nb.put(tempIsland, 2);
						} else {
							island.nb.put(tempIsland, 1);
						}
					}
					//right
					tempRow = row;
					tempCol = col + 1;
					while (tempCol < input.get(tempRow).length() && h.contains("" + input.get(tempRow).charAt(tempCol))) {
						tempCol = tempCol + 1;
					}
					if (tempCol < input.get(tempRow).length() && col + 1 != tempCol && n.contains("" + input.get(tempRow).charAt(tempCol))) {
						Island tempIsland = new Island(tempRow, tempCol, Integer.parseInt("" + input.get(tempRow).charAt(tempCol)));
						if (input.get(tempRow).charAt(tempCol - 1) == '=') {
							island.nb.put(tempIsland, 2);
						} else {
							island.nb.put(tempIsland, 1);
						}
					}
					islands.add(island);
				}
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		Graphics2D gNew = (Graphics2D) g.create();
		
		int roundedRectWidth = Math.max(getWidth() - 500, 300);
		int roundedRectHeight = Math.max(getHeight() - 180, 100);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(BOARD_BG_COLOR);
		g2d.fillRoundRect(50, 125, roundedRectWidth, roundedRectHeight, 100, 100);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// gNew.translate(zx, zy);
		// gNew.scale(zoom, zoom);
		// gNew.translate(-zx, -zy);
		gNew.translate(x, y);
		gNew.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// System.out.println(x + ", " + y);
		// lines first so they overlap
		gNew.setColor(Color.BLACK);
		concurrentModificationErrorFix = true;
		for (Island island : islands) {
			
			for (Island tempIsland : island.nb.keySet()) {
				gNew.setStroke(new BasicStroke(5));
				if (island.nb.get(tempIsland) == 2) {
					gNew.drawLine(15 + (island.col - board[0].length/2) * spacing, 15 + (island.row - board.length/2) * spacing, 15 + (tempIsland.col - board[0].length/2) * spacing, 15 + (tempIsland.row - board.length/2) * spacing);
					gNew.drawLine(35 + (island.col - board[0].length/2) * spacing, 35 + (island.row - board.length/2) * spacing, 35 + (tempIsland.col - board[0].length/2) * spacing, 35 + (tempIsland.row - board.length/2) * spacing);
				} else if (island.nb.get(tempIsland) == 1) {
					gNew.drawLine(25 + (island.col - board[0].length/2) * spacing, 25 + (island.row - board.length/2) * spacing, 25 + (tempIsland.col - board[0].length/2) * spacing, 25 + (tempIsland.row - board.length/2) * spacing);
				}
			}
		}
		for (Island island : islands) {
			gNew.setColor(Color.BLACK);
			if (island.isFull()) {
				gNew.setColor(GOOD_COLOR);
			} else if (island.hasTooMany()) {
				gNew.setColor(BAD_COLOR);
			}
			gNew.fillOval((island.col - board[0].length/2) * spacing, (island.row - board.length/2) * spacing, 50, 50);
			gNew.setColor(Color.WHITE);
			gNew.setFont(new Font("Arial", Font.PLAIN, 40));
			gNew.drawString("" + island.value, 14 + (island.col - board[0].length/2) * spacing , 39 + (island.row - board.length/2) * spacing);
			if (!instructions && !solving && (island.equals(currentSelected0) || island.equals(currentSelected2) )) {
				gNew.drawOval((island.col - board[0].length/2) * spacing, (island.row - board.length/2) * spacing, 50, 50);
			}
		}
		concurrentModificationErrorFix = false;
		
		
		// tl
		if (notSolvedMessage) {
			if (groups != null) {
			    // get min/max corners and put box around it
			    for (HashSet<Island> group : groups) {
			    	if (group.size() > islands.size()/2) {
			    		continue;
			    	}
			      Area groupArea = new Area();
			      for (Island is : group) {
			      	int xPos = (is.col - board[0].length/2) * spacing;
			      	int yPos = (is.row - board.length/2) * spacing;
			      	groupArea.add(new Area(new RoundRectangle2D.Double(xPos - 7, yPos - 7, 64, 64, 20, 20)));
			      	Island[] nbs = getValidNeighbors(is);
			      	//n
			      	if (is.nb.keySet().contains(nbs[0]) && is.nb.get(nbs[0]) != 0) {
			      		int dist =  is.row - nbs[0].row;
			      		groupArea.add(new Area(new Rectangle2D.Double(xPos - 7, yPos - spacing * dist + 10, 64, spacing * dist + 10)));
			      	}
			      	//e
			      	if (is.nb.keySet().contains(nbs[1]) && is.nb.get(nbs[1]) != 0) {
			      		int dist = nbs[1].col - is.col;
			      		groupArea.add(new Area(new Rectangle2D.Double(xPos + 32, yPos - 7, spacing * dist, 64)));
			      	}
			      	//s
			      	//w
			      }
			      gNew.setStroke(new BasicStroke(5));
			      gNew.setColor(new Color(18, 52, 88));
			      gNew.draw(groupArea);
			    }
			}
			gNew.setStroke(new BasicStroke(5));
			g2d.setStroke(new BasicStroke(5));
			for (int i = 0; i < unsolvedIslands.size(); i++) {
				gNew.setColor(BAD_COLOR);
				gNew.drawOval((unsolvedIslands.get(i).col - board[0].length/2) * spacing - 5, (unsolvedIslands.get(i).row - board.length/2) * spacing - 5, 60, 60);
			}
			g2d.setColor(Color.WHITE);
			RoundRectangle2D roundedRect = new RoundRectangle2D.Double(80, 150, 335, 60, 10, 10);
			g2d.fill(roundedRect);
			g2d.setColor(Color.BLACK);
			g2d.draw(roundedRect);
			g2d.setColor(BAD_COLOR);
			g2d.setFont(new Font("Arial", Font.BOLD, 50));
			g2d.drawString("NOT SOLVED", 85, 200);
			
		}
		if (solvedMessage && !solving) {
			g2d.setStroke(new BasicStroke(5));
			g2d.setColor(Color.WHITE);
			RoundRectangle2D roundedRect = new RoundRectangle2D.Double(80, 150, 215, 60, 10, 10);
			g2d.fill(roundedRect);
			g2d.setColor(Color.BLACK);
			g2d.draw(roundedRect);
			g2d.setColor(GOOD_COLOR);
			g2d.setFont(new Font("Arial", Font.BOLD, 50));
			g2d.drawString("SOLVED", 85, 200);
		}
		
		if (unSolvableMessage) {
			g2d.setStroke(new BasicStroke(5));
			g2d.setColor(Color.WHITE);
			RoundRectangle2D roundedRect = new RoundRectangle2D.Double(80, 150, 352, 60, 10, 10);
			g2d.fill(roundedRect);
			g2d.setColor(Color.BLACK);
			g2d.draw(roundedRect);
			g2d.setColor(BAD_COLOR);
			g2d.setFont(new Font("Arial", Font.BOLD, 50));
			g2d.drawString("UNSOLVABLE", 85, 200);
		}
		
		// outer 2drect
		Rectangle2D full = new Rectangle2D.Double(-100, -100, 100 + getWidth(), 100 + getHeight());
    Area area = new Area(full);
    RoundRectangle2D roundedRect = new RoundRectangle2D.Double(50, 125, roundedRectWidth, roundedRectHeight, 100, 100);
    area.subtract(new Area(roundedRect));
    g2d.setColor(BG_COLOR);
    g2d.fill(area);
    g2d.setStroke(new BasicStroke(5));
		g2d.setColor(Color.BLACK);
		g2d.drawRoundRect(50, 125, roundedRectWidth, roundedRectHeight, 100, 100);
		// buttons
		for (int i = 0; i < buttons.size(); i++) {
			if (i >= 8 && i <= 11) {
				if (solving) {
					buttons.get(i).drawButton(g);
				}
			} else {
				if (buttons.get(i).message.equals("Undo (Z)")) {
					if (undoable) {
						buttons.get(i).drawButton(g);
					}
				} else {
					buttons.get(i).drawButton(g);
				}
			}
		}
		
		// title
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Times New Roman", Font.BOLD, 100));
		g2d.drawString("Hashi Puzzle", 50, 100);
		repaint();
		
		if (instructions) {
//			int lineSpacing = 20;
//			int currentPlace = 230;
//			int xStart = 300;
//			FontMetrics fm = g2d.getFontMetrics();
//			g2d.setStroke(new BasicStroke(3));
//			g2d.setColor(Color.WHITE);
//			g2d.fillRect(xStart, 200, 800, 700);
//			g2d.setColor(Color.BLACK);
//			g2d.drawRect(xStart, 200, 800, 700);
//			g2d.setFont(new Font("Arial", Font.BOLD, 50));
//			g2d.drawString("Instructions:", xStart + 10, currentPlace += lineSpacing);
//			g2d.setFont(new Font("Arial", Font.BOLD, 20));
//			g2d.drawString("Hashi Rules:", xStart + 10, currentPlace += lineSpacing);
//			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
//			g2d.drawString("-The number on each island tells you how many bridges must be connected to it.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Bridges can only be horizontal or vertical and cannot cross eachother.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Bridges must connect all islands to a single connected network.", xStart + 15, currentPlace += lineSpacing);
//			currentPlace += 10;
//			g2d.setFont(new Font("Arial", Font.BOLD, 20));
//			g2d.drawString("General info:", xStart + 10, currentPlace += lineSpacing);
//			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
//			g2d.drawString("-Connect 2 islands with a bridge by dragging from one to another.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Islands with the correct amount of bridges are highlighted green.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Islands with too many bridges are highlighted red.", xStart + 15, currentPlace += lineSpacing);
//			currentPlace += 10;
//			g2d.setFont(new Font("Arial", Font.BOLD, 20));
//			g2d.drawString("Controls:", xStart + 10, currentPlace += lineSpacing);
//			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
//			g2d.drawString("-Press 'ESC' to close the program.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Drag on the puzzle board to move the puzzle around.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Scroll to change the spacing between islands.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Hotkeys are indicated by the letter next to the button (I-instructions, R-Re-Center...).", xStart + 15, currentPlace += lineSpacing);
//			currentPlace += 10;
//			g2d.setFont(new Font("Arial", Font.BOLD, 20));
//			g2d.drawString("Buttons:", xStart + 10, currentPlace += lineSpacing);
//			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
//			g2d.drawString("-Instructions: Opens and closes this menu.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Re-Center: Puts the puzzle back in the locaiton where it started.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Clear Bridges: Removes all bridges on screen.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("--Undo: Undoes the clear bridges button press. Does not work after next action.", xStart + 30, currentPlace += lineSpacing);
//			g2d.drawString("-Import: Import a Hashi Puzzle from a text file with or without bridges.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Export: Export the current puzzle to a text file including current bridges.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("-Check Solution: Check if the current bridges on the board are a solution.", xStart + 15, currentPlace += lineSpacing);
//			g2d.drawString("--Red circles indicate islands with an incorrect number of connected bridges.", xStart + 30, currentPlace += lineSpacing);
//			g2d.drawString("--Blue bounding boxes indicate islands separated from the main network.", xStart + 30, currentPlace += lineSpacing);
//			g2d.drawString("-Solve Puzzle: Solves the current puzzle.", xStart + 15, currentPlace += lineSpacing);
			// i used chatgpt to reformat the stuff in the istructions :)
			    int lineSpacing = 20;
			    int currentPlace = 230;
			    int xStart = 300;
			    
			    // Prepare drawing context.
			    g2d.setStroke(new BasicStroke(3));
			    g2d.setColor(Color.WHITE);
			    g2d.fillRect(xStart, 200, 800, 700);
			    g2d.setColor(Color.BLACK);
			    g2d.drawRect(xStart, 200, 800, 700);

			    // --- Title ---
			    g2d.setFont(new Font("Arial", Font.BOLD, 50));
			    g2d.drawString("Instructions:", xStart + 10, currentPlace += lineSpacing);

			    // --- Hashi Rules header ---
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.drawString("Hashi Rules:", xStart + 10, currentPlace += lineSpacing);

			    // --- Rule 1 ---
			    // "-The number on each island tells you how many bridges must be connected to it."
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    String rule1a = "-The number on each island tells you how many ";
			    String rule1Highlight = "bridges";
			    String rule1b = " must be connected to it.";
			    FontMetrics fm = g2d.getFontMetrics();
			    
			    // Draw first segment.
			    g2d.drawString(rule1a, xStart + 15, currentPlace += lineSpacing);
			    int offset = fm.stringWidth(rule1a);
			    
			    // Draw highlighted part (in bold and colored).
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.MAGENTA);
			    g2d.drawString(rule1Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(rule1Highlight);
			    
			    // Draw remaining text.
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(rule1b, xStart + 15 + offset, currentPlace);

			    // --- Rule 2 ---
			    // "-Bridges can only be horizontal or vertical and cannot cross eachother."
			    String rule2a = "-Bridges can only be ";
			    String rule2Highlight1 = "horizontal";
			    String rule2Middle = " or ";
			    String rule2Highlight2 = "vertical";
			    String rule2b = " and cannot cross eachother.";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(rule2a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(rule2a);
			    
			    // First highlighted word.
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.MAGENTA);
			    g2d.drawString(rule2Highlight1, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(rule2Highlight1);
			    
			    // Draw middle plain text.
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(rule2Middle, xStart + 15 + offset, currentPlace);
			    offset += fm.stringWidth(rule2Middle);
			    
			    // Second highlighted word.
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.MAGENTA);
			    g2d.drawString(rule2Highlight2, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(rule2Highlight2);
			    
			    // Remainder of the line.
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(rule2b, xStart + 15 + offset, currentPlace);

			    // --- Rule 3 ---
			    // "-Bridges must connect all islands to a single connected network."
			    String rule3a = "-Bridges must connect all islands to a ";
			    String rule3Highlight = "single connected network";
			    String rule3b = ".";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(rule3a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(rule3a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.MAGENTA);
			    g2d.drawString(rule3Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(rule3Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(rule3b, xStart + 15 + offset, currentPlace);

			    currentPlace += 10; // Extra space between sections

			    // --- General Info section ---
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.drawString("General info:", xStart + 10, currentPlace += lineSpacing);
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));

			    // Example Info line: "-Connect 2 islands with a bridge by dragging from one to another."
			    String info1a = "-Connect 2 islands with a ";
			    String info1Highlight = "bridge";
			    String info1b = " by dragging from one to another.";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(info1a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(info1a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.MAGENTA);
			    g2d.drawString(info1Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(info1Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(info1b, xStart + 15 + offset, currentPlace);

			    // Info line for green highlight.
			    String info2a = "-Islands with the correct amount of bridges are highlighted ";
			    String info2Highlight = "green";
			    String info2b = ".";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(info2a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(info2a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.GREEN);
			    g2d.drawString(info2Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(info2Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(info2b, xStart + 15 + offset, currentPlace);

			    // Info line for red highlight.
			    String info3a = "-Islands with too many bridges are highlighted ";
			    String info3Highlight = "red";
			    String info3b = ".";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(info3a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(info3a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.RED);
			    g2d.drawString(info3Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(info3Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(info3b, xStart + 15 + offset, currentPlace);

			    currentPlace += 10; // Extra space

			    // --- Controls section ---
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.drawString("Controls:", xStart + 10, currentPlace += lineSpacing);
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));

			    // Control instruction: "-Press 'ESC' to close the program."
			    String ctrl1a = "-Press '";
			    String ctrl1Highlight = "ESC";
			    String ctrl1b = "' to close the program.";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(ctrl1a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(ctrl1a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.BLUE);
			    g2d.drawString(ctrl1Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(ctrl1Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(ctrl1b, xStart + 15 + offset, currentPlace);

			    // Other control instructions remain plain.
			    g2d.drawString("-Drag on the puzzle board to move the puzzle around.", xStart + 15, currentPlace += lineSpacing);
			    g2d.drawString("-Scroll to change the spacing between islands.", xStart + 15, currentPlace += lineSpacing);
			    g2d.drawString("-Hotkeys are indicated by the letter next to the button (I-instructions, R-Re-Center...).", xStart + 15, currentPlace += lineSpacing);

			    currentPlace += 10; // Extra space

			    // --- Buttons section ---
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.drawString("Buttons:", xStart + 10, currentPlace += lineSpacing);
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));

			    // Button: -Instructions: Opens and closes this menu.
			    g2d.drawString("-Instructions: Opens and closes this menu.", xStart + 15, currentPlace += lineSpacing);

			    // Button: -Re-Center: Puts the puzzle back in the location where it started.
			    String btn2a = "-Re-Center: Puts the puzzle back in the ";
			    String btn2Highlight = "location";
			    String btn2b = " where it started.";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(btn2a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(btn2a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.BLUE);
			    g2d.drawString(btn2Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(btn2Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(btn2b, xStart + 15 + offset, currentPlace);

			    // Button: -Clear Bridges: Removes all bridges on screen.
			    g2d.drawString("-Clear Bridges: Removes all bridges on screen.", xStart + 15, currentPlace += lineSpacing);

			    // Button: --Undo: Undoes the clear bridges button press. Does not work after next action.
			    g2d.drawString("--Undo: Undoes the clear bridges button press. Does not work after next action.", xStart + 30, currentPlace += lineSpacing);

			    // Button: -Import: Import a Hashi Puzzle from a text file with or without bridges.
			    g2d.drawString("-Import: Import a Hashi Puzzle from a text file with or without bridges.", xStart + 15, currentPlace += lineSpacing);

			    // Button: -Export: Export the current puzzle to a text file including current bridges.
			    g2d.drawString("-Export: Export the current puzzle to a text file including current bridges.", xStart + 15, currentPlace += lineSpacing);

			    // Button: -Check Solution: Check if the current bridges on the board are a solution.
			    String btn7a = "-Check Solution: Check if the current bridges on the board are a ";
			    String btn7Highlight = "solution";
			    String btn7b = ".";
			    fm = g2d.getFontMetrics();
			    
			    g2d.drawString(btn7a, xStart + 15, currentPlace += lineSpacing);
			    offset = fm.stringWidth(btn7a);
			    
			    g2d.setFont(new Font("Arial", Font.BOLD, 20));
			    g2d.setColor(Color.BLUE);
			    g2d.drawString(btn7Highlight, xStart + 15 + offset, currentPlace);
			    offset += g2d.getFontMetrics().stringWidth(btn7Highlight);
			    
			    g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			    g2d.setColor(Color.BLACK);
			    g2d.drawString(btn7b, xStart + 15 + offset, currentPlace);

			    // Button additional info lines.
			    g2d.drawString("--Red circles indicate islands with an incorrect number of connected bridges.", xStart + 30, currentPlace += lineSpacing);
			    g2d.drawString("--Blue bounding boxes indicate islands separated from the main network.", xStart + 30, currentPlace += lineSpacing);
			    g2d.drawString("-Solve Puzzle: Solves the current puzzle.", xStart + 15, currentPlace += lineSpacing);

			
		}
		
	}
	
	private Island[] getValidNeighbors(Island is) {
		Island[] output = new Island[4];//N E S W
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				
			}
		}
		int tempRow = is.row - 1;
		int tempCol = is.col;
		String n = "12345678";
		String vBlock = "-=";
		String hBlock = "|#";
		//n
		while (tempRow >= 0 && !vBlock.contains("" + board[tempRow][tempCol]) && !n.contains("" + board[tempRow][tempCol])) {
			tempRow--;
		}
		if (tempRow >= 0 && tempRow + 1 != is.row && n.contains("" + board[tempRow][tempCol])) {
			output[0] = new Island(tempRow, tempCol, Integer.parseInt("" + board[tempRow][tempCol]));
		} else {
			output[0] = null;
		}
		//s
		tempRow = is.row + 1;
		tempCol = is.col;
		while (tempRow < board.length && !vBlock.contains("" + board[tempRow][tempCol]) && !n.contains("" + board[tempRow][tempCol])) {
			tempRow++;
		}
		if (tempRow < board.length && tempRow - 1 != is.row && n.contains("" + board[tempRow][tempCol])) {
			output[2] = new Island(tempRow, tempCol, Integer.parseInt("" + board[tempRow][tempCol]));
		} else {
			output[2] = null;
		}
		//e
		tempRow = is.row;
		tempCol = is.col + 1;
		while (tempCol < board[tempRow].length && !hBlock.contains("" + board[tempRow][tempCol]) && !n.contains("" + board[tempRow][tempCol])) {
			tempCol++;
		}
		if (tempCol < board[tempRow].length && tempCol - 1 != is.col && n.contains("" + board[tempRow][tempCol])) {
			output[1] = new Island(tempRow, tempCol, Integer.parseInt("" + board[tempRow][tempCol]));
		} else {
			output[1] = null;
		}
		//w
		tempRow = is.row;
		tempCol = is.col - 1;
		while (tempCol >= 0 && !hBlock.contains("" + board[tempRow][tempCol]) && !n.contains("" + board[tempRow][tempCol])) {
			tempCol--;
		}
		if (tempCol >= 0 && tempCol + 1 != is.col && n.contains("" + board[tempRow][tempCol])) {
			output[3] = new Island(tempRow, tempCol, Integer.parseInt("" + board[tempRow][tempCol]));
		} else {
			output[3] = null;
		}
		return output;
	}
	
	private void drag(Island is1, Island is2) {
		char replacer = ' ';
		boolean horizontal = is1.row - is2.row == 0;
		
		if (!is1.nb.containsKey(is2) || is1.nb.get(is2) == 0) {
			if (horizontal) {
				replacer = '-';
			} else {
				replacer = '|';
			}
			is1.nb.put(is2, 1);
			is2.nb.put(is1, 1);
		} else if (is1.nb.get(is2) == 1) {
			if (horizontal) {
				replacer = '=';
			} else {
				replacer = '#';
			}
			is1.nb.put(is2, 2);
			is2.nb.put(is1, 2);
		} else {
			is1.nb.put(is2, 0);
			is2.nb.put(is1, 0);
		}
		int vIncrement = 0;
		int hIncrement = 0;
		if (horizontal) {
			hIncrement = (is2.col - is1.col)/Math.abs(is2.col - is1.col);
		} else {
			vIncrement = (is2.row - is1.row)/Math.abs(is2.row - is1.row);
		}
		int tempRow = is1.row + vIncrement;
		int tempCol = is1.col + hIncrement;
		String n = "12345678";
		while (tempCol >= 0 && tempRow >= 0 && tempRow < board.length && tempCol < board[tempRow].length && !n.contains("" + board[tempRow][tempCol])) {
			board[tempRow][tempCol] = replacer;
			tempRow += vIncrement;
			tempCol += hIncrement;
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//x = (int) (zoom * (x - mx) + mx);
		//y = (int) (zoom * (y - my) + my);
		//x -= zoom * spacing * (x-mx)/(Math.sqrt((x-mx)*(x-mx)+(y-my)*(y-my)));
		//y -= zoom * spacing * (y-mx)/(Math.sqrt((x-mx)*(x-mx)+(y-my)*(y-my)));
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(50, 125, Math.max(getWidth() - 500, 300), Math.max(getHeight() - 180, 100), 100, 100);
		if (new Area(roundedRect).contains(e.getPoint()) && !instructions) {
			if (e.getUnitsToScroll() > 0) {
				spacing *= 0.9;
			} else {
				spacing *= 1.1;
			}
			if (spacing > 200) {
				spacing = 200;
			}
			if (spacing < 70) {
				spacing = 70;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		RoundRectangle2D roundedRect = new RoundRectangle2D.Double(50, 125, Math.max(getWidth() - 500, 300), Math.max(getHeight() - 180, 100), 100, 100);
		if (currentSelected0 == null && !instructions && new Area(roundedRect).contains(e.getPoint())) {
			x += e.getX()-mx;
			y += e.getY()-my;
		}
		mx = e.getX();
		my = e.getY();
		boolean selected = false;
		for (Island is : islands) {
			// if distance < radius (25) from center changed to 30 for more leniency
			double cx = x + (is.col - board[0].length/2) * spacing + 25;
			double cy = y + (is.row - board.length/2) * spacing + 25;
			double dist = Math.sqrt((e.getX() - cx)*(e.getX() - cx) + (e.getY() - cy)*(e.getY() - cy));
			if (dist < 30) {
				selected = true;
				currentSelected2 = is;
			}
		}
		if (!selected) {
			currentSelected2 = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		boolean selected = false;
		for (Island is : islands) {
			// if distance < radius (25) from center changed to 30 for more leniency
			double cx = x + (is.col - board[0].length/2) * spacing + 25;
			double cy = y + (is.row - board.length/2) * spacing + 25;
			double dist = Math.sqrt((e.getX() - cx)*(e.getX() - cx) + (e.getY() - cy)*(e.getY() - cy));
			if (dist < 30) {
				selected = true;
				currentSelected2 = is;
			}
		}
		if (selected && !instructions && !solving) {
			getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			currentSelected2 = null;
		}
		for (int i = 0; i < buttons.size(); i++) {
			if (e.getX() > buttons.get(i).x && e.getY() > buttons.get(i).y && e.getX() < buttons.get(i).x + buttons.get(i).width && e.getY() < buttons.get(i).y + buttons.get(i).height) {
				buttons.get(i).hovering = true;
			} else {
				buttons.get(i).hovering = false;
			}
		}
	}
	
	public void buttonClicked(int buttonID) {
		switch(buttonID) {
		case 0: // instructions
			instructions = !instructions;
			break;
		case 1: // re center
			spacing = 90;
			x = 100 + board[0].length/2 * spacing;
			y = 150 + board.length/2 * spacing;
			break;
		case 2:
			if (solving) {
				break;
			}
			islandsCopy = new HashSet<Island>();
			for (Island toCopy : islands) {
				Island toAdd = new Island(toCopy.row, toCopy.col, toCopy.value);
				for (Island nb : toCopy.nb.keySet()) {
					toAdd.nb.put(nb, toCopy.nb.get(nb));
				}
				islandsCopy.add(toAdd);
			}
			boardCopy = new char[board.length][];
			for (int i = 0; i < board.length; i++) {
				boardCopy[i] = new char[board[i].length];
				for (int j = 0; j < board[i].length; j++) {
					boardCopy[i][j] = board[i][j];
				}
			}
			String toRemove = "=-#|"; 
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					if (toRemove.contains("" + board[i][j])) {
						board[i][j] = ' ';
					}
				}
			}
			for (Island is : islands) {
				is.nb.clear();
			}
			undoable = true;
			break;
		case 3: // import file
			JFileChooser fileChooser = new JFileChooser();
			
			// make it look windowsy
			try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.updateComponentTreeUI(fileChooser);
			} catch (Exception e) {
				System.err.println(e);
			}
			
			//start user selection in downloads folder
			String userHome = System.getProperty("user.home");
      File downloads = new File(userHome, "Downloads");
      fileChooser.setCurrentDirectory(downloads);
      // filter so only .txt files
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
      fileChooser.setFileFilter(filter);
      
      int result = fileChooser.showOpenDialog(this);
      if(result == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          // System.out.println("Selected file: " + selectedFile.getAbsolutePath());
          List<String> stuff = null;
					try {
						stuff = HashiDriver.txtToList(selectedFile.getAbsolutePath());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
          init(stuff);
      }
      break;
		case 4: // export file
			JFileChooser fileChooser2 = new JFileChooser();
			// make it look windowsy
			try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.updateComponentTreeUI(fileChooser2);
			} catch (Exception e) {
				System.err.println(e);
			}
			// only write to txt file
      FileNameExtensionFilter filter2 = new FileNameExtensionFilter("Text Files", "txt");
      fileChooser2.setFileFilter(filter2);
      // export to downloads folder by default
      String userHome2 = System.getProperty("user.home");
      File downloads2 = new File(userHome2, "Downloads");
      if (downloads2.exists()) {
          fileChooser2.setCurrentDirectory(downloads2);
      }
      // user file selection
      int userSelection = fileChooser2.showSaveDialog(this);
      if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser2.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".txt");
        }
        // write board to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
          for (char[] row : board) {
            writer.write(row);
            writer.newLine();   
          }
          JOptionPane.showMessageDialog(this, "File saved successfully at: \n" + fileToSave.getAbsolutePath());
	      } catch (IOException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	      }
      }
			break;
		case 5: // check solution
			if (solving && !solvedMessage) {
				break;
			}
			unsolvedIslands = new ArrayList<Island>();
			boolean good = true;
			Island starter = null;
			for (Island is : islands) {
				if (!is.isFull()) {
					unsolvedIslands.add(is);
					good = false;
				}
				starter = is;
			}
			// bfs on the island. count the number of islands visited. if total islands = islands visited then good else bad
			int count = 1;
			/*create a queue toVisit
			mark starter as visited and put starter into toVisit 
			while toVisit is non-empty 
    		remove the head head of toVisit
    		mark and enqueue all (unvisited) neighbours of head*/
			//bfs
			// System.out.println("starter: " + starter + ", nbs: " + starter.nb.keySet());
			Queue<Island> toVisit = new LinkedList<Island>();
			HashSet<Island> visited = new HashSet<Island>();
			for (Island is : starter.nb.keySet()) {
				if (starter.nb.get(is) == 0) {
					continue;
				}
				for (Island temp : islands) {
					if (temp.equals(is)) {
						toVisit.add(temp);
					}
				}
			}
			visited.add(starter);
			while (!toVisit.isEmpty()) {
				// System.out.println("queue1: " + toVisit);
				
				Island head = toVisit.poll();
				// System.out.println("head nbs1: " + head.nb.keySet());
				count++;
				visited.add(head);
				for (Island is : head.nb.keySet()) {
					if (head.nb.get(is) == 0) {
						continue;
					}
					// System.out.println("head nbs2: " + is);
					if (!visited.contains(is) && !toVisit.contains(is)) {
						for (Island temp : islands) {
							if (temp.equals(is)) {
								toVisit.add(temp);
							}
						}
					}
				}
			}
			boolean good2 = true;
			if (islands.size() != count) {
				good2 = false;
			}
			// get every group to put boxes around
			// dfs on all elemts bascily
		    groups = new ArrayList<>();
		    // HashSet<Island> totalVisited = new HashSet<>();
		    for (Island all : islands) {
		    	Queue<Island> newToVisit = new LinkedList<Island>();
				HashSet<Island> newVisited = new HashSet<Island>();
				for (Island is : all.nb.keySet()) {
					if (all.nb.get(is) == 0) {
						continue;
					}
					for (Island temp : islands) {
						if (temp.equals(is)) {
							newToVisit.add(temp);
						}
					}
				}
				newVisited.add(all);
				
				while (!newToVisit.isEmpty()) {
					// System.out.println("queue1: " + toVisit);
					
					Island head = newToVisit.poll();
					// System.out.println("head nbs1: " + head.nb.keySet());
					newVisited.add(head);
					for (Island is : head.nb.keySet()) {
						if (head.nb.get(is) == 0) {
							continue;
						}
						// System.out.println("head nbs2: " + is);
						if (!newVisited.contains(is) && !newToVisit.contains(is)) {
							for (Island temp : islands) {
								if (temp.equals(is)) {
									newToVisit.add(temp);
								}
							}
						}
					}
				}
				groups.add(newVisited);
				
	    }
			
			
			if (!good || !good2) {
				// not solved
				solved = false;
				if (!solvedMessage && !notSolvedMessage) {
					notSolvedMessage = true;
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							//System.out.println("Here1");
							try {
								Thread.sleep(2500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							//System.out.println("Here2");
							notSolvedMessage = false;
						}
					});
					t.start();
				}
			} else {
				solved = true;
				groups = null;
				if (!solvedMessage && !notSolvedMessage) {
					solvedMessage = true;
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							//System.out.println("Here1");
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							//System.out.println("Here2");
							solvedMessage = false;
						}
					});
					t.start();
				}
			}
			break;
		case 6: // solve
			buttonClicked(2);
			undoable = false;
			
			if (!solving) {
				solving = true;
				solvable = true;
				Thread t2 = new Thread(new Runnable() {
					@Override
					public void run() {
						if (unSolvableMessage) {
							// System.out.println("Here");
							return;
						}
						if (!solvable) {
							unSolvableMessage = true;
						} else {
							unSolvableMessage = false;
						}
						try {
							Thread.sleep(2500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						unSolvableMessage = false;
					}
				});
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						deterministicSolver();
						solvedMessage = true;
						buttonClicked(5);
						solvedMessage = false;
						solvable = nonDeterministicSolver(0);
						// System.out.println(solvable);
						solving = false;
						if (!unSolvableMessage) {
							t2.start();
						}
						
					}
				});
				
				t.start();
				// System.out.println(unSolvableMessage);
				
				
				
			}
			break;
		case 7:
			if (undoable) {
				undoable = false;
				islands = islandsCopy;
				board = boardCopy;
			}
			break;
		case 8:
			animationSpeed = 1.5;
			break;
		case 9:
			animationSpeed = 0.5;
			break;
		case 10:
			animationSpeed = 0.1;
			break;
		case 11:
			animationSpeed = 0;
			break;
		}
		
			
	}
	
	// loop through each island
	// if it is not solved at any given island, draw a line to its first valid nb and rerun deterministic. if solved yay else nay;
	
	private boolean nonDeterministicSolver(int n) {
		int nCopy = n;
		if (n > islands.size() * 8) {
			return false;
		}
		islandsCopy = new HashSet<Island>();
		for (Island toCopy : islands) {
			Island toAdd = new Island(toCopy.row, toCopy.col, toCopy.value);
			for (Island nb : toCopy.nb.keySet()) {
				toAdd.nb.put(nb, toCopy.nb.get(nb));
			}
			islandsCopy.add(toAdd);
		}
		boardCopy = new char[board.length][];
		for (int i = 0; i < board.length; i++) {
			boardCopy[i] = new char[board[i].length];
			for (int j = 0; j < board[i].length; j++) {
				boardCopy[i][j] = board[i][j];
			}
		}
		for (Island is : islands) {
			if (!is.isFull()) {
				Island[] validNb = getValidNeighbors(is);
				boolean selected = false;
				for (Island nb : validNb) {
					if (nb == null || selected) {
						continue;
					}
					if (nCopy > 0) {
						nCopy--;
						continue;
					}
					for (Island temp : islands) {
						if (nb.equals(temp) && !temp.isFull()) {
							selected =true;
							while (concurrentModificationErrorFix) { 
								try {
									Thread.sleep(0);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							drag(is, temp);
							try {
								Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		deterministicSolver();
		solvedMessage = true;
		buttonClicked(5);
		boolean currentIsSolved = solved;
		solvedMessage = false;
		if (!currentIsSolved) {
			islands = islandsCopy;
			board = boardCopy;
			nonDeterministicSolver(n + 1);
		}
		return true;
	}

	
	private void deterministicSolver() {
		for (int i = 0; i < islands.size(); i++) {
			for (Island is : islands) {
				Island[] validNb = getValidNeighbors(is);
				if (is.totalNeighborValue() == 0) {
					int counter = 0;
					for (int j = 0; j < validNb.length; j++) {
						if (validNb[j] != null) {
							for (Island temp : islands) {
								if (temp.equals(validNb[j])) {
									if (!temp.isFull()) {
										
										counter++;
									} else {
										validNb[j] = null;
										// System.out.println("Here: " + is);
									}
								}
							}
						}
					}
					if (is.value/(counter * 1.0) > 2) {
						//unsolvable
						return;
					}
					if ((is.value + 1)/counter >= 2) {
//						if (is.row == 8 && is.col == 4)
//							System.out.println(is + ", " + counter);
						for (Island tempNb : validNb) {
							if (tempNb != null) {
								if (is.value % 2 == 0) {
									
									// is.nb.put(tempNb, 2);
									for (Island temp : islands) {
										if (temp.equals(tempNb)) {
											// temp.nb.put(is, 2);
											while (concurrentModificationErrorFix) {
												try {
													Thread.sleep(0);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											while (concurrentModificationErrorFix) { 
												try {
													Thread.sleep(0);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											drag(is, temp);
											try {
												Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											while (concurrentModificationErrorFix) { 
												try {
													Thread.sleep(0);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											drag(is, temp);
											try {
												Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}
								} else {
									//is.nb.put(tempNb, 1);
									for (Island temp : islands) {
										if (temp.equals(tempNb)) {
											//temp.nb.put(is, 1);
											while (concurrentModificationErrorFix) { 
												try {
													Thread.sleep(0);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											drag(is, temp);
											try {
												Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}
								}
							}
						}
					}
				} else {
					// total neighbor value > 0
					// if value - totalNeighborValue == number of connections left
					// do those connections
					int numConnections = 8;
					for (int cCount : is.nb.values()) {
						numConnections -= cCount;
					}
					for (int c = 0; c < validNb.length; c++) {
						if (validNb[c] == null) {
							numConnections -= 2;
							continue;
						}
						for (Island temp : islands) {
							if (temp.equals(validNb[c])) {
								if (is.nb.containsKey(temp) && temp.isFull()) {
									numConnections -= (2 - is.nb.get(temp));
								} else if (temp.isFull()) {
									numConnections -= 2;
								}
							}
						}
					}
					
					if (is.value - is.totalNeighborValue() == numConnections && numConnections != 0) {
						
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						for (Island nb : validNb) {
							if (nb == null) {
								continue;
							}
							boolean dontUse = false;
							for (Island temp : islands) {
								if (temp.equals(nb)) {
									if (temp.isFull()) {
										dontUse = true;
									}
								}
							}
							if (dontUse) {
								continue;
							}
							if (!is.nb.containsKey(nb) || is.nb.get(nb) == 0) {
								for (Island temp : islands) {
									if (nb.equals(temp)) {
										while (concurrentModificationErrorFix) { 
											try {
												Thread.sleep(0);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										drag(is, temp);
										try {
											Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										while (concurrentModificationErrorFix) { 
											try {
												Thread.sleep(0);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										drag(is, temp);
										try {
											Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										
									}
								}
							} else if (is.nb.get(nb) == 1) {
								for (Island temp : islands) {
									if (nb.equals(temp)) {
										while (concurrentModificationErrorFix) { 
											try {
												Thread.sleep(0);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										drag(is, temp);
										try {
											Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
					// if there are n possible spots left and n - 1 connections then connect once to each empty neighbor
					// System.out.println(numConnections + ", " + is);
					if (numConnections == is.value - is.totalNeighborValue() + 1 && !is.isFull()) {
						for (Island nb : validNb) {
							if (nb == null) {
								continue;
							}
							for (Island temp : islands) {
								if (nb.equals(temp)) {
									if (!temp.isFull() && (!is.nb.containsKey(temp) || is.nb.get(temp) == 0)) {
										drag(is, temp);
										try {
											Thread.sleep((long) (ANIMATION_TIME * animationSpeed));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
		if (e.getKeyCode() == KeyEvent.VK_I) {
			buttonClicked(0);
		}
		if (e.getKeyCode() == KeyEvent.VK_R) {
			buttonClicked(1);
		}
		if (e.getKeyCode() == KeyEvent.VK_C) {
			buttonClicked(2);
		}
		if (e.getKeyCode() == KeyEvent.VK_J) {
			buttonClicked(3);
		}
		if (e.getKeyCode() == KeyEvent.VK_E) {
			buttonClicked(4);
		}
		if (e.getKeyCode() == KeyEvent.VK_V) {
			buttonClicked(5);
		}
		if (e.getKeyCode() == KeyEvent.VK_S) {
			buttonClicked(6);
		}
		if (e.getKeyCode() == KeyEvent.VK_Z) {
			buttonClicked(7);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
				
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (int i = 0; i < buttons.size(); i++) {
			if (e.getX() > buttons.get(i).x && e.getY() > buttons.get(i).y && e.getX() < buttons.get(i).x + buttons.get(i).width && e.getY() < buttons.get(i).y + buttons.get(i).height) {
				buttonClicked(i);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		boolean selected = false;
		for (Island is : islands) {
			// if distance < radius (25) from center changed to 30 for more leniency
			double cx = x + (is.col - board[0].length/2) * spacing + 25;
			double cy = y + (is.row - board.length/2) * spacing + 25;
			double dist = Math.sqrt((e.getX() - cx)*(e.getX() - cx) + (e.getY() - cy)*(e.getY() - cy));
			if (dist < 30) {
				currentSelected0 = is;
				selected = true;
			}
		}
		if (!selected) {
			currentSelected0 = null;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		boolean selected = false;
		for (Island is : islands) {
			// if distance < radius (25) from center
			double cx = x + (is.col - board[0].length/2) * spacing + 25;
			double cy = y + (is.row - board.length/2) * spacing + 25;
			double dist = Math.sqrt((e.getX() - cx)*(e.getX() - cx) + (e.getY() - cy)*(e.getY() - cy));
			if (dist < 25) {
				currentSelected1 = is;
				selected = true;
			}
		}
		if (!selected) {
			currentSelected1 = null;
		}
		if (currentSelected0 != null && currentSelected1 != null && !currentSelected0.equals(currentSelected1)) {
			for (Island is : islands) {
				if (is.equals(currentSelected0)) {
					Island[] neighbors = getValidNeighbors(is);
					boolean found = false;
					for (int i = 0; i < neighbors.length; i++) {
						if (currentSelected1.equals(neighbors[i])) {
							found = true;
						}
					}
					if (!found || instructions || solving) {
						continue;
					}
					drag(currentSelected0, currentSelected1);
					undoable = false;
				}
			}
		}
		currentSelected0 = null;
		currentSelected1 = null;
		mx = e.getX();
		my = e.getY();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	public class Button {
		int x;
		int xOffset;
		int y;
		int width;
		int height;
		int font;
		int fillet;
		String message;
		boolean hovering;
		Area rrectArea;
		Color normalColor = new Color(255, 255, 255);
		Color hoverColor = new Color(200, 200, 200);
		
		public Button(int xOffset, int y, int width, int height, int font, int fillet, String message) {
			super();
			this.xOffset = xOffset;
			this.y = y;
			this.width = width;
			this.height = height;
			this.font = font;
			this.fillet = fillet;
			this.message = message;
			x = getWidth() - xOffset;
			rrectArea = new Area(new RoundRectangle2D.Double(x, y, width, height, fillet, fillet));
		}

		public void drawButton(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			x = getWidth() - xOffset;
			rrectArea = new Area(new RoundRectangle2D.Double(x, y, width, height, fillet, fillet));
			g2d.setStroke(new BasicStroke(5));
			if (hovering) {
				g2d.setColor(hoverColor);
			} else {
				g2d.setColor(normalColor);
			}
			
			g2d.fill(rrectArea);
			g2d.setColor(Color.BLACK);
			g2d.draw(rrectArea);
			g2d.setColor(Color.BLACK);
			g2d.setFont(new Font("Arial", Font.PLAIN, font));
			g2d.drawString(message, x + width/10, y + height/2 + font/3);
		}
	}
	
}
