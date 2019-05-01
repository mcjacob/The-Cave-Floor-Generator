public class Cavern
{
	public int width = 0, height = 0, doorsLeft = 0;
	
	public FloorCell[][] layout;
	
	public Cavern (int maxWidth, int maxHeight, boolean override)
	{
		this(maxWidth, maxHeight, override, 1);
	}
	
	public Cavern (int maxWidth, int maxHeight, boolean override, int doors)
	{
		if (maxWidth > 2 && maxHeight > 2 && !override)
		{
			width = Components.rng.nextInt(maxWidth / 2) + Components.rng.nextInt(maxWidth / 2 - (maxWidth + 1) % 2) + 3;
			height = Components.rng.nextInt(maxHeight / 2) + Components.rng.nextInt(maxHeight / 2 - (maxHeight + 1) % 2) + 3;
		}
		else if (maxWidth > 0 && maxHeight > 0 && override)
		{
			width = maxWidth;
			height = maxHeight;
		}
		else
			return;
		
		layout = new FloorCell[width][height];
		
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
			{
				layout[i][j] = new FloorCell();
				
				if (i == 0)
					layout[i][j].west.hasWall = true;
				if (i == width - 1)
					layout[i][j].east.hasWall = true;
				if (j == 0)
					layout[i][j].north.hasWall = true;
				if (j == height - 1)
					layout[i][j].south.hasWall = true;
			}
		
		this.doorsLeft = doors;
	}
	
	public Cavern (FloorCell[][] layout)
	{
		this.width = layout.length;
		this.height = layout[0].length;
		
		this.layout = new FloorCell[width][height];
		
		for (int i = 0; i < layout.length; i++)
			for (int j = 0; j < layout[0].length; j++)
				if (layout[i][j] != null)
					this.layout[i][j] = new FloorCell(layout[i][j]);
	}
	
	public Cavern (Cavern c)
	{
		this(c.layout);
	}
	
	public boolean hasDoor(Direction d)
	{
		if (layout != null)
			switch (d)
			{
				case NORTH:
					for (int i = 0; i < layout.length; i++)
						if (layout[i][0] != null)
							if (layout[i][0].north.hasDoor())
								return true;
					break;
					
				case SOUTH:
					for (int i = 0; i < layout.length; i++)
						if (layout[i][layout[0].length - 1] != null)
							if (layout[i][layout[0].length - 1].south.hasDoor())
								return true;
					break;
					
				case EAST:
					for (int j = 0; j < layout[0].length; j++)
						if (layout[layout.length - 1][j] != null)
							if (layout[layout.length - 1][j].east.hasDoor())
								return true;
					break;
					
				case WEST:
					for (int j = 0; j < layout[0].length; j++)
						if (layout[0][j] != null)
							if (layout[0][j].west.hasDoor())
								return true;
					break;
			}
		
		return false;
	}
	
	public boolean canAddDoor(Direction d)
	{
		if (layout != null && doorsLeft > 0)
			switch (d)
			{
				case NORTH:
					for (int i = 0; i < layout.length; i++)
						if (layout[i][0] != null)
							if (!layout[i][0].north.hasDoor())
								return true;
					break;
					
				case SOUTH:
					for (int i = 0; i < layout.length; i++)
						if (layout[i][layout[0].length - 1] != null)
							if (!layout[i][layout[0].length - 1].south.hasDoor())
								return true;
					break;
					
				case EAST:
					for (int j = 0; j < layout[0].length; j++)
						if (layout[layout.length - 1][j] != null)
							if (!layout[layout.length - 1][j].east.hasDoor())
								return true;
					break;
					
				case WEST:
					for (int j = 0; j < layout[0].length; j++)
						if (layout[0][j] != null)
							if (!layout[0][j].west.hasDoor())
								return true;
					break;
			}
		
		return false;
	}
}