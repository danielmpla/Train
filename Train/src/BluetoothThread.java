import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

/**
 * 
 * @author Christopher Voigt
 * */
public class BluetoothThread extends Thread {

	private OutputStream outputStream;
	private InputStream inputStream;
	private RemoteDevice brickConnector;
	private BTConnection link;

	private ColorThread colorThread;

	private boolean host;
	private boolean passed;
	private boolean waiting;
	private int positionNumber; 			// 4 verschiedene Haltepunkte
	private int positionNumberOfOtherTrain;

	public BluetoothThread(boolean isHost, ColorThread colorThread) {
		setHost(isHost);
		setColorThread(colorThread);
		
		setPassed(false);
		setWaiting(false);

		if (isHost()) {
			connectToOtherBrick();
		} else {
			waitForConnection();
		}
	}

	private void connectToOtherBrick() {
		LCD.clear();
		LCD.drawString("connecting...", 3, 3);

		brickConnector = Bluetooth.getKnownDevice("NXT4");

		if (brickConnector == null) {
			LCD.clear();
			LCD.drawString("No such device", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}

		link = Bluetooth.connect(brickConnector);

		if (link == null) {
			LCD.clear();
			LCD.drawString("failed :(", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}

		setOutputStream(link.openDataOutputStream());
		setInputStream(link.openDataInputStream());
		getInputStream().mark(5);

		LCD.clear();
		LCD.drawString("success :)", 3, 3);
	}
	private void waitForConnection() {
		LCD.clear();
		LCD.drawString("waiting...", 3, 3);

		link = Bluetooth.waitForConnection(15000, NXTConnection.PACKET);

		if (link == null) {
			LCD.clear();
			LCD.drawString("failed :(", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}

		setOutputStream(link.openDataOutputStream());
		setInputStream(link.openDataInputStream());
		getInputStream().mark(5);

		LCD.clear();
		LCD.drawString("success :)", 3, 3);
	}

	private void listenToOtherBrick() {
		try {
			if (getInputStream().available() > 0) {
				int signal = getInputStream().read();
				getInputStream().reset();

				if (signal == 0) { /* FAHR WEITER */
					colorThread.setEnd(true);
				}
				if (signal == 1) { /* ANDERER ZUG FÄHRT */
					if (getPositionNumber() == 10) { colorThread.setEnd(false);	}
					if (getPositionNumber() == 20) {
						if (getPositionNumberOfOtherTrain() == 20) {
							colorThread.setEnd(false);
						} else {
							colorThread.setEnd(true);
							setPassed(false);
						}
					}
					if (getPositionNumber() == 30) { colorThread.setEnd(false);	}
					if (getPositionNumber() == 40) {
						if (getPositionNumberOfOtherTrain() != 40) {
							colorThread.setEnd(true);
							setPassed(false);
						} else {
							colorThread.setEnd(false);
						}
					}
					LCD.clear();
					LCD.drawInt(getPositionNumberOfOtherTrain(), 3, 3);
				}
				if (signal == 10) { /* Anderer Zug befindet sich an Position 1 */
					if (getPositionNumber() == 20) { colorThread.setEnd(false); setPassed(true); }
					if (getPositionNumber() == 30) {
						if (isHost()) {
							colorThread.setEnd(true);
						} else {
							colorThread.setEnd(false);	
						}
					}
					if (getPositionNumber() == 40) { colorThread.setEnd(true); setPassed(true); }
					setPositionNumberOfOtherTrain(signal);
				}
				if (signal == 20) { /* Anderer Zug befindet sich an Position 2 */
					if (getPositionNumber() == 10) { colorThread.setEnd(true); }
					if (getPositionNumber() == 30) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { colorThread.setEnd(true); }
					setPositionNumberOfOtherTrain(signal);
				}
				if (signal == 30) { /* Anderer Zug befindet sich an Position 3 */
					if (getPositionNumber() == 10) {
						if (isHost()) {
							colorThread.setEnd(true);
						} else {
							colorThread.setEnd(false);	
						}
					}
					if (getPositionNumber() == 20) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { colorThread.setEnd(false); setPassed(true); }
					setPositionNumberOfOtherTrain(signal);
				}
				if (signal == 40) { /* Anderer Zug befindet sich an Position 4 */
					if (getPositionNumber() == 10) { colorThread.setEnd(false); }
					if (getPositionNumber() == 20) { colorThread.setEnd(true); }
					if (getPositionNumber() == 30) { colorThread.setEnd(true); }
					setPositionNumberOfOtherTrain(signal);
				}
			}
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("listening error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}

	private void talkToOtherBrick() {
		try {
			if (hasPassed()) {
				getOutputStream().write(0);
			}
			if (isWaiting()) {
				getOutputStream().write(getPositionNumber());
			} else {
				getOutputStream().write(1);
			}
			getOutputStream().flush();

		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("signal error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}	
	}

	@Override
	public void run() {
		while (Button.readButtons() != Button.ID_ESCAPE) {
			listenToOtherBrick();
			talkToOtherBrick();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {}
		}

		LCD.clear();
		LCD.drawString("closing...", 3, 3);
	}

	/*
	 * --------------------------------------------------------------------------
	 */

	private InputStream getInputStream() {
		return inputStream;
	}

	private void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	private OutputStream getOutputStream() {
		return outputStream;
	}

	private void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	private boolean isHost() {
		return host;
	}

	private void setHost(boolean host) {
		this.host = host;
	}

	private void setColorThread(ColorThread colorThread) {
		this.colorThread = colorThread;
	}

	private boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public void setPositionNumber(int positionNumber) {
		this.positionNumber = positionNumber;
	}

	private int getPositionNumber() {
		return positionNumber;
	}
	
	public void setPositionNumberOfOtherTrain(int positionNumberOfOtherTrain) {
		this.positionNumberOfOtherTrain = positionNumberOfOtherTrain;
	}

	private int getPositionNumberOfOtherTrain() {
		return positionNumberOfOtherTrain;
	}

	private boolean hasPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}
}