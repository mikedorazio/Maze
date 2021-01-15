import java.awt.*;
import javax.swing.*;
import org.apache.logging.log4j.*;

/**
  * The JFrame that hosts all components for a Maze.
  * @author Mike D'Orazio
  */
public class MazeFrame
	extends JFrame {

	public static Logger logger = LogManager.getLogger(MazeFrame.class);
		/** grid sizes the user can choose */
	public static final int[] GRID_SIZES = {3, 4, 8, 16, 28, 32, 36, 40, 44, 50, 75, 90, 120, 150, 200};
	public static final String TITLE = "Mike's Maze Frame";
	public static final int ROWS = 4, COLS = 4;
		/** the JPanel that will host the Maze */
	MazePanel mazePanel;
	
	/**
	  * Constructs the JFrame that will host all components of the Maze.
	  * @param title the title for this Maze.
	  * @param rows the number of rows for this Maze
	  * @param cols the number of columns for this Maze
	  */
	public MazeFrame(String title, int rows, int cols) {
		super();
		setTitle(TITLE, rows, cols);
		getContentPane().setLayout(new BorderLayout());
		mazePanel = new MazePanel(this, rows, cols);
		setBackground(Color.white);
		getContentPane().add(mazePanel, BorderLayout.CENTER);

		ControlPanel control = new ControlPanel(mazePanel);
		mazePanel.setControlPanel(control);
		getContentPane().add(control, BorderLayout.EAST);

		JPanel north = new JPanel();
		north.add(new JLabel(" "));
		getContentPane().add(north, BorderLayout.NORTH);
		JPanel west = new JPanel();
		west.add(new JLabel(" "));
		getContentPane().add(west, BorderLayout.WEST);
	}

	/**
	 * Sets the title of this Maze
	 * @param title the title of this Maze
	 * @param rows the number of rows for this Maze
	 * @param cols the number of columns for this Maze
	 */
	public void setTitle(String title, int rows, int cols) {
		super.setTitle(title + " size - " + rows + " x " + cols);
	}

	/**
	  * The main() entry point of this program.
	  * @param args the array of command line arguments given
	  */
	public static void main(String[] args) {
		MazeFrame mf = null;
		switch (args.length) {
			case 0 : 
				mf = new MazeFrame("Mike's Maze Frame", ROWS, COLS);
				break;
			case 1 : 
				mf = new MazeFrame("Mike's Maze Frame",
									Integer.parseInt(args[0]), Integer.parseInt(args[0]));
				break;
			case 2 : 
				mf = new MazeFrame("Mike's Maze Frame",
									Integer.parseInt(args[0]), Integer.parseInt(args[1]));
				break;
		}

		mf.setSize(700, 700);
		mf.setVisible(true);
		mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
