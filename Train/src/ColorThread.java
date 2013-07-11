import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.Color;

public class ColorThread extends Thread {

	private ColorHTSensor colorSensor = new ColorHTSensor(SensorPort.S4);
	private int direction; // -1 kommt vom Kunde, 1 kommt vom Gro�handel (gro�er Zug umgekehrt!)
	private boolean isBigTrain;
	private int colorSensorID;// NXT_07 = 1, NXT_03 = 2, NXT4 = 3
	private boolean signalGo = false;
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

	public void setSignalGo(boolean go) {
		signalGo = go;
	}

	public int getColorID(int red, int green, int blue) {
		red = (int) (red * multiplier);
		blue = (int) (blue * multiplier);
		green = (int) (green * multiplier);
		
		switch (colorSensorID) {
		case 1:
			if (red > 185 && green > 25 && green < 110 && blue > 5 && blue < 75) { // ROT
				return Color.RED;
			}
			if (red > 250 && green > 250 && blue < 45) { // GELB
				return Color.YELLOW;
			}
			
			return 255;
		case 2:
			if (red > 170 && green > 20 && green < 55 && blue < 25) { // ROT
				return Color.RED;
			}
			if (red > 10 && red < 70 && green > 55 && green < 110 && blue > 180) { // BLAU
				return Color.BLUE;
			}
			if (red > 225 && green > 225 && blue < 25) { // GELB
				return Color.YELLOW;
			}
			return 255;
		case 3:
			if (red > 225 && green > 25 && green < 75 && blue < 36) { // ROT
				return Color.RED;
			}
			if (red > 20 && red < 75 && green > 65 && green < 115 && blue > 200) { // BLAU
				return Color.BLUE;
			}
			if (red > 250 && green > 250 && blue < 25) { // GELB
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
			return !(colorSensor.getColor().getRed() * multiplier < 110
					&& colorSensor.getColor().getRed() * multiplier > 10
					&& colorSensor.getColor().getGreen() * multiplier > 55
					&& colorSensor.getColor().getGreen() * multiplier < 170
					&& colorSensor.getColor().getBlue() * multiplier > 35 && colorSensor
					.getColor().getBlue() * multiplier < 110);
		case 2:
			return !(colorSensor.getColor().getRed() * multiplier < 80
					&& colorSensor.getColor().getRed() * multiplier > 15
					&& colorSensor.getColor().getGreen() * multiplier > 65
					&& colorSensor.getColor().getBlue() * multiplier > 20 && colorSensor
					.getColor().getBlue() * multiplier < 85);
		case 3:
			return !(colorSensor.getColor().getRed() * multiplier < 115
					&& colorSensor.getColor().getRed() * multiplier > 10
					&& colorSensor.getColor().getGreen() * multiplier > 65
					&& colorSensor.getColor().getBlue() * multiplier > 20 && colorSensor
					.getColor().getBlue() * multiplier < 135);
		default:
			return false;
		}
	}

	public int getDirection() {
		return direction;
	}

	public void run() {
		while(colorSensor.initWhiteBalance() == 0){}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
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
				Motor.B.stop(true);
				Motor.C.stop(false);
				color = colorSensor.getColor();
				if(getColorID(color.getRed(), color.getGreen(), color.getBlue()) != Color.RED){
					Motor.B.setSpeed(100);
					Motor.C.setSpeed(100);
					if (direction == Main.FORWARD) {
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
				while(notGreen()){}
				if (direction == Main.BACKWARD) {
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
				Motor.B.stop(true);
				Motor.C.stop(false);
				color = colorSensor.getColor();
				if(getColorID(color.getRed(), color.getGreen(), color.getBlue()) == Color.BLUE){
					Motor.B.setSpeed(100);
					Motor.C.setSpeed(100);
					if (direction == Main.BACKWARD) {
						Motor.B.backward();
						Motor.C.backward();
					} else {
						Motor.B.forward();
						Motor.C.forward();
					}
					while((getColorID(color.getRed(), color.getGreen(), color.getBlue())) == Color.BLUE){
						color = colorSensor.getColor();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Motor.B.stop(true);
					Motor.C.stop(false);
					Motor.B.setSpeed(Main.SPEED);
					Motor.C.setSpeed(Main.SPEED);
				}
				bluetooth.setWaiting(true);
				if (direction == Main.FORWARD) {
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
				signalGo = false;
				while (!signalGo) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (direction == Main.BACKWARD) {
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
				Motor.B.stop(true);
				Motor.C.stop(true);
				if (direction == Main.BACKWARD && !isBigTrain || direction == Main.FORWARD && isBigTrain) {
					Motor.A.rotateTo(-30);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Motor.A.rotateTo(0);
				}
				while (notGreen()) {
				}
				direction = direction * -1;
				Motor.A.rotateTo(0);
				if (direction == Main.BACKWARD) {
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
