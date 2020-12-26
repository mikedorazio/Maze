import java.util.*;
import java.awt.Point;

import org.apache.log4j.*;

/**
  * Class that represents a "perfect" maze.  Each cell is connected to every other cell
  * by a unique path.
  * @author Mike D'Orazio
  */
public class Maze {
		// logger
	public static Logger logger = Logger.getLogger(Maze.class);
		/** the number of rows in this maze */
	private int rows;
		/** the number of rows in this maze */
	private int columns;
		/** the total number of cells for this maze */
	private int totalCells;
		/** List to keep track of which cells have been visited during construction */
	private List buildVisited;
		/** List to keep track of which walls have been removed during construction */
	private List wallHistory;
		/** List of all cells visited while solving */
	private List solveVisited;
		/** List to keep track of which cells have walls between them */
	private List walls;
		/** List to keep track of which cells are part of the solution */
	private List solution;
	private int BUILD = 0; private int SOLVE = 1;

	/**
	  * Constructs a 4x4 Maze.
	  */
	public Maze() {
		this(4,4);
	}

	/**
	  * Construct a Maze with the given number of rows and columns.
	  */
	public Maze(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		totalCells = rows * columns;
		buildVisited = new ArrayList(); 
		solveVisited = new ArrayList();
		wallHistory = new ArrayList();
		solution = new ArrayList();
		walls = createInitialWallList();
		build(0);
	}

	/**
	  * Gets the total number of cells in this Maze.
	  * @return the total number of cells for this Maze.
	  */
	public int getTotalCells() {
		return rows * columns;
	}

	/**
	  * Gets the total number of rows in this Maze.
	  * @return the total number of rows for this Maze.
	  */
	public int getRows() {
		return rows;
	}

	/**
	  * Gets the total number of columns in this Maze.
	  * @return the total number of columns for this Maze.
	  */
	public int getColumns() {
		return columns;
	}

	/**
	  * Constructs a wall between every pair of cells that are neighbors. A wall
	  * between cells x and y is represented by the Point(x,y).
	  */
	public List createInitialWallList() {
		List w = new ArrayList();
		for (int i = 0; i < rows * columns; i++) {
			for (int j = i; j < rows * columns; j++) {
				if (isRightOf(i, j)) {
					w.add(new Point(i, j));
					logger.debug(j + " is right of " + i);
				}
				if (isBelow(i, j)) {
					w.add(new Point(i, j));
					logger.debug(j + " is below of " + i);
				}
			}
		}
		logger.debug("walls : " + w);
		return w;
	}

	/**
	  * Gets all the neighbors for a given cell based on the given flags.
	  * @param unvisitedOnly if true, only get neighbors that have not been visited.
	  * @param withPath if true, returns all neighbors that have no wall between them.
	  * @param cell the cell in question.
	  * @return an ArrayList that contains all the neighbors of the given cell.
	  */
	private List getNeighbors(int type, boolean unvisitedOnly, boolean withPath, int cell) {
		logger.debug("cell in question: " +  cell);

		List visited;

		if (type == BUILD) visited = getBuildVisited(); else visited = getSolveVisited();

		ArrayList list = new ArrayList();
		Integer neighbor;
		
		int left = cell - 1;
		if (! withPath || ! walls.contains(new Point(left, cell))) {
			if (left >= 0 && getRowOfCell(left) == getRowOfCell(cell)) {
				neighbor = new Integer(left);
				if (unvisitedOnly && (! visited.contains(neighbor))) {
					logger.debug(" left near " + left);
					list.add(neighbor);
				}
			}
		}

		int right = cell + 1;
		if (! withPath || ! walls.contains(new Point(cell, right))) {
			if (right < rows*columns && getRowOfCell(right) == getRowOfCell(cell)) {
				neighbor = new Integer(right);
				if (unvisitedOnly && (! visited.contains(neighbor))) {
					logger.debug(" right near " + right);
					list.add(neighbor);
				}
			}
		}

		int above = cell - columns;
		if (! withPath || ! walls.contains(new Point(above, cell))) {
			if (above >= 0 ) {
				neighbor = new Integer(above);
				if (unvisitedOnly && (! visited.contains(neighbor))) {
					logger.debug(" above near " + above);
					list.add(neighbor);
				}
			}
		}

		int below = cell + columns;
		if (! withPath || ! walls.contains(new Point(cell, below))) {
			if (below < rows*columns ) {
				neighbor = new Integer(below);
				if (unvisitedOnly && (! visited.contains(neighbor))) {
					logger.debug(" below near " + below);
					list.add(neighbor);
				}
			}
		}

		logger.debug(list);
		return list;
	}

