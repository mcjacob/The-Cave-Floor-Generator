public class FloorCell
{
	public CellType type;
	public CellSide north, south, east, west;
	
	public FloorCell()
	{
		this(CellType.CELL, new CellSide[5]);
	}
	
	public FloorCell(CellType ct)
	{
		this(ct, new CellSide[5]);
	}
	
	public FloorCell(CellType ct, CellSide... sides)
	{
		type = ct;
		
		if (sides.length <= 4)
			for (CellSide side: sides)
				switch (side.side)
				{
					case NORTH:
						north = new CellSide(side);
						break;
					case SOUTH:
						south = new CellSide(side);
						break;
					case EAST:
						east = new CellSide(side);
						break;
					case WEST:
						west = new CellSide(side);
						break;
				}
		if (north == null)
			north = new CellSide(Direction.NORTH);
		if (south == null)
			south = new CellSide(Direction.SOUTH);
		if (east == null)
			east = new CellSide(Direction.EAST);
		if (west == null)
			west = new CellSide(Direction.WEST);
	}
	
	public FloorCell(FloorCell fc)
	{
		this(fc.type, fc.north, fc.south, fc.east, fc.west);
	}
	
	public FloorCell(FloorCell fc, Direction d)
	{
		this (fc);
		
		switch (d)
		{
			case NORTH:
				north.door = new Door();
				break;
			case SOUTH:
				south.door = new Door();
				break;
			case EAST:
				east.door = new Door();
				break;
			case WEST:
				west.door = new Door();
		}
	}
	
	public boolean allSidesWall()
	{
		return (north.hasWall && !north.hasDoor()) && (south.hasWall && !south.hasDoor()) && (east.hasWall && !east.hasDoor()) && (west.hasWall && !west.hasDoor());
	}
}