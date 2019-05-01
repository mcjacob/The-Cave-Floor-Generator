public class Corridor
{
	public int length = 0, doorsLeft = 0;
	public boolean vertical = false;
	
	public FloorCell[][] layout;
	public int width, height;
	
	public Corridor (int maxWidth, int maxHeight)
	{
		if (maxWidth > 2 || maxHeight > 2)
			while (length == 0 || (length > maxWidth && !vertical) || (length > maxHeight && vertical))
			{
				if (maxWidth > 2 && maxHeight > 2)
					vertical = Components.rng.nextBoolean();
				else if (maxWidth > 2)
					vertical = false;
				else if (maxHeight > 2)
					vertical = true;
				
				if (vertical)
					length = Components.rng.nextInt(maxHeight / 2) + Components.rng.nextInt(maxHeight / 2 - (maxHeight + 1) % 2) + 3;
				else
					length = Components.rng.nextInt(maxWidth / 2) + Components.rng.nextInt(maxWidth / 2 - (maxWidth + 1) % 2) + 3;
			}
		
		if (vertical)
			layout = new FloorCell[1][length];
		else
			layout = new FloorCell[length][1];
		
		width = layout.length;
		height = layout[0].length;
		
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
		
		doorsLeft = 1;
	}
	
	public Corridor (int length, boolean vertical, FloorCell[][] layout)
	{
		this(length, vertical, layout, 1);
	}
	
	public Corridor (int length, boolean vertical, FloorCell[][] layout, int doors)
	{
		this.length = length;
		this.vertical = vertical;
		this.doorsLeft = doors;
		this.width = layout.length;
		this.height = layout[0].length;
		
		this.layout = new FloorCell[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				if (layout[i][j] != null)
					this.layout[i][j] = new FloorCell(layout[i][j]);
	}
	
	public Corridor (Corridor c)
	{
		this(c.length, c.vertical, c.layout, c.doorsLeft);
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