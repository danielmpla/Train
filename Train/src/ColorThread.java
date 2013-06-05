import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;


public class ColorThread extends Thread{

	ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	WaitOnColor waitOnColor = new WaitOnColor();
	boolean end = false;
	
	public void setEnd(boolean bool){
		end = bool;
	}
	
	public void run(){
		while(true){
			if(colorSensor.getColor().getRed() < 190 && colorSensor.getColor().getGreen() > 45 && colorSensor.getColor().getGreen() < 110 && colorSensor.getColor().getBlue() > 25 && colorSensor.getColor().getBlue() < 78){ //ROT
				end = false;
				LCD.clear();
				LCD.drawString("ROT", 1, 2);
				//waitOnColor.wait(Main.RED, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				while(!end){
				}
				end = false;
				Motor.B.forward();
				Motor.C.forward();
				try {
					LCD.clear();
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(colorSensor.getColor().getRed() > 35 && colorSensor.getColor().getRed() < 80 && colorSensor.getColor().getGreen() > 80 && colorSensor.getColor().getGreen() < 130 && colorSensor.getColor().getBlue() > 170 && colorSensor.getColor().getBlue() < 255){ //BLAU
				LCD.clear();
				LCD.drawString("BLAU", 1, 2);
				Motor.B.setSpeed(250);
				Motor.C.setSpeed(250);
				try {
					sleep(1000);
					LCD.clear();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(colorSensor.getColor().getRed() > 250 && colorSensor.getColor().getGreen() > 250 && colorSensor.getColor().getBlue() < 150){ //GELB
				end = false;
				LCD.clear();
				LCD.drawString("GELB", 1, 2);
				//waitOnColor.wait(Main.YELLOW, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				Motor.A.rotateTo(-30);
				while(!end){
				}
				end = false;
				Motor.A.rotateTo(0);
				Motor.B.forward();
				Motor.C.forward();
				try {
					LCD.clear();
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
}
