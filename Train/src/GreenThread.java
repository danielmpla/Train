import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class GreenThread extends Thread{

	private ColorThread colorThread;
	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	
	public GreenThread(ColorThread colorThread) {
		this.colorThread = colorThread;	
	}
	
	public void run(){
		if(colorSensor.getColor().getRed() < 170 && colorSensor.getColor().getRed() > 50 && colorSensor.getColor().getGreen() > 80 && colorSensor.getColor().getGreen() < 225 && colorSensor.getColor().getBlue() > 40 && colorSensor.getColor().getBlue() < 200){ //GRÜN		
			LCD.drawString("GRUEN", 1, 2);
			
			Motor.B.setSpeed(Main.speed);
			Motor.C.setSpeed(Main.speed);
			
			colorThread.setEnd(true);
		}
	}
}