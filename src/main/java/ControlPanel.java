import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.apache.log4j.*;

/**
  * A control panel that hosts components used by the MazePanel.
  * @author Mike D'Orazio
  */
public class ControlPanel
	extends Box {

		/** JPanel that hosts "generate" related components. */
	JPanel generatePanel;
		/** JPanel that hosts "solve" related components. */
	JPanel solvePanel;
		/** JPanel that hosts the GUI representation of the Maze */
	MazePanel mazePanel;
	JButton animate;

	/**
	  * Constructs the Box that will host the components that control the Maze.
	  * @param mazePanel the component that will repsond to events from components in this Box.
	  */
	public ControlPanel(MazePanel mazePanel) {
		super(BoxLayout.Y_AXIS);
		this.mazePanel = mazePanel;
		buildGeneratePanel();
		buildSolvePanel();
		add(generatePanel);
		add(solvePanel);

		JButton print = new JButton("Print");
		print.addActionListener(mazePanel);
		add(print);
		add(Box.createVerticalGlue());
	}

	/**
	  * Builds the JPanel that hosts "generate" related components.
	  */
	public void buildGeneratePanel() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		generatePanel = new JPanel(gbl);
		
		gbc.fill = GridBagConstraints.BOTH;
		generatePanel.setBorder(new TitledBorder("Generate"));

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JLabel label = new JLabel("Grid Size:");
		generatePanel.add(label, gbc);

		JComboBox box = new JComboBox();
		for (int i = 0; i < MazeFrame.GRID_SIZES.length; i++) {
			box.addItem(String.valueOf(MazeFrame.GRID_SIZES[i]));
		}
		box.setSelectedItem(String.valueOf(MazeFrame.ROWS));

		box.addActionListener(mazePanel);
		box.setActionCommand("GridSize");
		generatePanel.add(box, gbc);

		JCheckBox animate = new JCheckBox("Animate");
		animate.addActionListener(mazePanel);
		animate.setActionCommand("gAnimate");
		generatePanel.add(animate, gbc);

		DefaultBoundedRangeModel dbrm = new DefaultBoundedRangeModel();
		dbrm.setValue(250);
		dbrm.setExtent(50);
		dbrm.setMinimum(0);
		dbrm.setMaximum(1000);
		final JSlider slider = new JSlider(dbrm);
		slider.setMajorTickSpacing(250);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				mazePanel.setGenerateSleepValue(slider.getValue());
			}
		});
		generatePanel.add(slider, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JButton redraw = new JButton("Generate");
		redraw.addActionListener(mazePanel);
		generatePanel.add(redraw, gbc);

	}

	/**
	  * Builds the JPanel that hosts "solve" related components.
	  */
	public void buildSolvePanel() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		solvePanel = new JPanel(gbl);
		solvePanel.setBorder(new TitledBorder("Solve"));

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JLabel label = new JLabel("Animation Speed:");
		solvePanel.add(label, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		final JSlider slider = new JSlider(new DefaultBoundedRangeModel(250, 50, 0, 1000));
		slider.setMajorTickSpacing(250);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				mazePanel.setSolveSleepValue(slider.getValue());
			}
		});
		solvePanel.add(slider, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		animate = new JButton("Solve Animated");
		animate.setEnabled(false);
		animate.addActionListener(mazePanel);
		animate.setActionCommand("sAnimate");
		solvePanel.add(animate, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JButton clear = new JButton("Clear");
		clear.addActionListener(mazePanel);
		solvePanel.add(clear, gbc);	

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JButton solve = new JButton("Solve");
		solve.addActionListener(mazePanel);
		solvePanel.add(solve, gbc);
	}

	public void setAnimateEnabled(boolean b) {
		animate.setEnabled(b);
	}
}
