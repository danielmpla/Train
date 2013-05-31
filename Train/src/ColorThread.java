import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class ColorThread extends Thread{

	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	WaitOnColor waitOnColor = new WaitOnColor();
	
	public void run(){
		while(true){
			switch(colorSensor.getColorID()){
			case Main.RED:
				waitOnColor.wait(Main.RED, true, 600);
				while(Button.readButtons() != Button.ID_ENTER || colorSensor.getColorID() == Main.GREEN){
				}
				Motor.B.forward();
				Motor.C.forward();
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case Main.BLUE:
				Motor.B.setSpeed(250);
				Motor.C.setSpeed(250);
				
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case Main.YELLOW:
				waitOnColor.wait(Main.YELLOW, true, 600);
				Motor.A.rotateTo(-30);
				while(Button.readButtons() != Button.ID_ENTER){
				}
				Motor.A.rotateTo(0);
				Motor.B.forward();
				Motor.C.forward();
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			
		}
	}
}
