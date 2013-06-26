import java.util.BitSet;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;

public class ColorThread extends Thread {

	private ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	private WaitOnColor waitOnColor = new WaitOnColor();
	private int direction; // -1 kommt vom Kunde, 1 kommt vom Gro�handel (gro�er
							// Zug umgekehrt!)
	private boolean isBigTrain;
	private int colorSensorID;// NXT_07 = 1, NXT_03 = 2, NXT4 = 3
	private boolean end = false;
	private boolean isBehindCross = false;
	private BluetoothThread bluetooth;
	
	private float multiplier;

	public ColorThread(boolean isBigTrain, int colorSensorID, int direction) {
		this.isBigTrain = isBigTrain;
		this.colorSensorID = colorSensorID;
		this.direction = direction;
	}

	public void setBluetoothThread(BluetoothThread blue) {
		bluetooth = blue;
	}

	public void setEnd(boolean b) {
		end = b;
	}

	public int getColorID(int red, int green, int blue) {
		red = (int) (red * multiplier);
		blue = (int) (blue * multiplier);
		green = (int) (green * multiplier);
		
		switch (colorSensorID) {
		case 1:
			if (red > 190 && green > 35 && green < 70 && blue > 15 && blue < 40) { // ROT
				
				return Color.RED;
			}
			/*
			 * if(red > 35 && red < 80 && green > 80 && green < 130 && blue >
			 * 170 && blue < 255){ //BLAU return Color.BLUE; }
			 */
			if (red > 250 && green > 250 && blue < 25) { // GELB
				
				return Color.YELLOW;
			}
			
			return 255;
		case 2:
			if (red > 150 && green > 20 && green < 75 && blue < 45) { // ROT
				
				return Color.RED;
			}
			if (red > 10 && red < 50 && green > 50 && green < 105 && blue > 170) { // BLAU
				
				return Color.BLUE;
			}
			if (red > 225 && green > 190 && blue < 25) { // GELB
				return Color.YELLOW;
			}
			return 255;
		case 3:
			if (red > 235 && green > 20 && green < 95 && blue > 0 && blue < 60) { // ROT
				return Color.RED;
			}
			if (red > 15 && red < 60 && green > 65 && green < 115 && blue > 200) { // BLAU
				return Color.BLUE;
			}
			if (red > 250 && green > 250 && blue < 50) { // GELB
				return Color.YELLOW;
			}
			return 255;
		default:
			return 255;
		}
	}

	public boolean notGreen() {
		switch (colorSensorID) {
		case 1:
			return !(colorSensor.getColor().getRed() * multiplier < 80
					&& colorSensor.getColor().getRed() * multiplier > 25
					&& colorSensor.getColor().getGreen() * multiplier > 55
					&& colorSensor.getColor().getGreen() * multiplier < 135
					&& colorSensor.getColor().getBlue() * multiplier > 35 && colorSensor
					.getColor().getBlue() * multiplier < 85);
		case 2:
			return !(colorSensor.getColor().getRed() * multiplier < 70
					&& colorSensor.getColor().getRed() * multiplier > 15
					&& colorSensor.getColor().getGreen() * multiplier > 55
					&& colorSensor.getColor().getGreen() * multiplier < 250
					&& colorSensor.getColor().getBlue() * multiplier > 20 && colorSensor
					.getColor().getBlue() * multiplier < 65);
		case 3:
			return !(colorSensor.getColor().getRed() * multiplier < 115
					&& colorSensor.getColor().getRed() * multiplier > 30
					&& colorSensor.getColor().getGreen() * multiplier > 75
					&& colorSensor.getColor().getGreen() * multiplier < 155
					&& colorSensor.getColor().getBlue() * multiplier > 40 && colorSensor
					.getColor().getBlue() * multiplier < 135);
		default:
			return false;
		}
	}

	public int getDirection() {
		return direction;
	}

	public void run() {
		colorSensor.initWhiteBalance();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int r = colorSensor.getColor().getRed();
		int g = colorSensor.getColor().getGreen();
		int b = colorSensor.getColor().getBlue();
		
		float average = (r + g + b) / 3;
		multiplier = 250 / average;
		
		while (true) {
			Color color = colorSensor.getColor();
			switch (getColorID(color.getRed(), color.getGreen(), color.getBlue())) {
			case Color.RED:
				LCD.clear();
				LCD.drawString("ROT", 1, 2);
				// waitOnColor.wait(Main.RED, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				color = colorSensor.getColor();
				if(getColorID(color.getRed(), color.getGreen(), color.getBlue()) != Color.RED){
					Motor.B.setSpeed(100);
					Motor.C.setSpeed(100);
					if (direction == 1) {
						Motor.B.backward();
						Motor.C.backward();
					} else {
						Motor.B.forward();
						Motor.C.forward();
					}
					while((getColorID(color.getRed(), color.getGreen(), color.getBlue())) != Color.RED){
						color = colorSensor.getColor();
					}
					Motor.B.stop(true);
					Motor.C.stop(true);
					Motor.B.setSpeed(Main.SPEED);
					Motor.C.setSpeed(Main.SPEED);
				}
				while (notGreen()) {
				}
				if (direction == -1) {
					Motor.B.backward();
					Motor.C.backward();
				} else {
					Motor.B.forward();
					Motor.C.forward();
				}

				LCD.clear();

				break;
			case Color.BLUE:
				LCD.clear();
				LCD.drawString("BLAU", 1, 2);
				Motor.B.stop();
				Motor.C.stop();
				bluetooth.setWaiting(true);
				if (direction == 1) {
					if (isBehindCross) {
						bluetooth.setPositionNumber(40);//40
						LCD.drawString("Signal 40", 5, 1);
					} else {
						bluetooth.setPositionNumber(10);//10
						LCD.drawString("Signal 10", 5, 1);
					}
				} else {
					if (isBehindCross) {
						bluetooth.setPositionNumber(20);//20
						LCD.drawString("Signal 20", 5, 1);
					} else {
						bluetooth.setPositionNumber(30);//30
						LCD.drawString("Signal 30", 5, 1);
					}
				}

				while (!end) {
				}
				if (direction == -1) {
					Motor.B.backward();
					Motor.C.backward();
				} else {
					Motor.B.forward();
					Motor.C.forward();
				}
				bluetooth.setWaiting(false);
				isBehindCross = !isBehindCross; 
				LCD.clear();

				break;
			case Color.YELLOW:
				LCD.clear();
				LCD.drawString("GELB", 1, 2);
				// waitOnColor.wait(Main.YELLOW, true, 600);
				Motor.B.stop(true);
				Motor.C.stop(true);
				if (direction == -1 && !isBigTrain || direction == 1
						&& isBigTrain) {
					Motor.A.rotateTo(-30);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Motor.A.rotateTo(0);
				}
				while (notGreen()) {
				}
				direction = direction * -1;
				Motor.A.rotateTo(0);
				if (direction == -1) {
					Motor.B.backward();
					Motor.C.backward();
				} else {
					Motor.B.forward();
					Motor.C.forward();
				}
				isBehindCross = false;
				LCD.clear();

				break;
			}
		}
	}
}
