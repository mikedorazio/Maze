import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.*;

import org.apache.logging.log4j.*;

/**
  * A JPanel that hosts the Frame that we will use to render the Maze.
  * @author Mike D'Orazio
  */
public class MazePanel 
	extends JPanel 
	implements ActionListener, MouseInputListener {

	public static Logger logger = LogManager.getLogger(MazePanel.class);
		/**  the Maze this panel will host */
	private Maze maze;
		/** width of maze in pixels */
	private int width;
		/** height of maze in pixels */
	private int height;
		/** indicates if generation of maze should be animated. */
	private boolean generateAnimated;
		/** indicates if solving maze should be animated. */
	private boolean solveAnimated;
		/** sleep time for animation */
	private int solveSleepValue;
		/** the cell that is currently being visited for the solution */
	private int currentSolutionCell;
		/** the sleep interval used when showing Maze generation. */
	private int generateSleepValue;
	private java.util.List walls;
	private java.util.List solutionVisited;
	private MazeFrame parent;
	private ControlPanel controlPanel;

	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	/**
	  * Constructs a MazePanel that will host the Maze.
	  * @param parent the JFrame that is the parent of this Panel.
	  * @param rows the number of rows the Maze should have.
	  * @param columns the number of columns the Maze should have.
	  */
	public MazePanel(MazeFrame parent, int rows, int columns) {
		this.parent = parent;
		maze = new Maze(rows, columns);
		walls = maze.getWalls();
		solutionVisited = new ArrayList();
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	  * Handles MouseEvent events.  These are generated when user is solving maze.
	  */
	public void mouseDragged(MouseEvent me) {
		int solutionCell = getCell(me.getX(), me.getY());
		logger.error("sol: " + solutionCell + " curr: " + currentSolutionCell);
		if (currentSolutionCell != solutionCell && 
			maze.pathExists(currentSolutionCell, solutionCell)) {
				// we have visited this one already...must be backtracking...
			if (maze.getSolution().contains(new Integer(solutionCell))) {
				maze.getSolution().remove(new Integer(currentSolutionCell));
			}
			else {
				if (currentSolutionCell == 0) {
					maze.getSolution().add(new Integer(currentSolutionCell));
				}
				maze.getSolution().add(new Integer(solutionCell));
			}
			currentSolutionCell = solutionCell;
			paintImmediately(new Rectangle(getWidth(), getHeight()));
		}
	}

	public void setGenerateSleepValue(int value) {
		this.generateSleepValue = value;
	}
	public void setSolveSleepValue(int value) {
		this.solveSleepValue = value;
	}

	/**
	  * Handles all ActionEvents generated on the ControlPanel.	
	  * @param e the ActionEvent to handle.
	  */
	public void actionPerformed(ActionEvent e) {
		logger.debug("actionPerformed" + e);
		if (e.getActionCommand().equalsIgnoreCase("Clear")) {
			maze.getSolution().clear();
			solutionVisited = new ArrayList();
			currentSolutionCell = 0;
			repaint();
		}
		if (e.getActionCommand().equalsIgnoreCase("gAnimate")) {
			JCheckBox box = (JCheckBox)e.getSource();
			logger.error("setting gAnimate to " + box.isSelected());
			setGenerateAnimated(box.isSelected());
		}
		if (e.getActionCommand().equalsIgnoreCase("sAnimate")) {
			maze.getSolution().clear();
			solutionVisited = new ArrayList();
			solveAnimated();
		}
		if (e.getActionCommand().equalsIgnoreCase("GridSize")) {
			JComboBox box = (JComboBox)e.getSource();
			logger.debug(box.getSelectedItem());
			int index = box.getSelectedIndex();
			parent.setTitle(MazeFrame.TITLE, MazeFrame.GRID_SIZES[index], MazeFrame.GRID_SIZES[index]);
				// TOFIX: put in worker thread
			maze.setRows(MazeFrame.GRID_SIZES[index]);
			maze.setColumns(MazeFrame.GRID_SIZES[index]);
			maze.reset();
			walls = maze.getWalls();
			solutionVisited = new ArrayList();
			currentSolutionCell = 0;
			repaint();
			controlPanel.setAnimateEnabled(false);
		}
		if (e.getActionCommand().equalsIgnoreCase("Generate")) {
			Thread worker = new Thread() {
				public void run() {
					maze.reset();
					currentSolutionCell = 0;
					walls = maze.getWalls();
					solutionVisited = new ArrayList();
					if (getGenerateAnimated())
						drawGeneration();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							repaint();
							controlPanel.setAnimateEnabled(false);
						}
					});
				}
			};
			worker.start();
		}
		if (e.getActionCommand().equalsIgnoreCase("Solve")) {
			Thread worker = new Thread() {
				public void run() {
					maze.solve();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
				 			repaint();
							controlPanel.setAnimateEnabled(true);
						}
					});
				}
			};
			worker.start();
		}
		if (e.getActionCommand().equalsIgnoreCase("Print")) {
			print();
		}
	}

	private void solveAnimated() {
			// get all of the visited cells while sovling Maze
		java.util.List solution = maze.getSolveVisited();
		logger.debug("solutionVisited is " + solution);
		Iterator i = solution.iterator();
		while (i.hasNext()) {
			Integer integer = (Integer)i.next();
			solutionVisited.add(integer);
			paintImmediately(new Rectangle(getWidth(), getHeight()));
			try { Thread.sleep(solveSleepValue); } catch (Exception e) {}
		}
	}

	private void drawGeneration() {
			// create fully walled maze
		walls = maze.createInitialWallList();
			// get Walls that were removed during construction
		java.util.List removedWalls = maze.getWallHistory();
		logger.debug("removedWalls is " + removedWalls);
		Iterator i = removedWalls.iterator();
		while (i.hasNext()) {
			Point p = (Point)i.next();
			walls.remove(p);
			paintImmediately(new Rectangle(getWidth(), getHeight()));
			try { Thread.sleep(generateSleepValue); } catch (Exception e) {}
		}
		logger.debug("walls is " + walls);
	}


	/**
	  * Paints the Panel.
	  * @param g the Graphics context to paint on.
	  */
	public void paintComponent(Graphics g) {
		//logger.debug("paintComponent() : g = " + this);
		//super.paintComponent(g);

			// clear out the Panel
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.black);

			// get dimensions
		width = (this.getWidth() / maze.getColumns()) ;
		height = (this.getHeight() / maze.getRows()) ;
		//logger.debug("width:" + width);
		//logger.debug("height is " + height);

			// draw starting "S" and finishing "F"
		g.setColor(Color.red);
		g.setFont(new Font("Times New Roman", Font.ITALIC, height));
		g.drawString("S", 0, height);
		g.drawString("F", width*(maze.getColumns()-1), height*maze.getRows());
		g.setColor(Color.black);

			// draw outer box
		int offset = 0;
		g.drawLine(0+offset, 0+offset, width*maze.getColumns()+offset, offset);
		g.drawLine(width*maze.getColumns()+offset, offset, 
					width*maze.getColumns()+offset, height*maze.getRows()+offset);
		g.drawLine(width*maze.getColumns()+offset, height*maze.getRows()+offset,
					0+offset, height*maze.getRows()+offset);
		g.drawLine(0+offset, height*maze.getRows()+offset, 0+offset, 0+offset);

			// draw walls that currently exist.  If we are in the middle of creating the
			// maze, only the walls that are left standing will be drawn.  This will give
			// it the animated effect.
		Iterator iterator = walls.iterator();
		while (iterator.hasNext()) {
			Point wall = (Point)iterator.next();
			int i = wall.x; int j = wall.y;
			drawWall(g, i, j, Color.black);
		}

			// draw the cells that were visited while solving 
		int previousWall = 0;
		Iterator visitedIterator = solutionVisited.iterator();
		//if (visitedIterator.hasNext())
			//previousWall =  ((Integer)iterator.next()).intValue();
		while (visitedIterator.hasNext()) {
			Integer solutionCell = (Integer)visitedIterator.next();
			drawSolutionCell(g, previousWall, solutionCell.intValue());
			previousWall = solutionCell.intValue();
		}

			// draw the solution for the current Maze
		iterator = maze.getSolution().iterator();
		previousWall = 0;
		if (iterator.hasNext())
			previousWall =  ((Integer)iterator.next()).intValue();
		while (iterator.hasNext()) {
			Integer solutionCell = (Integer)iterator.next();
			drawSolutionCell(g, previousWall, solutionCell.intValue());
			previousWall = solutionCell.intValue();
		}
	}

	/**
	  * Draws a solution line between the given cells.
	  * @param g the Graphics context to draw on.
	  * @param start the start cell of the solution.
	  * @param end the end cell of the solution.
	  */
	public void drawSolutionCell(Graphics g, int start, int end) {
			// the solution should be from the middle of the start cell
			// to the middle of the end cell.
		int widthOffset = width / 2;
		int heightOffset = height / 2;
		g.setXORMode(Color.orange);
		//g.setColor(Color.red);

			// determine row and column of cells
		int rowOfStart = maze.getRowOfCell(start);
		int columnOfStart = maze.getColumnOfCell(start);
		int rowOfEnd = maze.getRowOfCell(end);
		int columnOfEnd = maze.getColumnOfCell(end);

			// draw the line
		g.drawLine(columnOfStart*width+widthOffset, rowOfStart*height+heightOffset,
					columnOfEnd*width+widthOffset, rowOfEnd*height+heightOffset);
	}

	/**
	  * Draws a wall between 2 cells in the given color.
	  * @param g the Graphics context to draw on.
	  * @param firstCell the first cell of a wall to be drawn.
	  * @param secondCell the second cell of a wall to be drawn.
	  * @param color the color to draw the wall.
	  */
	public void drawWall(Graphics g, int firstCell, int secondCell, Color color) {
		//logger.debug("drawing wall btwn " + firstCell + " and " + secondCell + " in" + color);

			// get the first cell to be lower number
		if (firstCell > secondCell) {
			int temp = secondCell;
			secondCell = firstCell;
			firstCell = temp;
		}

			// get row and column of each cell
		int firstCellsRow = maze.getRowOfCell(firstCell);
		int firstCellsColumn = maze.getColumnOfCell(firstCell);
		int secondCellsRow = maze.getRowOfCell(secondCell);
		int secondCellsColumn = maze.getColumnOfCell(secondCell);

		g.setColor(color);
			// wall to be removed is between two cells on the same row
		if (firstCellsRow == secondCellsRow) {
			/*
			logger.debug("drawing same row wall at " + 
							(firstCellsColumn*width+width) + ", " +
							(firstCellsRow*height) + ", " + 
							(firstCellsColumn*width+width) + ", " +
							(firstCellsRow*height+height));
			*/

			g.drawLine(firstCellsColumn*width+width, firstCellsRow*height,
						firstCellsColumn*width+width, firstCellsRow*height+height);
		}
		else {
			// different rows
			/*
			logger.debug("drawing different row wall at " + 
							(firstCellsColumn*width) + ", " +
							(firstCellsRow*height+height) + ", " + 
							(firstCellsColumn*width+width) + ", " +
							(firstCellsRow*height+height));
			*/
			g.drawLine(firstCellsColumn*width, firstCellsRow*height+height,
						firstCellsColumn*width+width, firstCellsRow*height+height);
		}
	}

	/**
	  * Sets the flag for animation when generating the maze.
	  * @param ga flag to indicate if maze generation should be animated.
	  */
	public void setGenerateAnimated(boolean ga) {
		this.generateAnimated = ga;
	}

	/**
	  * Sets the flag for animation when solving the maze.
	  * @param sa flag to indicate if maze solving should be animated.
	  */
	public void setSolveAnimated(boolean sa) {
		this.solveAnimated = sa;
	}

	/**
	  * Gets the flag for animation when generating the maze.
	  * @return the flag that indicates if maze generation should be animated.
	  */
	public boolean getGenerateAnimated() {
		return generateAnimated;
	}

	/**
	  * Gets the flag for animation when solving the maze.
	  * @return the flag that indicates if maze solving should be animated.
	  */
	public boolean getSolveAnimated() {
		return solveAnimated;
	}

	/**
	  * Gets a cell number for the given x,y coordinates.
	  * @param x the x coordinate in question.
	  * @param y the y coordinate in question.
	  * @param the cell located at x,y.
	  */
	public int getCell(int x, int y) {
		int columnsIn = x / width;
		logger.debug("columns in is " + columnsIn);
		int rowsDown =  y / height;
		logger.debug("rows down is " + rowsDown);
		return rowsDown * maze.getColumns() + columnsIn;
	}
	
	public void setControlPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}

	/**
	  * Prints the Maze.
	  */
	public void print() {
		logger.debug("print() called");
		try {
			MazePDF pdf = new MazePDF();
			logger.debug("MazePDF() created");
			pdf.setMaze(maze);
			logger.debug("about to show maze");
			Runtime.getRuntime().exec("C:/Program Files (x86)/Adobe/Acrobat Reader DC/Reader/AcroRd32.exe maze.pdf");
		}
		catch (Throwable e) {
			parent.setTitle("Mike");
			logger.error(e);
		}
	}
}
