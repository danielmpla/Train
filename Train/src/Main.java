import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class Main {
	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	static final int RED = 0;
	static final int YELLOW = 3;
	static final int BLUE = 4;
	static final int GREEN = 1;
	int actualSpeed = 600;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}
	
	public int getSpeed(){
		return actualSpeed;
	}
	
	
	public Main(){
		//int actualSpeed = 300;
		Motor.B.setSpeed(actualSpeed);
		Motor.C.setSpeed(actualSpeed);
		
		ColorThread colorThread = new ColorThread();
		colorThread.setDaemon(true);
		colorThread.start();
		
		
		Motor.B.forward();
		Motor.C.forward();
		
		while(Button.readButtons() != Button.ID_ESCAPE){
			
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
