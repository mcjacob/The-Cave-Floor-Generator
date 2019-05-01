public class CellSide
{
	public Direction side;
	public Door door;
	
	public boolean hasWall;
	
	public CellSide(Direction dir)
	{
		this(dir, null, false);
	}
	
	public CellSide(Direction dir, Door d)
	{
		this(dir, d, false);
	}
	
	public CellSide(Direction dir, boolean wall)
	{
		this(dir, null, wall);
	}
	
	public CellSide(Direction dir, Door d, boolean wall)
	{
		side = dir;
		
		if (d != null)
		{
			hasWall = true;
			door = new Door(d);
		}
		else
			hasWall = wall;
	}

	public CellSide(CellSide cs)
	{
		this(cs.side, cs.door, cs.hasWall);
	}
	
	public boolean hasDoor()
	{
		return door != null;
	}
}