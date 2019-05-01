import java.util.Random;

public abstract class Components
{
	public static Random rng = new Random();
	
	public static int[] doorCoord = new int[2];
	public static Direction nextDirect;
	public static boolean addNextDoor = true;
	
	public static FloorCell[][] layout;
	
	public static int[][] stairSpots(int[][] previousSpots, boolean goingDown, boolean rejectPrevSpots)
	{
		int[][] stairSpots = new int[2][2];
		
		/* If this is the first level, generate an up stair (stairSpots[0]) and
		 * a down stair (stairSpots[1]) in different random locations. The second
		 * component to the array is the x (stairSpots[n][0]) and
		 * y values (stairSpots[n][1]) of the stair location. If the value is 0,
		 * then it is on the West/North side of the coordinate plane.
		 * If the value is 1, then it is on the East/South side of the coordinate plane.
		 */
		if (rejectPrevSpots)
			while (stairSpots[0][0] == stairSpots[1][0] && stairSpots[0][1] == stairSpots[1][1])
			{
				stairSpots[0][0] = rng.nextInt(2);
				stairSpots[0][1] = rng.nextInt(2);
				stairSpots[1][0] = rng.nextInt(2);
				stairSpots[1][1] = rng.nextInt(2);
			}
		else if(goingDown)
		{
			System.out.println("Changing Down Stair at: " + previousSpots[1][0] + " " + previousSpots[1][1] + " into an Up Stair." );
			
			if (previousSpots[1][0] == 0)
				stairSpots[0][0] = 0;
			else
				stairSpots[0][0] = 1;
			
			if (previousSpots[1][1] == 0)
				stairSpots[0][1] = 0;
			else
				stairSpots[0][1] = 1;
			
			stairSpots[1][0] = rng.nextInt(2);
			stairSpots[1][1] = rng.nextInt(2);
			
			while (stairSpots[0][0] == stairSpots[1][0] && stairSpots[0][1] == stairSpots[1][1])
			{
				stairSpots[1][0] = rng.nextInt(2);
				stairSpots[1][1] = rng.nextInt(2);
			}
		}
		else
		{
			System.out.println("Changing Up Stair at: " + previousSpots[0][0] + " " + previousSpots[0][1] + " into a Down Stair." );
			
			if (previousSpots[0][0] == 0)
				stairSpots[1][0] = 0;
			else
				stairSpots[1][0] = 1;
			
			if (previousSpots[0][1] == 0)
				stairSpots[1][1] = 0;
			else
				stairSpots[1][1] = 1;
			
			stairSpots[0][0] = rng.nextInt(2);
			stairSpots[0][1] = rng.nextInt(2);
			
			while (stairSpots[0][0] == stairSpots[1][0] && stairSpots[0][1] == stairSpots[1][1])
			{
				stairSpots[0][0] = rng.nextInt(2);
				stairSpots[0][1] = rng.nextInt(2);
			}
		}
		
		return stairSpots;
	}
	
	public static boolean mayAddDoor()
	{
		if (rng.nextInt(4) == 0 && addNextDoor)
			return true;
		return false;
	}

	public static FloorCell[][] initialize(FloorCell[][] floor, int[][] stairCoords)
	{
		layout = new FloorCell[floor.length][floor[0].length];
		layout = Window.arraySum(0, 0, layout, floor);
		
		doorCoord = stairCoords[0];
		
		System.out.println("Up Stair Coordinates: " + stairCoords[0][0] + " " + stairCoords[0][1]
				+ "\nDown Stair Coordinates: " + stairCoords[1][0] + " " + stairCoords[1][1]
						+ "\nStarting Door Coordinates: " + doorCoord[0] + " " + doorCoord[1]);
		
		boolean cavernNext = true;
		
		while (true)
		{
			System.out.println("Initializing Next Item");
			
			cavernNext = rng.nextInt(4) != 0;
			
			if (cavernNext)
			{
				if (!generateCavern())
					if (!generateCorridor())
						break;
			}
			else if (!generateCorridor())
				break;
		}
		
		System.out.println("Initialization Complete.\nFixing Walls and Doors.");
		
		Window.fixWalls(floor);
		Window.fixDoors(layout);
		
		System.out.println("Walls and Doors Fixed.\nFixing Down Stair.");
		
		fixDownStair(stairCoords[1]);
		
		System.out.println("Down Stair Fixed.\nRe-Fixing Walls and Doors.");
		
		Window.fixWalls(floor);
		Window.fixDoors(layout);
		
		System.out.println("Post-Initialization Complete.");
		
		return layout;
	}
	
