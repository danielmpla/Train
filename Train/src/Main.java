import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.util.TextMenu;


public class Main {
	static final int RED = 0;
	static final int YELLOW = 3;
	static final int BLUE = 4;
	static final int GREEN = 1;
	static final int SPEED = 500;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}
	
	public Main(){
		//int actualSpeed = 300;
		Motor.B.setSpeed(SPEED);
		Motor.C.setSpeed(SPEED);
		
		String[] trainItems = {"kleiner Zug", "grosser Zug"};
		String[] locationItems = {"Grosshandel", "Kunde"};
		String[] colorItems = {"1 (NXT_07)", "2 (NXT_03)", "3 (NXT4)"};
		TextMenu trainSizeMenu = new TextMenu(trainItems, 2, "Zugart waehlen");
		TextMenu colorIDMenu = new TextMenu(colorItems, 2, "Color ID waehlen");
		TextMenu locationMenu = new TextMenu(locationItems, 2, "Startpunktwahl!");
		
		boolean isBigTrain;
		int direction;
		int colorSensorID;
		
		if(trainSizeMenu.select() == 0){
			isBigTrain = false;
		}else{
			isBigTrain = true;
		}
		
		if(locationMenu.select() == 0){
			direction = 1;
		}else{
			direction = -1;
		}
		
		colorSensorID = colorIDMenu.select() + 1;
		
		ColorThread colorThread = new ColorThread(isBigTrain, colorSensorID, direction);
		colorThread.setDaemon(true);
		
		boolean isHost = colorSensorID == 2;
		
		BluetoothThread bluetoothThread = new BluetoothThread(isHost, colorThread);
		bluetoothThread.setDaemon(true);
		bluetoothThread.start();
		
		colorThread.start();
		colorThread.setBluetoothThread(bluetoothThread);
		
		if(direction == 1){
			Motor.B.forward();
			Motor.C.forward();
		}else{
			Motor.B.backward();
			Motor.C.backward();
		}
		while(Button.readButtons() != Button.ID_ESCAPE){
			/*if(Button.readButtons() == Button.ID_ENTER){
				Motor.B.setSpeed(SPEED);
				Motor.C.setSpeed(SPEED);
			}*/
		}
		System.exit(0);
		try {
			colorThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
