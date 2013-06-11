import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;


public class ColorThread extends Thread{

	private ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	private WaitOnColor waitOnColor = new WaitOnColor();
	private int direction;
	private boolean isBigTrain;
	private int colorSensorID;// NXT_07 = 1, NXT_03 = 2, NXT4 = 3
	
	public ColorThread(boolean isBigTrain, int colorSensorID, int direction){
		this.isBigTrain = isBigTrain;
		this.colorSensorID = colorSensorID;
		this.direction = direction;
	}
	
	public int getColorID(int red, int green, int blue){
		switch (colorSensorID){
		case 1:
			if(red > 190 && green > 45 && green < 110 && blue > 25 && blue < 78){ //ROT
				LCD.clear();
				LCD.drawInt(Color.RED, 6, 6);
				return Color.RED;
			}
			if(red > 35 && red < 80 && green > 80 && green < 130 && blue > 170 && blue < 255){ //BLAU
				LCD.clear();
				LCD.drawInt(Color.BLUE, 6, 6);
				return Color.BLUE;
			}
			if(red > 250 && green > 250 && blue < 150){ //GELB
				LCD.clear();
				LCD.drawInt(Color.YELLOW, 6, 6);
				return Color.YELLOW;
			}
			LCD.clear();
			LCD.drawInt(255, 6, 6);
			return 255;
		case 2:
			if(red > 150 && green > 20 && green < 65 && blue < 30){ //ROT
				LCD.clear();
				LCD.drawInt(Color.RED, 6, 6);
				return Color.RED;
			}
			if(red > 35 && red < 80 && green > 80 && green < 130 && blue > 170 && blue < 255){ //BLAU
				LCD.clear();
				LCD.drawInt(Color.BLUE, 6, 6);
				return Color.BLUE;
			}
			if(red > 225 && green > 190 && blue < 25){ //GELB
				LCD.clear();
				LCD.drawInt(Color.YELLOW, 6, 6);
				return Color.YELLOW;
			}
			LCD.clear();
			LCD.drawInt(255, 6, 6);
			return 255;
		case 3:
			if(red > 190 && green > 45 && green < 110 && blue > 25 && blue < 78){ //ROT
				LCD.clear();
				LCD.drawInt(Color.RED, 6, 6);
				return Color.RED;
			}
			if(red > 35 && red < 80 && green > 80 && green < 130 && blue > 170 && blue < 255){ //BLAU
				LCD.clear();
				LCD.drawInt(Color.BLUE, 6, 6);
				return Color.BLUE;
			}
			if(red > 250 && green > 250 && blue < 150){ //GELB
				LCD.clear();
				LCD.drawInt(Color.YELLOW, 6, 6);
				return Color.YELLOW;
			}
			LCD.clear();
			LCD.drawInt(255, 6, 6);
			return 255;
		default:
			return 255;
		}
	}
	
	public boolean notGreen(){
		switch (colorSensorID){
		case 1:
			return !(colorSensor.getColor().getRed() < 170 && colorSensor.getColor().getRed() > 25 && colorSensor.getColor().getGreen() > 75 && colorSensor.getColor().getGreen() < 225 && colorSensor.getColor().getBlue() > 40 && colorSensor.getColor().getBlue() < 200);
		case 2:
			return !(colorSensor.getColor().getRed() < 70 && colorSensor.getColor().getRed() > 15 && colorSensor.getColor().getGreen() > 55 && colorSensor.getColor().getGreen() < 90 && colorSensor.getColor().getBlue() > 20 && colorSensor.getColor().getBlue() < 65);
		case 3:
		default:
			return false;	
		}
	}
	
	public int getDirection(){
		return direction;
	}
	
	public void run(){
		while(true){
			switch (getColorID(colorSensor.getColor().getRed(), colorSensor.getColor().getGreen(), colorSensor.getColor().getBlue())) {
			case Color.RED:
				LCD.clear();
				LCD.drawString("ROT", 1, 2);
				//waitOnColor.wait(Main.RED, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				while(notGreen()){
				}
				if(direction == -1){
					Motor.B.backward();
					Motor.C.backward();
				}else{
					Motor.B.forward();
					Motor.C.forward();
				}
				
				LCD.clear();
				
				break;
			case Color.BLUE:
				LCD.clear();
				LCD.drawString("BLAU", 1, 2);
				Motor.B.setSpeed(250);
				Motor.C.setSpeed(250);
				LCD.clear();
				
				break;
			case Color.YELLOW:
				LCD.clear();
				LCD.drawString("GELB", 1, 2);
				//waitOnColor.wait(Main.YELLOW, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				if(direction == -1){
					Motor.A.rotateTo(-30);
				}
				while(notGreen()){
				}
				direction = direction * -1;
				Motor.A.rotateTo(0);
				if(direction == -1){
					Motor.B.backward();
					Motor.C.backward();
				}else{
					Motor.B.forward();
					Motor.C.forward();
				}
				LCD.clear();
					
				break;
			}
		}
	}
}