	public static boolean generateCavern()
	{
		int[] maxSize = cavernMaxSize(doorCoord[0], doorCoord[1], nextDirect, layout);
		
		Door newDoor;
		
		if (maxSize[0] < 3 || maxSize[1] < 3)
			return false;
		
		addNextDoor = true;
		
		Cavern cavern = new Cavern(maxSize[0], maxSize[1], false);
		int minOffSet = -1, maxOffSet = -1, offSet = -1, x = 0, y = 0;
		boolean rowFits = false;
		
		System.out.println("Actual Size: " + cavern.width + " " + cavern.height);
		
		if (nextDirect == null)
		{
			x = Math.max(0, doorCoord[0] - cavern.width + 1);
			y = Math.max(0, doorCoord[1] - cavern.height + 1);
		}
		else
		{
			switch (nextDirect)
			{
				case NORTH:
					for (int i = doorCoord[0]; i > doorCoord[0] - cavern.width && i >= 0; i--)
					{
						rowFits = true;
						
						for (int j = doorCoord[1] - 1; j >= doorCoord[1] - cavern.height; j--)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							minOffSet++;
						else
							break;
					}
					
					for (int i = doorCoord[0]; i < layout.length && i < doorCoord[0] + cavern.width; i++)
					{
						rowFits = true;
						
						for (int j = doorCoord[1] - 1; j >= doorCoord[1] - cavern.height; j--)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							maxOffSet++;
						else
							break;
					}
					
					if (minOffSet + maxOffSet + 1 - cavern.width < 0)
					{
						System.out.println("Resizing Cavern to fit Space.");
						
						cavern = new Cavern(minOffSet + maxOffSet + 1, cavern.height, true);
						
						System.out.println("New Size: " + cavern.width + " " + cavern.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet);
					
					offSet = cavern.width - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - cavern.width);
					
					System.out.println("Actual Offset: " + offSet);
					
					x = doorCoord[0] - offSet;
					y = doorCoord[1] - cavern.height;
										
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].north.door = newDoor;
					cavern.layout[offSet][cavern.height - 1].south.door = newDoor;
					break;
				case SOUTH:
					for (int i = doorCoord[0]; i > doorCoord[0] - cavern.width && i >= 0; i--)
					{
						rowFits = true;
						
						for (int j = doorCoord[1] + 1; j <= doorCoord[1] + cavern.height; j++)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							minOffSet++;
						else
							break;
					}
					
					for (int i = doorCoord[0]; i < doorCoord[0] + cavern.width && i < layout.length; i++)
					{
						rowFits = true;
						
						for (int j = doorCoord[1] + 1; j <= doorCoord[1] + cavern.height; j++)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							maxOffSet++;
						else
							break;
					}
					
					if (minOffSet + maxOffSet + 1 - cavern.width < 0)
					{
						System.out.println("Resizing Cavern to fit Space.");
						
						cavern = new Cavern(minOffSet + maxOffSet + 1, cavern.height, true);
						
						System.out.println("New Size: " + cavern.width + " " + cavern.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet);
					
					offSet = cavern.width - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - cavern.width);
					
					System.out.println("Actual Offset: " + offSet);
					
					x = doorCoord[0] - offSet;
					y = doorCoord[1] + 1;
										
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].south.door = newDoor;
					cavern.layout[offSet][0].north.door = newDoor;
					break;
				case EAST:
					for (int j = doorCoord[1]; j > doorCoord[1] - cavern.height && j >= 0; j--)
					{
						rowFits = true;
						
						for (int i = doorCoord[0] + 1; i <= doorCoord[0] + cavern.width; i++)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							minOffSet++;
						else
							break;
					}
					
					for (int j = doorCoord[1]; j < doorCoord[1] + cavern.height && j < layout[0].length; j++)
					{
						rowFits = true;
						
						for (int i = doorCoord[0] + 1; i <= doorCoord[0] + cavern.width; i++)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							maxOffSet++;
						else
							break;
					}
					
					if (minOffSet + maxOffSet + 1 - cavern.height < 0)
					{
						System.out.println("Resizing Cavern to fit Space.");
						
						cavern = new Cavern(cavern.width, minOffSet + maxOffSet + 1, true);
						
						System.out.println("New Size: " + cavern.width + " " + cavern.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet);
					
					offSet = cavern.height - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - cavern.height);
					
					System.out.println("Actual Offset: " + offSet);
					
					x = doorCoord[0] + 1;
					y = doorCoord[1] - offSet;
										
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].east.door = newDoor;
					cavern.layout[0][offSet].west.door = newDoor;
					break;
				case WEST:
					for (int j = doorCoord[1]; j > doorCoord[1] - cavern.height && j >= 0; j--)
					{
						rowFits = true;
						
						for (int i = doorCoord[0] - 1; i >= doorCoord[0] - cavern.width; i--)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							minOffSet++;
						else
							break;
					}
					
