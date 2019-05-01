import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Window extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final int CELL_SIZE = 32;
	
	private JTextField textField = new JTextField("level");
	
	private static final String INITIALIZE = "Initialize";
	private static final String GO_UP = "Go Up";
	private static final String GO_DOWN = "Go Down";
	private static final String SET_MONSTERS = "Set Monsters";
	private static final String SET_TRAPS = "Set Traps";
	private static final String SET_LOOT = "Set Loot";
	
	private static final JButton initialize = new JButton(INITIALIZE);
	private static final JButton goUp = new JButton(GO_UP);
	private static final JButton goDown = new JButton(GO_DOWN);
	private static final JButton setMonsters = new JButton(SET_MONSTERS);
	private static final JButton setTraps = new JButton(SET_TRAPS);
	private static final JButton setLoot = new JButton(SET_LOOT);
	
	private static final Font font = new Font("Arial", Font.PLAIN, 32);
	
	private static final Color BROWN = new Color(169, 99, 49);
	private static final Color TAN = new Color(222, 184, 135);
	private static final Color LIGHT_BLUE = new Color(135, 206, 250);
	
	private int level = 1, previousLevel;
	
	private int[][] previousStairLocations = new int[2][2];
	private boolean rejectPrevStairSpots = true;
	
	private static boolean debugMode = false;
	private static boolean redraw = true;
	
	private FloorCell[][] floor;

	public Window()
	{
		textField.setFont(font);
		textField.setHorizontalAlignment(JTextField.CENTER);
		this.add(textField);
		addButton(initialize);
		addButton(goUp);
		addButton(goDown);
		addButton(setMonsters);
		addButton(setTraps);
		addButton(setLoot);
	}
	
	private void addButton (JButton button)
	{
		button.setFont(font);
		button.setActionCommand(button.getText());
		button.addActionListener(this);
		this.add(button);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Dimension d = this.getSize();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, d.width, d.height);
		drawArray(caveFloor(), d, g);
	}
	
	public void drawArray(FloorCell[][] array, Dimension dim, Graphics g)
	{
		int width = array.length * CELL_SIZE, height = array[0].length * CELL_SIZE;
		
		for (int i = 0; i < array.length; i++)
			for (int j = 0; j < array[i].length; j++)
			{
				
				
				if (array[i][j] == null)
					array[i][j] = new FloorCell(CellType.ROCK);
				
				switch (array[i][j].type)
				{
					case CELL:
						g.setColor(Color.BLACK);
						g.drawRect((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
						
						g.setColor(Color.WHITE);
						break;
					case STAIR_UP:
						g.setColor(Color.BLACK);
						g.drawRect((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
						
						g.setColor(LIGHT_BLUE);
						break;
					case STAIR_DOWN:
						g.setColor(Color.BLACK);
						g.drawRect((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
						
						g.setColor(Color.BLUE);
						break;
					case ROCK:
						if (debugMode)
						{
							g.setColor(Color.BLACK);
							g.drawRect((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
						}
						else
							g.setColor(Color.WHITE);
						break;
				}
				
				g.fillRect((dim.width - width) / 2 + i * CELL_SIZE + CELL_SIZE / 4, (dim.height - height) / 2 + j * CELL_SIZE + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
				
				if (array[i][j].north.hasDoor())
				{
					g.setColor(BROWN);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.NORTH, g);
					
					if (array[i][j].north.door.isSecret)
						drawSecret((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.NORTH, g);
					if (array[i][j].north.door.isBarred)
						drawBarred((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.NORTH, g);
					if (array[i][j].north.door.isLocked)
						drawLocked((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.NORTH, g);
				}
				else if (array[i][j].north.hasWall)
				{
					g.setColor(Color.LIGHT_GRAY);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.NORTH, g);
				}
				
				if (array[i][j].south.hasDoor())
				{
					g.setColor(BROWN);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.SOUTH, g);
					
					if (array[i][j].south.door.isSecret)
						drawSecret((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.SOUTH, g);
					if (array[i][j].south.door.isBarred)
						drawBarred((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.SOUTH, g);
					if (array[i][j].south.door.isLocked)
						drawLocked((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.SOUTH, g);
				}
				else if (array[i][j].south.hasWall)
				{
					g.setColor(Color.LIGHT_GRAY);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.SOUTH, g);
				}
				
				if (array[i][j].east.hasDoor())
				{
					g.setColor(BROWN);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.EAST, g);
					
					if (array[i][j].east.door.isSecret)
						drawSecret((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.EAST, g);
					if (array[i][j].east.door.isBarred)
						drawBarred((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.EAST, g);
					if (array[i][j].east.door.isLocked)
						drawLocked((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.EAST, g);
				}
				else if (array[i][j].east.hasWall)
				{
					g.setColor(Color.LIGHT_GRAY);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.EAST, g);
				}
				
				if (array[i][j].west.hasDoor())
				{
					g.setColor(BROWN);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.WEST, g);
					
					if (array[i][j].west.door.isSecret)
						drawSecret((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.WEST, g);
					if (array[i][j].west.door.isBarred)
						drawBarred((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.WEST, g);
					if (array[i][j].west.door.isLocked)
						drawLocked((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.WEST, g);
				}
				else if (array[i][j].west.hasWall)
				{
					g.setColor(Color.LIGHT_GRAY);
					drawTrap((dim.width - width) / 2 + i * CELL_SIZE, (dim.height - height) / 2 + j * CELL_SIZE, Direction.WEST, g);
				}
			}
	}
	
	private void drawTrap(int x, int y, Direction dir, Graphics g)
	{
		switch (dir)
		{
			case NORTH:
				g.fillPolygon(new int[]{x, x + CELL_SIZE / 4, x + (CELL_SIZE * 3) / 4, x + CELL_SIZE},
						new int[]{y, y + CELL_SIZE / 4, y + CELL_SIZE / 4, y}, 4);
				break;
			case SOUTH:
				g.fillPolygon(new int[]{x, x + CELL_SIZE / 4, x + (CELL_SIZE * 3) / 4, x + CELL_SIZE},
						new int[]{y + CELL_SIZE, y + (CELL_SIZE * 3) / 4, y + (CELL_SIZE * 3) / 4, y + CELL_SIZE}, 4);
				break;
			case EAST:
				g.fillPolygon(new int[]{x + CELL_SIZE, x + (CELL_SIZE * 3) / 4, x + (CELL_SIZE * 3) / 4, x + CELL_SIZE},
						new int[]{y, y + CELL_SIZE / 4, y + (CELL_SIZE * 3) / 4, y + CELL_SIZE}, 4);
				break;
			case WEST:
				g.fillPolygon(new int[]{x, x + CELL_SIZE / 4, x + CELL_SIZE / 4, x},
						new int[]{y, y + CELL_SIZE / 4, y + (CELL_SIZE * 3) / 4, y + CELL_SIZE}, 4);
				break;
		}
	}
	
	private void drawSecret(int x, int y, Direction dir, Graphics g)
	{
		g.setColor(TAN);
		int width = 0, height = 0;
		
		switch (dir)
		{
			case NORTH:
				x += CELL_SIZE / 4;
				width = CELL_SIZE / 2;
				height = CELL_SIZE / 4;
				break;
			case SOUTH:
				x += CELL_SIZE / 4;
				y += (CELL_SIZE * 3) / 4;
				width = CELL_SIZE / 2;
				height = CELL_SIZE / 4;
				break;
			case EAST:
				x += (CELL_SIZE * 3) / 4;
				y += CELL_SIZE / 4;
				height = CELL_SIZE / 2;
				width= CELL_SIZE / 4;
				break;
			case WEST:
				y += CELL_SIZE / 4;
				height = CELL_SIZE / 2;
				width = CELL_SIZE / 4;
				break;
		}
		
		g.fillRect(x, y, width, height);
	}
	
	private void drawBarred(int x, int y, Direction dir, Graphics g)
	{
		g.setColor(Color.GRAY);
		
		switch (dir)
		{
			case NORTH:
				g.fillPolygon(new int[]{x, x + CELL_SIZE / 4, x + CELL_SIZE / 4},
						new int[]{y, y, y + CELL_SIZE / 4}, 3);
				g.fillPolygon(new int[]{x + CELL_SIZE, x + (CELL_SIZE * 3) / 4, x + (CELL_SIZE * 3) / 4},
						new int[]{y, y, y + CELL_SIZE / 4}, 3);
				break;
			case SOUTH:
				g.fillPolygon(new int[]{x, x + CELL_SIZE / 4, x + CELL_SIZE / 4},
						new int[]{y + CELL_SIZE, y + CELL_SIZE, y + (CELL_SIZE * 3) / 4}, 3);
				g.fillPolygon(new int[]{x + CELL_SIZE, x + (CELL_SIZE * 3) / 4, x + (CELL_SIZE * 3) / 4},
						new int[]{y + CELL_SIZE, y + CELL_SIZE, y + (CELL_SIZE * 3) / 4}, 3);
				break;
			case EAST:
				g.fillPolygon(new int[]{x + CELL_SIZE, x + CELL_SIZE, x + (CELL_SIZE * 3) / 4},
						new int[]{y, y + CELL_SIZE / 4, y + CELL_SIZE / 4}, 3);
				g.fillPolygon(new int[]{x + CELL_SIZE, x + CELL_SIZE, x + (CELL_SIZE * 3) / 4},
						new int[]{y + CELL_SIZE, y + (CELL_SIZE * 3) / 4, y + (CELL_SIZE * 3) / 4}, 3);
				break;
			case WEST:
				g.fillPolygon(new int[]{x, x, x + CELL_SIZE / 4},
						new int[]{y, y + CELL_SIZE / 4, y + CELL_SIZE / 4}, 3);
				g.fillPolygon(new int[]{x, x, x + CELL_SIZE / 4},
						new int[]{y + CELL_SIZE, y + (CELL_SIZE * 3) / 4, y + (CELL_SIZE * 3) / 4}, 3);
				break;
		}
	}
	
	private void drawLocked(int x, int y, Direction dir, Graphics g)
	{
		g.setColor(Color.BLACK);
		int width = 0, height = 0;
		
		switch (dir)
		{
			case NORTH:
				x += (CELL_SIZE * 3) / 8;
				y += CELL_SIZE / 16;
				width = CELL_SIZE / 4;
				height = CELL_SIZE / 8;
				break;
			case SOUTH:
				x += (CELL_SIZE * 3) / 8;
				y += (CELL_SIZE * 13) / 16;
				width = CELL_SIZE / 4;
				height = CELL_SIZE / 8;
				break;
			case EAST:
				x += (CELL_SIZE * 13) / 16;
				y += (CELL_SIZE * 3) / 8;
				width = CELL_SIZE / 8;
				height = CELL_SIZE / 4;
				break;
			case WEST:
				x += CELL_SIZE / 16;
				y += (CELL_SIZE * 3) / 8;
				width = CELL_SIZE / 8;
				height = CELL_SIZE / 4;
				break;
		}
		
		g.fillRect(x, y, width, height);
	}
	
	public FloorCell[][] caveFloor()
	{
		if (redraw)
		{
			floor = new FloorCell[level + 6][level + 6];
			
			for (int i = 0; i < floor.length; i++)
				for (int j = 0; j < floor[0].length; j++)
					floor[i][j] = new FloorCell(CellType.ROCK);
			
			int[][] stairSpots = Components.stairSpots(previousStairLocations, previousLevel < level, rejectPrevStairSpots);
			
			previousStairLocations[0][0] = stairSpots[0][0];
			previousStairLocations[0][1] = stairSpots[0][1];
			previousStairLocations[1][0] = stairSpots[1][0];
			previousStairLocations[1][1] = stairSpots[1][1];
			
			stairSpots[0][0] *= floor.length - 1;
			stairSpots[1][0] *= floor.length - 1;
			stairSpots[0][1] *= floor[0].length - 1;
			stairSpots[1][1] *= floor[0].length - 1;
			
			floor[stairSpots[0][0]][stairSpots[0][1]].type = CellType.STAIR_UP;
			floor[stairSpots[1][0]][stairSpots[1][1]].type = CellType.STAIR_DOWN;
			
			//----------------------------------------------------------------
			
			floor = Components.initialize(floor, stairSpots);
			
			//----------------------------------------------------------------
			
			fixWalls(floor);
			fixDoors(floor);
			
			redraw = false;
		}
		
		return floor;
	}
	
	public static void fixDoors(FloorCell[][] floor)
	{
		for (int i = 0; i < floor.length; i++)
			for (int j = 0; j < floor[0].length; j++)
			{
				if ((floor[i][j].type == CellType.STAIR_UP || floor[i][j].type == CellType.STAIR_DOWN) && floor[i][j].allSidesWall())
				{
					floor[i][j].north.door = new Door();
					floor[i][j].south.door = new Door();
					floor[i][j].east.door = new Door();
					floor[i][j].west.door = new Door();
				}
				
				if (j > 0)
				{
					if (floor[i][j].north.hasDoor())
					{
						if (floor[i][j - 1].type == CellType.ROCK)
							floor[i][j].north.door = null;
						else
							floor[i][j - 1].south.door = floor[i][j].north.door;
					}
					if (floor[i][j].north.hasWall && floor[i][j - 1].type != CellType.ROCK)
						floor[i][j - 1].south.hasWall = true;
				}
				else if (floor[i][j].north.hasDoor())
						floor[i][j].north.door = null;
				
				if (j < floor.length - 1)
				{
					if (floor[i][j].south.hasDoor())
					{
						if (floor[i][j + 1].type == CellType.ROCK)
							floor[i][j].south.door = null;
						else
							floor[i][j + 1].north.door = floor[i][j].south.door;
					}
					if (floor[i][j].south.hasWall && floor[i][j + 1].type != CellType.ROCK)
						floor[i][j + 1].north.hasWall = true;
				}
				else if (floor[i][j].south.hasDoor())
						floor[i][j].south.door = null;
				
				if (i > 0)
				{
					if (floor[i][j].west.hasDoor())
					{
						if (floor[i - 1][j].type == CellType.ROCK)
							floor[i][j].west.door = null;
						else
							floor[i - 1][j].east.door = floor[i][j].west.door;
					}
					if (floor[i][j].west.hasWall && floor[i - 1][j].type != CellType.ROCK)
						floor[i - 1][j].east.hasWall = true;
				}
				else if (floor[i][j].west.hasDoor())
					floor[i][j].west.door = null;
				
				if (i < floor.length - 1)
				{
					if (floor[i][j].east.hasDoor())
					{
						if (floor[i + 1][j].type == CellType.ROCK)
							floor[i][j].east.door = null;
						else
							floor[i + 1][j].west.door = floor[i][j].east.door;
					}
					if (floor[i][j].east.hasWall && floor[i + 1][j].type != CellType.ROCK)
						floor[i + 1][j].west.hasWall = true;
				}
				else if (floor[i][j].east.hasDoor())
					floor[i][j].east.door = null;
			}
	}
	
	public static void fixWalls(FloorCell[][] floor)
	{
		for (int i = 0; i < floor.length; i++)
		{
			if (debugMode || floor[i][0].type != CellType.ROCK)
				floor[i][0].north.hasWall = true;
			else
				floor[i][0].north.hasWall = false;
			floor[i][0].north.door = null;
			if (debugMode || floor[i][floor[0].length - 1].type != CellType.ROCK)
				floor[i][floor[0].length - 1].south.hasWall = true;
			else
				floor[i][floor[0].length - 1].south.hasWall = false;
			floor[i][floor[0].length - 1].south.door = null;
		}
		
		for (int i = 0; i < floor[0].length; i++)
		{
			if (debugMode || floor[0][i].type != CellType.ROCK)
				floor[0][i].west.hasWall = true;
			else
				floor[0][i].west.hasWall = false;
			floor[0][i].west.door = null;
			if (debugMode || floor[floor.length - 1][i].type != CellType.ROCK)
				floor[floor.length - 1][i].east.hasWall = true;
			else
				floor[floor.length - 1][i].east.hasWall = false;
			floor[floor.length - 1][i].east.door = null;
		}
	}

	public static FloorCell[][] arraySum(int xOffSet, int yOffSet, FloorCell[][] larger, FloorCell[][] smaller)
	{
		if (larger != null && smaller != null && larger.length >= smaller.length + xOffSet)
			if (larger[0].length >= smaller[0].length + yOffSet)
				for (int i = 0; i < smaller.length; i++)
					for (int j = 0; j < smaller[i].length; j++)
						if (smaller[i][j] != null)
						{
							if (larger[i + xOffSet][j + yOffSet] != null)
								if (larger[i + xOffSet][j + yOffSet].type == CellType.STAIR_UP || larger[i + xOffSet][j + yOffSet].type == CellType.STAIR_DOWN)
									smaller[i][j].type = larger[i + xOffSet][j + yOffSet].type;
							
							larger[i + xOffSet][j + yOffSet] = new FloorCell(smaller[i][j]);
						}
		
		return larger;
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getActionCommand().equals(initialize.getActionCommand()))
		{
			Components.reset();
			previousLevel = level;
			
			if (textField.getText().equals("level"))
				textField.setText(Integer.toString(level));
			
			level = Integer.parseInt(textField.getText());
			textField.setText(Integer.toString(level));
			rejectPrevStairSpots = true;
			redraw = true;
			repaint();
		}
		else if (arg0.getActionCommand().equals(goUp.getActionCommand()))
		{
			Components.reset();
			previousLevel = level;
			level--;
			textField.setText(Integer.toString(level));
			rejectPrevStairSpots = false;
			redraw = true;
			repaint();
		}
		else if (arg0.getActionCommand().equals(goDown.getActionCommand()))
		{
			Components.reset();
			previousLevel = level;
			level++;
			textField.setText(Integer.toString(level));
			rejectPrevStairSpots = false;
			redraw = true;
			repaint();
		}
		else if (arg0.getActionCommand().equals(setTraps.getActionCommand()))
		{			
			int numTraps;
			
			if (level < 8)
			{
				numTraps = (Components.rng.nextInt(4) + 1) / 2 - 1;
			}
			else if (level < 12)
			{
				numTraps = (Components.rng.nextInt(6) + 1) / 2 - 1;
			}
			else if (level < 16)
			{
				numTraps = (Components.rng.nextInt(8) + 1) / 2 - 1;
			}
			else
			{
				numTraps = (Components.rng.nextInt(10) + 1) / 2 - 1;
			}
			
			System.out.println("Number of Traps: " + (int) Math.max(numTraps, 0));
			
			if (numTraps > 0)
			{
				int attribute;
				
				for (int i = 0; i < numTraps; i++)
				{
					System.out.println("Trap " + (i + 1) + ":");
					
					attribute = Components.rng.nextInt(6);
					
					switch (attribute)
					{
						case 0:
							System.out.println("Purpose: Alarm");
							break;
						case 1:
							System.out.println("Purpose: Delay");
							break;
						case 2:
							System.out.println("Purpose: Restrain");
							break;
						case 3:
						case 4:
						case 5:
							System.out.println("Purpose: Slay");
					}
					
					attribute = Components.rng.nextInt(20);
					
					switch (attribute)
					{
						case 0:
						case 1:
							System.out.println("Trigger: Pressure Plate");
							break;
						case 2:
						case 3:
							System.out.println("Trigger: Trip Wire");
							break;
						case 4:
							System.out.println("Trigger: Chest Opened");
							break;
						case 5:
							System.out.println("Trigger: Chest Closed");
							break;
						case 6:
							System.out.println("Trigger: Door Opened");
							break;
						case 7:
							System.out.println("Trigger: Door Closed");
							break;
						case 8:
							System.out.println("Trigger: Doorknob Turned Left");
							break;
						case 9:
							System.out.println("Trigger: Doorknob Turned Right");
							break;
						case 10:
							System.out.println("Trigger: Zone Entered");
							break;
						case 11:
							System.out.println("Trigger: Zone Left");
							break;
						case 12:
						case 13:
							System.out.println("Trigger: Object Moved");
							break;
						case 14:
						case 15:
							System.out.println("Trigger: Lever Flipped");
							break;
						case 16:
						case 17:
							System.out.println("Trigger: Button Pressed");
							break;
						case 18:
							System.out.println("Trigger: Bright Light");
							break;
						case 19:
							System.out.println("Trigger: Loud Sound");
							break;
					}
					
					attribute = Components.rng.nextInt(4);
					
					if (attribute == 0)
						System.out.println("Severity: Dangerous");
					else
						System.out.println("Severity: Moderate");
				}
			}
		}
		else if (arg0.getActionCommand().equals(setMonsters.getActionCommand()))
		{
			
		}
		else if (arg0.getActionCommand().equals(setLoot.getActionCommand()))
		{
			
		}
	}
}
