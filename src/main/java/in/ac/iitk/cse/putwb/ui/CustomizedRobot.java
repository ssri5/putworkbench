/**
 * 
 */
package in.ac.iitk.cse.putwb.ui;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import sun.awt.SunToolkit;

/**
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("restriction")
public class CustomizedRobot extends Robot {

	/**
	 * @throws AWTException
	 */
	public CustomizedRobot() throws AWTException {
		super();
		setAutoWaitForIdle(false);
	}
	
	
	public void pressKey(int keycode) throws InvocationTargetException, InterruptedException {
		keyPress(keycode);
		waitForEventsToClear();
		Thread.sleep(50);
		keyRelease(keycode);
		waitForEventsToClear();
	}
	
	public void moveMouseToLocation(Point location) throws InterruptedException, InvocationTargetException {
		Point currentLocation = MouseInfo.getPointerInfo().getLocation();
		int euclideanDistance = (int) Math.floor(Math.hypot((location.x - currentLocation.x), (location.y - currentLocation.y)));
		int speed = 1;
		int steps = euclideanDistance/20;
		for (int i=0; i<steps; i++){  
			int mov_x = ((location.x * i)/steps) + (currentLocation.x *(steps-i)/steps);
			int mov_y = ((location.y * i)/steps) + (currentLocation.y *(steps-i)/steps);
			mouseMove(mov_x,mov_y);
			waitForEventsToClear();
			Thread.sleep(speed);
		}
	}
	
	public void moveMouseToComponent(Component forComponent) throws InvocationTargetException, InterruptedException {
		Point midPoint = getMidPoint(forComponent);
		moveMouseToLocation(midPoint);
	}
	
	private Thread emptyThread = new Thread() {
		public void run() {
			
		}
	};
	
	private synchronized void waitForEventsToClear() throws InvocationTargetException, InterruptedException {
		if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("Cannot call method from the event dispatcher thread");
        }
		SunToolkit.flushPendingEvents();
		EventQueue.invokeAndWait(emptyThread);
		Thread.sleep(50);
	}
	
	private static Point getMidPoint(Component forComponent) {
		Point location = forComponent.getLocationOnScreen();
		Dimension size = forComponent.getSize();
		location.x += size.getWidth()/2;
		location.y += size.getHeight()/2;
		return location;
	}
	
	public void click() throws InvocationTargetException, InterruptedException {
		mousePress(InputEvent.BUTTON1_MASK);
		waitForEventsToClear();
		Thread.sleep(200);
		mouseRelease(InputEvent.BUTTON1_MASK);
		waitForEventsToClear();
		Thread.sleep(200);
	}
	
	public void typeString(String st) throws Exception {
		String upperCase = st.toUpperCase();

	    for(int i = 0; i < upperCase.length(); i++) {
	        String letter = Character.toString(upperCase.charAt(i));
	        String code = "VK_" + letter;

	        Field f = KeyEvent.class.getField(code);
	        int keyEvent = f.getInt(null);

	        pressKey(keyEvent);
	    }
	}
}