	/**
	  * Removes walls between cells, therefore building the Maze.
	  */
	private void build(int currentCell) {
		logger.debug("build() called");
		int numberVisited = 1;
		List neighbors = null;
		getBuildVisited().add(new Integer(currentCell));
		
		Stack history = new Stack();
		history.push(new Integer(currentCell));

		while (numberVisited < getTotalCells()) {
				// get all the neighbors of this cell (a list of Integers)
			neighbors = getNeighbors(BUILD, true, false, currentCell);
			logger.debug(neighbors);
			if (neighbors != null && neighbors.size() > 0) {
				int neighborCount = neighbors.size();
				int randomCount = (int) (Math.random() * neighborCount);
				Integer randomNeighbor = (Integer)neighbors.get(randomCount);
					// remove wall
				Point p = new Point(Math.min(currentCell, randomNeighbor.intValue()),
									Math.max(currentCell, randomNeighbor.intValue()));
				int index = getWalls().indexOf(p);
				logger.debug("wall found at " + index);
				if (index >= 0) {
					getWalls().remove(index);
					getWallHistory().add(p);
				}
				getBuildVisited().add(new Integer(currentCell));
				history.push(new Integer(currentCell));
				currentCell = randomNeighbor.intValue();
				numberVisited++;
			}
			else {
				// this cell has no unvisited neighbors, add it to the visited list and
				// pop off the last cell that was visited in the history stack and try
				// that cell.  Even though it has been visited before, it may have some
				// unvisited neighbors.
				getBuildVisited().add(new Integer(currentCell));
				currentCell = ((Integer)history.pop()).intValue();
			}
		}
	}

	/**
	  * Determines if a path exists between two cells.  For a path to exist, they must be
	  * neighbors and they must not have a wall between them.
	  * @param a first cell in question.
	  * @param b second cell in question.
	  * @return true if a path exists from a to b, false otherwise.
	  */
	public boolean pathExists(int a, int b) {
		boolean c = ((isRightOf(a, b) || isRightOf(b, a) || 
					 isBelow(a, b) || isBelow(b, a)) && 
					! (wallExists(a, b) || wallExists(b, a)));
		logger.debug("checking path between " + a + " and " + b + " returning " + c);
		return c;
	}

	/**
	  * Determines if there is a wall between two cells.
	  * @param a first cell in question.
	  * @param b second cell in question.
	  * @return true if a wall exists between a and b, false otherwise.
	  */
	public boolean wallExists(int a, int b) {
		return walls.contains(new Point(a, b));
	}

	/**
	  * Determines if a cell has an unvisited neighbor to the right.
	  */
	public boolean hasRightNeighbor(int a) {
		if (solveVisited.contains(new Integer(a+1))) return false;
		if (walls.contains(new Point(a, a+1))) return false;
		return isRightOf(a, a+1);
	}
	public boolean hasLeftNeighbor(int a) {
		if (solveVisited.contains(new Integer(a-1))) return false;
		if (walls.contains(new Point(a-1, a))) return false;
		if (a == 0) return false;
		return isRightOf(a-1, a);
	}
	public boolean hasAboveNeighbor(int a) {
		if (solveVisited.contains(new Integer(a - columns))) return false;
		if (walls.contains(new Point(a-columns, a))) return false;
		if (a - columns < 0) return false;
		return isBelow(a - columns, a);
	}
	public boolean hasBelowNeighbor(int a) {
		if (solveVisited.contains(new Integer(a + columns))) return false;
		if (walls.contains(new Point(a, a+columns))) return false;
		if (a + columns > totalCells) return false;
		return isBelow(a, a+columns);
	}
	
	public boolean isRightOf(int a, int b) {
		if ((a+1) == b && getRowOfCell(a) == getRowOfCell(b))
			return true;
		return false;
	}

	public boolean isBelow(int a, int b) {
		if (b > totalCells) return false;
		return a+columns == b;
	}

	public int getRowOfCell(int cell) {
		return cell / columns;
	}

