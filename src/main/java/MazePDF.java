import java.awt.*;
import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/*
 * A class that can generate the PDF equivalent of a Maze.
 * @author Mike D'Orazio
 */
public class MazePDF {
		// logger
	public static Logger logger = LogManager.getLogger(MazePDF.class);
	Maze maze;
	FileOutputStream fos;
	public static final String FILENAME = "c:/Personal/01164926/src/java/apps/maze/maze.pdf";
	//public static final String FILENAME = "maze.pdf";

	public MazePDF() {
	}

	public MazePDF(Maze maze) {
		setMaze(maze);
	}

	public void setMaze(Maze maze) {
		this.maze = maze;
		initialize();
	}

	public static void main(String[] args) {
		int sides = 32;
		if (args.length > 0) sides = Integer.parseInt(args[0]);

		Maze maze = new Maze(sides, sides);
		MazePDF pdf = new MazePDF(maze);
	}

	private void initialize() {
			// step 1: creation of a document-object
		Document.compress = false;
		Document document = new Document(PageSize.A4);
			//595 by 842
		float width = document.getPageSize().getWidth();
		float height = document.getPageSize().getHeight();

		try {
				logger.error("about to open file");
			fos = new FileOutputStream(FILENAME);
				logger.error("finished opening file");
				// step 2: we create a writer that listens to the document 
				// document, and directs a PDF-stream to a file 
			PdfWriter writer = PdfWriter.getInstance(document, fos);

				//step 3: we open the document
			document.open();
	
				// step 4: we add a paragraph to the document
			PdfContentByte cb = writer.getDirectContent();
				// flip orientation so (0,0) is in upper left
			cb.concatCTM(1f, 0f, 0f, -1f, 0f, PageSize.A4.getHeight());
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, 
									BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

				// get width and height of a cell in the Maze
			float cellWidth = width / maze.getRows();
			float cellHeight = height / maze.getColumns();

				// draw a Horizontal Template
			PdfTemplate hTemplate = cb.createTemplate(cellWidth, cellHeight);
			hTemplate.moveTo(0,0);
			hTemplate.lineTo(cellWidth, 0);
			hTemplate.stroke();
				// draw a Vertical Template
			PdfTemplate vTemplate = cb.createTemplate(cellWidth, cellHeight);
			vTemplate.moveTo(cellWidth,0);
			vTemplate.lineTo(cellWidth, cellHeight);
			vTemplate.stroke();

			cb.setLineWidth(.50f);
			cb.setColorStroke(Color.black);

				// draw outer box
			cb.moveTo(0, 0);
			cb.lineTo(width, 0);
			cb.lineTo(width, height - cellHeight);
			cb.moveTo(width, height);
			cb.lineTo(0, height);
			cb.lineTo(0, cellHeight);
			cb.stroke();

			java.util.List walls = maze.getWalls();
			for (Iterator iterator = walls.iterator(); iterator.hasNext();) {
				Point p = (Point)iterator.next();
				int x = (int)p.getX(); int y = (int)p.getY();
				if (maze.isBelow(x, y)) {
					//System.err.println(y + " is below " + x);
					cb.addTemplate(hTemplate, maze.getColumnOfCell(x) * cellWidth,
											maze.getRowOfCell(y) * cellHeight);
				}
				if (maze.isRightOf(x, y)) {
					//System.err.println(y + " is right of " + x);
					cb.addTemplate(vTemplate, maze.getColumnOfCell(x) * cellWidth,
											maze.getRowOfCell(y) * cellHeight);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
			// step 5: we close the document
		document.close();
	}

	public FileOutputStream getFileOutputStream() {
		return fos;
	}
}

