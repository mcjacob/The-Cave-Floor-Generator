import javax.swing.*;
public class TheCaveFloorGenerator
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("The Cave Floor Generator");
		Window window = new Window();
		setupWindow(frame, window);
		frame.setVisible(true);
	}

	private static void setupWindow(JFrame f, Window w)
	{
		f.add(w);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
}