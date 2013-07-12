import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.util.TextMenu;

/**
 * Is the Main program and starts the threads
 * 
 * @author Daniel
 */
public class Main {
	static final int RED = 0;
	static final int YELLOW = 3;
	static final int BLUE = 4;
	static final int GREEN = 1;
	static final int SPEED = 500;
	static final int FORWARD = 1;
	static final int BACKWARD = -1;

	private BluetoothThread bluetoothThread;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}

	/**
	 * 
	 */
	public Main() {
		Motor.B.setSpeed(SPEED);
		Motor.C.setSpeed(SPEED);

		/**
		 * Creates the menu
		 */
		
		String[] trainItems = { "kleiner Zug", "grosser Zug" };
		String[] locationItems = { "Grosshandel", "Kunde" };
		String[] colorItems = { "1 (NXT_07)", "2 (NXT_03)", "3 (NXT4)" };
		TextMenu trainSizeMenu = new TextMenu(trainItems, 2, "Zugart waehlen");
		TextMenu colorIDMenu = new TextMenu(colorItems, 2, "Color ID waehlen");
		TextMenu locationMenu = new TextMenu(locationItems, 2, "Startpunktwahl!");

		boolean isBigTrain;
		int direction;
		int colorSensorID;

		/**
		 * Stores the inputs of the menu
		 */
		
		if (trainSizeMenu.select() == 0) {
			isBigTrain = false;
		} else {
			isBigTrain = true;
		}

		if (locationMenu.select() == 0) {
			direction = FORWARD;
		} else {
			direction = BACKWARD;
		}

		colorSensorID = colorIDMenu.select() + 1;

		/**
		 * Initializes ColorThread
		 */
		
		ColorThread colorThread = new ColorThread(isBigTrain, colorSensorID, direction);
		colorThread.setDaemon(true);
		boolean isHost = colorSensorID == 2;
		
		/**
		 * Initializes BluetoothThread if it is a small train
		 */
		
		if (!isBigTrain) {
			bluetoothThread = new BluetoothThread(isHost, colorThread);
			bluetoothThread.setDaemon(true);
			bluetoothThread.start();
		}
		
		/**
		 * starts the ColorThread
		 */
		
		colorThread.start();
		/**
		 * starts the BluetoothThread if it is a small train 
		 */
		
		if (!isBigTrain) {
			colorThread.setBluetoothThread(bluetoothThread);
		}
		
		/**
		 * starts the engines
		 */
		
		if (direction == FORWARD) {
			Motor.B.forward();
			Motor.C.forward();
		} else {
			Motor.B.backward();
			Motor.C.backward();
		}
		
		/**
		 * runs the program until ESCAPE is pressed
		 */
		
		while (Button.readButtons() != Button.ID_ESCAPE) {}
		System.exit(0);
		
	}

}