					for (int j = doorCoord[1]; j < doorCoord[1] + cavern.height && j < layout[0].length; j++)
					{
						rowFits = true;
						
						for (int i = doorCoord[0] - 1; i >= doorCoord[0] - cavern.width; i--)
							if (!isSpawnable(layout[i][j]))
							{
								rowFits = false;
								break;
							}
						
						if (rowFits)
							maxOffSet++;
						else
							break;
					}
					
					if (minOffSet + maxOffSet + 1 - cavern.height < 0)
					{
						System.out.println("Resizing Cavern to fit Space.");
						
						cavern = new Cavern(cavern.width, minOffSet + maxOffSet + 1, true);
						
						System.out.println("New Size: " + cavern.width + " " + cavern.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet);
					
					offSet = cavern.height - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - cavern.height);
					
					System.out.println("Actual Offset: " + offSet);
					
					x = doorCoord[0] - cavern.width;
					y = doorCoord[1] - offSet;
										
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].west.door = newDoor;
					cavern.layout[cavern.width - 1][offSet].east.door = newDoor;
					break;
			}
		}
		
		System.out.println("Direction: " + nextDirect + "\nPlacing at: " + x + " " + y);
		
		Window.arraySum(x, y, layout, cavern.layout);
		
		tryAddDoor(x, x + cavern.width, y, Direction.NORTH, cavern, null);
		tryAddDoor(x, x + cavern.width, y + cavern.height - 1, Direction.SOUTH, cavern, null);
		tryAddDoor(y, y + cavern.height, x + cavern.width - 1, Direction.EAST, cavern, null);
		tryAddDoor(y, y + cavern.height, x, Direction.WEST, cavern, null);
		
		forceAddDoor(x, x + cavern.width, y, Direction.NORTH, cavern, null);
		forceAddDoor(x, x + cavern.width, y + cavern.height - 1, Direction.SOUTH, cavern, null);
		forceAddDoor(y, y + cavern.height, x + cavern.width - 1, Direction.EAST, cavern, null);
		forceAddDoor(y, y + cavern.height, x, Direction.WEST, cavern, null);
		return true;
	}
	
	public static boolean generateCorridor()
	{
		int[] maxSize = corridorMaxSize(doorCoord[0], doorCoord[1], nextDirect, layout);
		
		if (maxSize[0] < 3 && maxSize[1] < 3)
			return false;
		
		Door newDoor;
		
		addNextDoor = true;
		
		Corridor corridor = new Corridor(maxSize[0], maxSize[1]);
		int minOffSet = -1, maxOffSet = -1, offSet = 0, x = 0, y = 0;
		
		System.out.println("Actual Size: " + corridor.width + " " + corridor.height);
		
		if (nextDirect == null)
		{
			x = Math.max(0, doorCoord[0] - corridor.width + 1);
			y = Math.max(0, doorCoord[1] - corridor.height + 1);
		}
		else
			switch (nextDirect)
			{
				case NORTH:
					if (!corridor.vertical)
					{
						for (int i = doorCoord[0]; i >= 0 && i > doorCoord[0] - corridor.width; i--)
						{
							if (isSpawnable(layout[i][doorCoord[1] - 1]))
								minOffSet++;
							else
								break;
						}
						
						for (int i = doorCoord[0]; i < layout.length && i < doorCoord[0] + corridor.width; i++)
						{
							if (isSpawnable(layout[i][doorCoord[1] - 1]))
								maxOffSet++;
							else
								break;
						}
						
						offSet = corridor.width - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - corridor.width);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet + "\nActual Offset: " + offSet);
					
					x = doorCoord[0] - offSet;
					y = doorCoord[1] - corridor.height;
					
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].north.door = newDoor;
					corridor.layout[offSet][corridor.height - 1].south.door = newDoor;
					break;
				case SOUTH:
					if (!corridor.vertical)
					{
						for (int i = doorCoord[0]; i >= 0 && i > doorCoord[0] - corridor.width; i--)
						{
							if (isSpawnable(layout[i][doorCoord[1] + 1]))
								minOffSet++;
							else
								break;
						}
						
						for (int i = doorCoord[0]; i < layout.length && i < doorCoord[0] + corridor.width; i++)
						{
							if (isSpawnable(layout[i][doorCoord[1] + 1]))
								maxOffSet++;
							else
								break;
						}
						
						offSet = corridor.width - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - corridor.width);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet + "\nActual Offset: " + offSet);
					
					x = doorCoord[0] - offSet;
					y = doorCoord[1] + 1;
					
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].south.door = newDoor;
					corridor.layout[offSet][0].north.door = newDoor;
					break;
				case EAST:
					if (corridor.vertical)
					{
						for (int j = doorCoord[1]; j >= 0 && j > doorCoord[1] - corridor.height; j--)
						{
							if (isSpawnable(layout[doorCoord[0] + 1][j]))
								minOffSet++;
							else
								break;
						}
						
						for (int j = doorCoord[1]; j < layout[0].length && j < doorCoord[1] + corridor.height; j++)
						{
							if (isSpawnable(layout[doorCoord[0] + 1][j]))
								maxOffSet++;
							else
								break;
						}
						
						offSet = corridor.height - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - corridor.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet + "\nActual Offset: " + offSet);
					
					x = doorCoord[0] + 1;
					y = doorCoord[1] - offSet;
					
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].east.door = newDoor;
					corridor.layout[0][offSet].west.door = newDoor;
					break;
				case WEST:
					if (corridor.vertical)
					{
						for (int j = doorCoord[1]; j >= 0 && j > doorCoord[1] - corridor.height; j--)
						{
							if (isSpawnable(layout[doorCoord[0] - 1][j]))
								minOffSet++;
							else
								break;
						}
						
						for (int j = doorCoord[1]; j < layout[0].length && j < doorCoord[1] + corridor.height; j++)
						{
							if (isSpawnable(layout[doorCoord[0] - 1][j]))
								maxOffSet++;
							else
								break;
						}
						
						offSet = corridor.height - maxOffSet - 1 + rng.nextInt(minOffSet + maxOffSet + 2 - corridor.height);
					}
					
					System.out.println("Max Offset: " + maxOffSet + "\nMin Offset: " + minOffSet + "\nActual Offset: " + offSet);
					
					x = doorCoord[0] - corridor.width;
					y = doorCoord[1] - offSet;
					
					newDoor = new Door();
					layout[doorCoord[0]][doorCoord[1]].west.door = newDoor;
					corridor.layout[corridor.width - 1][offSet].east.door = newDoor;
					break;
			}
		
		System.out.println("Direction: " + nextDirect + "\nPlacing at: " + x + " " + y);
		
		Window.arraySum(x, y, layout, corridor.layout);
		
		tryAddDoor(x, x + corridor.width, y, Direction.NORTH, null, corridor);
		tryAddDoor(x, x + corridor.width, y + corridor.height - 1, Direction.SOUTH, null, corridor);
		tryAddDoor(y, y + corridor.height, x + corridor.width - 1, Direction.EAST, null, corridor);
		tryAddDoor(y, y + corridor.height, x, Direction.WEST, null, corridor);
		
		forceAddDoor(x, x + corridor.width, y, Direction.NORTH, null, corridor);
		forceAddDoor(x, x + corridor.width, y + corridor.height - 1, Direction.SOUTH, null, corridor);
		forceAddDoor(y, y + corridor.height, x + corridor.width - 1, Direction.EAST, null, corridor);
		forceAddDoor(y, y + corridor.height, x, Direction.WEST, null, corridor);
		return true;
	}
	
	public static void fixDownStair(int[] downStairCoords)
	{
		if (downStairCoords[0] == 0)
		{
			if (!isSpawnable(layout[1][downStairCoords[1]]) && (layout[1][downStairCoords[1]].west.hasDoor() || !layout[1][downStairCoords[1]].west.hasWall))
				return;
			
			System.out.println("EAST Side cut off");
		}
		else
		{
			if (!isSpawnable(layout[downStairCoords[0] - 1][downStairCoords[1]]) && (layout[downStairCoords[0] - 1][downStairCoords[1]].east.hasDoor() || !layout[downStairCoords[0] - 1][downStairCoords[1]].east.hasWall))
				return;
			
			System.out.println("WEST Side cut off");
		}
		
		if (downStairCoords[1] == 0)
		{
			if (!isSpawnable(layout[downStairCoords[0]][1]) && (layout[downStairCoords[0]][1].north.hasDoor() || !layout[downStairCoords[0]][1].north.hasWall))
				return;
			
			System.out.println("SOUTH Side cut off");
		}
		else
		{
			if (!isSpawnable(layout[downStairCoords[0]][downStairCoords[1] - 1]) && (layout[downStairCoords[0]][downStairCoords[1] - 1].south.hasDoor() || !layout[downStairCoords[0]][downStairCoords[1] - 1].south.hasWall))
				return;
			
			System.out.println("NORTH Side cut off");
		}
		
		int width = 0, height = 0, x = 0, y = 0;
		boolean rowFits = false;
		Door door = new Door();
		
		if (downStairCoords[0] == 0)
			for (int i = 0; i < layout.length; i++)
			{
				if (isSpawnable(layout[i][downStairCoords[1]]))
					width++;
				else
					break;
			}
		else
			for (int i = downStairCoords[0]; i >= 0; i--)
			{
				if (isSpawnable(layout[i][downStairCoords[1]]))
					width++;
				else
					break;
			}
		
		x = Math.max(0, downStairCoords[0] - width + 1);
		
		if (downStairCoords[1] == 0)
			for (int j = 0; j < layout[0].length; j++)
			{
				rowFits = true;
				
				for (int i = x; i < x + width; i++)
					if (!isSpawnable(layout[i][j]))
					{
						rowFits = false;
						break;
					}
				
				if (rowFits)
					height++;
				else
					break;
			}
		else
			for (int j = downStairCoords[1]; j >= 0; j--)
			{
				rowFits = true;
				
				for (int i = x; i < x + width; i++)
					if (!isSpawnable(layout[i][j]))
					{
						rowFits = false;
						break;
					}
				
				if (rowFits)
					height++;
				else
					break;
			}
		
		y = Math.max(0, downStairCoords[1] - height + 1);
		
		System.out.println("Creating Cavern of size: " + width + " " + height + "\nPlacing at: " + x + " " + y);
		
		Cavern cavern = new Cavern(width, height, true);
		
		Window.arraySum(x, y, layout, cavern.layout);
		
		if (y > 0)
			for (int i = x; i < x + width; i++)
			{
				if (!isSpawnable(layout[i][y - 1]))
				{
					layout[i][y].north.door = door;
					layout[i][y - 1].south.door = door;
					
					System.out.println("Adding NORTH door at: " + i + " " + y);
					
					break;
				}
			}
		else if (y + height < layout[0].length)
			for (int i = x; i < x + width; i++)
			{
				if (!isSpawnable(layout[i][y + height]))
				{
					layout[i][y + height - 1].south.door = door;
					layout[i][y + height].north.door = door;
					
					System.out.println("Adding SOUTH door at: " + i + " " + (y + height - 1));
					
					break;
				}
			}
		else if (x > 0)
			for (int j = y; j < y + height; j++)
			{
				if (!isSpawnable(layout[x - 1][j]))
				{
					layout[x][j].west.door = door;
					layout[x - 1][j].east.door = door;
					
					System.out.println("Adding WEST door at: " + x + " " + j);
					
					break;
				}
			}
		else if (x + width < layout.length)
			for (int j = y; j < y + height; j++)
			{
				if (!isSpawnable(layout[x + width][j]))
				{
					layout[x + width - 1][j].east.door = door;
					layout[x + width][j].west.door = door;
					
					System.out.println("Adding EAST door at: " + (x + width - 1) + " " + j);
					
					break;
				}
			}
	}
	
	public static void tryAddDoor(int start, int end, int oppValue, Direction dir, Cavern cav, Corridor cor)
	{
		if (dir == null)
			return;
		
		int[] maxSize = new int[2];
		
		if (cav != null && cav.canAddDoor(dir) && !cav.hasDoor(dir) && addNextDoor)
		{
			for (int i = start; i < end; i++)
			{
				switch (dir)
				{
					case NORTH:
					case SOUTH:
						maxSize = corridorMaxSize(i, oppValue, dir, layout);
						break;
					case EAST:
					case WEST:
						maxSize = corridorMaxSize(oppValue, i, dir, layout);
						break;
				}
				
				if ((maxSize[0] > 2 || maxSize[1] > 2) && mayAddDoor())
				{
					System.out.println("Adding " + dir + " Door");
					
					switch (dir)
					{
						case NORTH:
						case SOUTH:
							doorCoord[0] = i;
							doorCoord[1] = oppValue;
							break;
						case EAST:
						case WEST:
							doorCoord[0] = oppValue;
							doorCoord[1] = i;
							break;
					}
					
					System.out.println("Door Coordinates: " + doorCoord[0] + " " + doorCoord[1]);
					nextDirect = dir;
					addNextDoor = false;
					break;
				}
			}
		}
		else if (cor != null && cor.canAddDoor(dir) && !cor.hasDoor(dir) && addNextDoor)
		{
			for (int i = start; i < end; i++)
			{
				switch (dir)
				{
					case NORTH:
					case SOUTH:
						maxSize = corridorMaxSize(i, oppValue, dir, layout);
						break;
					case EAST:
					case WEST:
						maxSize = corridorMaxSize(oppValue, i, dir, layout);
						break;
				}
				
				if ((maxSize[0] > 2 || maxSize[1] > 2) && mayAddDoor())
				{
					System.out.println("Adding " + dir + " Door");
					
					switch (dir)
					{
						case NORTH:
						case SOUTH:
							doorCoord[0] = i;
							doorCoord[1] = oppValue;
							break;
						case EAST:
						case WEST:
							doorCoord[0] = oppValue;
							doorCoord[1] = i;
							break;
					}
					
					System.out.println("Door Coordinates: " + doorCoord[0] + " " + doorCoord[1]);
					nextDirect = dir;
					addNextDoor = false;
					break;
				}
			}
		}
	}
	
	public static void forceAddDoor(int start, int end, int oppValue, Direction dir, Cavern cav, Corridor cor)
	{
		if (dir == null)
			return;
		
		int[] maxSize = new int[2];
		
		if (cav != null && cav.canAddDoor(dir) && addNextDoor)
		{
			for (int i = start; i < end; i++)
			{
				switch (dir)
				{
					case NORTH:
					case SOUTH:
						maxSize = corridorMaxSize(i, oppValue, dir, layout);
						break;
					case EAST:
					case WEST:
						maxSize = corridorMaxSize(oppValue, i, dir, layout);
						break;
				}
				
				if (maxSize[0] > 2 || maxSize[1] > 2)
				{
					System.out.println("Adding " + dir + " Door");
					
					switch (dir)
					{
						case NORTH:
						case SOUTH:
							doorCoord[0] = i;
							doorCoord[1] = oppValue;
							break;
						case EAST:
						case WEST:
							doorCoord[0] = oppValue;
							doorCoord[1] = i;
							break;
					}
					
					System.out.println("Door Coordinates: " + doorCoord[0] + " " + doorCoord[1]);
					nextDirect = dir;
					addNextDoor = false;
					break;
				}
			}
		}
		else if (cor != null && cor.canAddDoor(dir) && addNextDoor)
		{
			for (int i = start; i < end; i++)
			{
				switch (dir)
				{
					case NORTH:
					case SOUTH:
						maxSize = corridorMaxSize(i, oppValue, dir, layout);
						break;
					case EAST:
					case WEST:
						maxSize = corridorMaxSize(oppValue, i, dir, layout);
						break;
				}
				
				if (maxSize[0] > 2 || maxSize[1] > 2)
				{
					System.out.println("Adding " + dir + " Door");
					
					switch (dir)
					{
						case NORTH:
						case SOUTH:
							doorCoord[0] = i;
							doorCoord[1] = oppValue;
							break;
						case EAST:
						case WEST:
							doorCoord[0] = oppValue;
							doorCoord[1] = i;
							break;
					}
					
					System.out.println("Door Coordinates: " + doorCoord[0] + " " + doorCoord[1]);
					nextDirect = dir;
					addNextDoor = false;
					break;
				}
			}
		}
	}
	
	public static int[] cavernMaxSize (int x, int y, Direction dir, FloorCell[][] f)
	{
		if (x < 0 || x >= f.length || y < 0 || y >= f[0].length)
			return new int[] {0,0};
		
		int maxNorth = -1, maxSouth = -1, maxEast = -1, maxWest = -1, north = 0, south = 0, east = 0, west = 0;
		int[] maxSize = new int[2];
		
		System.out.println("Direction scanning (Cavern) is " + dir + " at " + x + " " + y);
		
		if (dir == null)
		{
			for (int j = y; j >= 0; j--)
			{
				east = 0;
				west = 0;
				
				for (int i = x; i >= 0; i--)
				{
					if (isSpawnable(f[i][j]))
						west++;
					else
						break;
				}
				
				for (int i = x; i < f.length; i++)
				{
					if (isSpawnable(f[i][j]))
						east++;
					else
						break;
				}
				
				if (east + west > 3 && east > 0 && west > 0)
					maxNorth++;
				else
					break;
			}
			for (int j = y; j < f[0].length; j++)
			{
				east = 0;
				west = 0;
				
				for (int i = x; i >= 0; i--)
				{
					if (isSpawnable(f[i][j]))
						west++;
					else
						break;
				}
				
				for (int i = x; i < f.length; i++)
				{
					if (isSpawnable(f[i][j]))
						east++;
					else
						break;
				}
				
				if (east + west > 3 && east > 0 && west > 0)
					maxSouth++;
				else
					break;
			}
			for (int i = x; i < f.length; i++)
			{
				north = 0;
				south = 0;
				
				for (int j = y; j >= 0; j--)
				{
					if (isSpawnable(f[i][j]))
						north++;
					else
						break;
				}
				
				for (int j = y; j < f[0].length; j++)
				{
					if (isSpawnable(f[i][j]))
						south++;
					else
						break;
				}
				
				if (north + south > 3 && north > 0 && south > 0)
					maxEast++;
				else
					break;
			}
			for (int i = x; i >= 0; i--)
			{
				north = 0;
				south = 0;
				
				for (int j = y; j >= 0; j--)
				{
					if (isSpawnable(f[i][j]))
						north++;
					else
						break;
				}
				
				for (int j = y; j < f[0].length; j++)
				{
					if (isSpawnable(f[i][j]))
						south++;
					else
						break;
				}
				
				if (north + south > 3 && north > 0 && south > 0)
					maxWest++;
				else
					break;
			}
		}
		else
			switch (dir)
			{
				case NORTH:
					maxSouth = 0;
					for (int i = x; i >= 0; i--)
					{
						north = 0;
						
						for (int j = y - 1; j >= 0; j--)
						{
							if (isSpawnable(f[i][j]))
								north++;
							else
								break;
						}
						
						if (north >= 3)
							maxWest++;
						else
							break;
					}
					
					for (int i = x; i < f.length; i++)
					{
						north = 0;
						
						for (int j = y - 1; j >= 0; j--)
						{
							if (isSpawnable(f[i][j]))
								north++;
							else
								break;
						}
						
						if (north >= 3)
							maxEast++;
						else
							break;
					}
					
					for (int j = y - 1; j >= 0; j--)
					{
						east = 0;
						west = 0;
						
						for (int i = x; i >= 0; i--)
						{
							if (isSpawnable(f[i][j]))
								west++;
							else
								break;
						}
						
						for (int i = x; i < f.length; i++)
						{
							if (isSpawnable(f[i][j]))
								east++;
							else
								break;
						}
						
						if (east + west > 3 && east > 0 && west > 0)
							maxNorth++;
						else
							break;
					}
					break;
					
				case SOUTH:
					maxNorth = 0;
					for (int i = x; i >= 0; i--)
					{
						south = 0;
						
						for (int j = y + 1; j < f[0].length; j++)
						{
							if (isSpawnable(f[i][j]))
								south++;
							else
								break;
						}
						
						if (south >= 3)
							maxWest++;
						else
							break;
					}
					
					for (int i = x; i < f.length; i++)
					{
						south = 0;
						
						for (int j = y + 1; j < f[0].length; j++)
						{
							if (isSpawnable(f[i][j]))
								south++;
							else
								break;
						}
						
						if (south >= 3)
							maxEast++;
						else
							break;
					}
					
					for (int j = y + 1; j < f[0].length; j++)
					{
						east = 0;
						west = 0;
						
						for (int i = x; i >= 0; i--)
						{
							if (isSpawnable(f[i][j]))
								west++;
							else
								break;
						}
						
						for (int i = x; i < f.length; i++)
						{
							if (isSpawnable(f[i][j]))
								east++;
							else
								break;
						}
						
						if (east + west > 3 && east > 0 && west > 0)
							maxSouth++;
						else
							break;
					}
					break;
					
				case EAST:
					maxWest = 0;
					for (int j = y; j >= 0; j--)
					{
						east = 0;
						
						for (int i = x + 1; i < f.length; i++)
						{
							if (isSpawnable(f[i][j]))
								east++;
							else
								break;
						}
						
						if (east >= 3)
							maxNorth++;
						else
							break;
					}
					
					for (int j = y; j < f[0].length; j++)
					{
						east = 0;
						
						for (int i = x + 1; i < f.length; i++)
						{
							if (isSpawnable(f[i][j]))
								east++;
							else
								break;
						}
						
						if (east >= 3)
							maxSouth++;
						else
							break;
					}
					
					for (int i = x + 1; i < f.length; i++)
					{
						north = 0;
						south = 0;
						
						for (int j = y; j >= 0; j--)
						{
							if (isSpawnable(f[i][j]))
								north++;
							else
								break;
						}
						
						for (int j = y; j < f[0].length; j++)
						{
							if (isSpawnable(f[i][j]))
								south++;
							else
								break;
						}
						
						if (north + south > 3 && north > 0 && south > 0)
							maxEast++;
						else
							break;
					}
					break;
					
				case WEST:
					maxEast = 0;
					for (int j = y; j >= 0; j--)
					{
						west = 0;
						
						for (int i = x - 1; i >= 0; i--)
						{
							if (isSpawnable(f[i][j]))
								west++;
							else
								break;
						}
						
						if (west >= 3)
							maxNorth++;
						else
							break;
					}
					
					for (int j = y; j < f[0].length; j++)
					{
						west = 0;
						
						for (int i = x - 1; i >= 0; i--)
						{
							if (isSpawnable(f[i][j]))
								west++;
							else
								break;
						}
						
						if (west >= 3)
							maxSouth++;
						else
							break;
					}
					
					for (int i = x - 1; i >= 0; i--)
					{
						north = 0;
						south = 0;
						
						for (int j = y; j >= 0; j--)
						{
							if (isSpawnable(f[i][j]))
								north++;
							else
								break;
						}
						
						for (int j = y; j < f[0].length; j++)
						{
							if (isSpawnable(f[i][j]))
								south++;
							else
								break;
						}
						
						if (north + south > 3 && north > 0 && south > 0)
							maxWest++;
						else
							break;
					}
					break;
			}
		
		maxSize[0] = maxEast + maxWest + 1;
		maxSize[1] = maxNorth + maxSouth + 1;
		
		if (maxSize[0] < 3 || maxSize[1] < 3)
		{
			maxSize[0] = 0;
			maxSize[1] = 0;
		}
		
		System.out.println("Max Size: " + maxSize[0] + " " + maxSize[1]);
		
		return maxSize;
	}
	
	public static int[] corridorMaxSize (int x, int y, Direction dir, FloorCell[][] f)
	{
		int[] maxSize = new int[2];
		int north = 0, south = 0, east = 0, west = 0;
		
		System.out.println("Direction scanning (Corridor) is " + dir + " at " + x + " " + y);
		
		if (dir == null && isSpawnable(f[x][y]))
		{
			for (int j = y; j >= 0; j--)
			{
				if (isSpawnable(f[x][j]))
					north++;
				else
					break;
			}
			for (int j = y; j < f[0].length; j++)
			{
				if (isSpawnable(f[x][j]))
					south++;
				else
					break;
			}
			for (int i = x; i < f.length; i++)
			{
				if (isSpawnable(f[i][y]))
					east++;
				else
					break;
			}
			for (int i = x; i >= 0; i--)
			{
				if (isSpawnable(f[i][y]))
					west++;
				else
					break;
			}
			
			maxSize[0] = east + west - 1;
			maxSize[1] = north + south - 1;
		}
		else
			switch (dir)
			{
				case NORTH:
					if (y - 1 >= 0)
					{
						for (int i = x; i >= 0; i--)
						{
							if (isSpawnable(f[i][y - 1]))
								west++;
							else
								break;
						}
						
						for (int i = x; i < f.length; i++)
						{
							if (isSpawnable(f[i][y - 1]))
								east++;
							else
								break;
						}
						
						for (int j = y - 1; j >= 0; j--)
						{
							if (isSpawnable(f[x][j]))
								north++;
							else
								break;
						}
						
						maxSize[0] = west + east - 1;
						maxSize[1] = north;
					}
					break;
					
				case SOUTH:
					if (y + 1 < f[0].length)
					{
						for (int i = x; i >= 0; i--)
						{
							if (isSpawnable(f[i][y + 1]))
								west++;
							else
								break;
						}
						
						for (int i = x; i < f.length; i++)
						{
							if (isSpawnable(f[i][y + 1]))
								east++;
							else
								break;
						}
						
						for (int j = y + 1; j < f[0].length; j++)
						{
							if (isSpawnable(f[x][j]))
								south++;
							else
								break;
						}
						
						maxSize[0] = west + east - 1;
						maxSize[1] = south;
					}
					break;
					
				case EAST:
					if (x + 1 < f.length)
					{
						for (int j = y; j >= 0; j--)
						{
							if (isSpawnable(f[x + 1][j]))
								north++;
							else
								break;
						}
						
						for (int j = y; j < f[0].length; j++)
						{
							if (isSpawnable(f[x + 1][j]))
								south++;
							else
								break;
						}
						
						for (int i = x + 1; i < f.length; i++)
						{
							if (isSpawnable(f[i][y]))
								east++;
							else
								break;
						}
						
						maxSize[0] = east;
						maxSize[1] = north + south - 1;
					}
					break;
					
				case WEST:
					if (x - 1 >= 0)
					{
						for (int j = y; j >= 0; j--)
						{
							if (isSpawnable(f[x - 1][j]))
								north++;
							else
								break;
						}
						
						for (int j = y; j < f[0].length; j++)
						{
							if (isSpawnable(f[x - 1][j]))
								south++;
							else
								break;
						}
						
						for (int i = x - 1; i >= 0; i--)
						{
							if (isSpawnable(f[i][y]))
								west++;
							else
								break;
						}
						
						maxSize[0] = west;
						maxSize[1] = north + south - 1;
					}
					break;
			}
		
		if (maxSize[0] < 3 && maxSize[1] < 3)
		{
			maxSize[0] = 0;
			maxSize[1] = 0;
		}
		
		System.out.println("Max size: " + maxSize[0] + " " + maxSize[1]);
		
		return maxSize;
	}
	
	public static boolean isSpawnable(FloorCell fc)
	{
		return fc == null || fc.type != CellType.CELL;
	}
	
	public static void reset()
	{
		doorCoord = new int[2];
		nextDirect = null;
		addNextDoor = true;
	}
}