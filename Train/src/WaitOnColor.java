import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class WaitOnColor{
	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	public void wait(int color, boolean forward, int speed){
		int actualSpeed = speed;
		Motor.B.stop(true);
		Motor.C.stop(false);
		if(colorSensor.getColorID() == color){
			return;
		}else{
			if(forward){
				Motor.B.backward();
				Motor.C.backward();
			}else{
				Motor.B.forward();
				Motor.C.forward();
			}
			while(colorSensor.getColorID() != color){}
			actualSpeed -= 50;
			Motor.B.setSpeed(actualSpeed);
			Motor.C.setSpeed(actualSpeed);
			wait(color, !forward, actualSpeed);
			Motor.B.setSpeed(400);
			Motor.C.setSpeed(400);
		}
	}
}