	public int getColumnOfCell(int cell) {
		return cell % columns;
	}

	public void setRows(int rows) {
		logger.debug("setting rows to " + rows);
		this.rows = rows;
		totalCells = rows * columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
		totalCells = rows * columns;
	}

	/**
	  * Resets the current Maze.
	  */
	public void reset() {
		logger.error("reset() called");
		buildVisited = new ArrayList();
		wallHistory = new ArrayList();
		solveVisited = new ArrayList();
		solution = new ArrayList();
		walls = createInitialWallList();
		build(0);
	}

	public List getWalls() {
		return walls;
	}
	public void setWalls(List list) {
		this.walls = list;
	}

	public List getSolution() {
		return solution;
	}
	public void setSolution(List solution) {
		this.solution = solution;
	}

	public List getBuildVisited() {
		return buildVisited;
	}
	public void setBuildVisited(Stack buildVisited) {
		this.buildVisited = buildVisited;
	}

	public List getWallHistory() {
		return wallHistory;
	}

	public List getSolveVisited() {
		return solveVisited;
	}
	public void setSolveVisited(List list) {
		this.solveVisited = list;
	}

	public boolean isSolved() {
		return getSolution() != null && (! getSolution().isEmpty());
	}

	/*
	 * Solves the Maze non-recursively.
	 * TOFIX: this method belongs in the Maze class but is here for now so we can animate...
	 */
 	public void solve() {
		if (isSolved()) {logger.error("done already"); return;}
 		Stack tempVisited = new Stack();
 		setSolveVisited(new Stack());
 			// assume start is upper left and end is lower right
 		int currentCell = 0; int endCell = getTotalCells();
 			// cell 0 is always part of solution
 		getSolution().add(new Integer(currentCell));
 		getSolveVisited().add(new Integer(currentCell));
 		
 		while (true) {
 			logger.debug("current Cell is " + currentCell);
 				// are we there yet???
 			if (currentCell == (endCell - 1)) break;
 				// get neighbors for this cell, if any, and pick one
 			java.util.List neighbors = getNeighbors(SOLVE, true, true, currentCell);
 			logger.debug("neighbors are " + neighbors);
 				// dead end...back out....
 			if (neighbors == null || neighbors.size() == 0) {
 				logger.debug("no neighbors for cell " + currentCell + " removing.....");
 					// remove cell from solution and pop the previous cell off the stack
 				getSolution().remove(new Integer(currentCell));
 				currentCell = ((Integer)tempVisited.pop()).intValue();
 				getSolveVisited().add(new Integer(currentCell));
 				continue;
 			}
				// add this to the ones we have tried...
			tempVisited.push(new Integer(currentCell));
 				// how many neighbors does this cell has...
 			int neighborSize = neighbors.size();
 				// choose one
 			int cell = (int)(Math.random() * neighborSize);
 			currentCell = ((Integer)neighbors.get(cell)).intValue();
 				// add this cell to the solution and visited
 			getSolution().add(new Integer(currentCell));
 			getSolveVisited().add(new Integer(currentCell));
 		}
 	}

	/**
	  * Solves the Maze using recursion starting at the given cell.
	  * @param cell the cell to start solving the Maze.
	  * @return true if this cell can be part of the solution, false otherwise.
	  */
    private boolean solve(int cell) {
		logger.debug("solve with " + cell);
			// add this cell to the solution.  If it winds up that this cell is not part 
			// of the final solution, we will remove it later.
		getSolution().add(new Integer(cell));
			// if we are on the last cell, we are done.
		if (cell == getTotalCells() - 1) {
			return true;
		}

			// add this cell to those we have visited
		getSolveVisited().add(new Integer(cell));

			// recursively keep right, left, below and above.
		if (
			(hasAboveNeighbor(cell) && solve(cell - getColumns())) ||
			(hasLeftNeighbor(cell) && solve(cell - 1)) ||
			(hasBelowNeighbor(cell) && solve(cell + getColumns())) || 
			(hasRightNeighbor(cell) && solve(cell + 1)) 
			) {
			logger.debug("solve is true with " + cell);
			return true;
		}
		logger.debug(cell + " is not a solution");
			// no solution via this cell, remove from solution
		getSolution().remove(new Integer(cell));
		return false;
	}
}
