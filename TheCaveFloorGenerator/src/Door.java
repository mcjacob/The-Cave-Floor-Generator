public class Door
{
	public boolean isLocked, isSecret, isBarred;
	
	public Door()
	{
		this(Components.rng.nextInt(8) == 0, Components.rng.nextInt(8) == 0, Components.rng.nextInt(8) == 0);
	}
	
	public Door(boolean locked, boolean secret, boolean barred)
	{
		isLocked = locked;
		isSecret = secret;
		isBarred = barred;
	}
	
	public Door(Door d)
	{
		this.isLocked = d.isLocked;
		this.isSecret = d.isSecret;
		this.isBarred = d.isBarred;
	}
}