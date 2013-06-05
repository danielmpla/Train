import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class Main {
	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	static final int RED = 0;
	static final int YELLOW = 3;
	static final int BLUE = 4;
	static final int GREEN = 1;
	static final int speed = 600;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}
	
	public int getSpeed(){
		return speed;
	}
	
	
	public Main(){
		//int actualSpeed = 300;
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);
		
		ColorThread colorThread = new ColorThread();
		colorThread.setDaemon(true);
		colorThread.start();
		
		
		Motor.B.forward();
		Motor.C.forward();
		
		while(Button.readButtons() != Button.ID_ESCAPE){
			if(colorSensor.getColor().getRed() < 170 && colorSensor.getColor().getRed() > 50 && colorSensor.getColor().getGreen() > 80 && colorSensor.getColor().getGreen() < 225 && colorSensor.getColor().getBlue() > 40 && colorSensor.getColor().getBlue() < 200){ //GRÜN		
				LCD.drawString("GRUEN", 1, 2);
				
				Motor.B.setSpeed(speed);
				Motor.C.setSpeed(speed);
				
				colorThread.setEnd(true);
			}
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
