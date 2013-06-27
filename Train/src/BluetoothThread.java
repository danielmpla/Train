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
	private boolean waiting;
	private int positionNumber; // 4 verschiedene Haltepunkte
	private boolean passed;
	private int lastPositionOfOtherTrain;

	public BluetoothThread(boolean isHost, ColorThread colorThread) {
		LCD.clear();
		setHost(isHost);
		setColorThread(colorThread);
		setWaiting(false);
		setPassed(false);

		if (isHost()) {
			connectToOtherBrick();
		} else {
			waitForConnection();
		}
	}

	private void connectToOtherBrick() {
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

		LCD.clear();
		LCD.drawString("success :)", 3, 3);
	}

	private void waitForConnection() {
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

		LCD.clear();
		LCD.drawString("success :)", 3, 3);
	}

	private void listenToOtherBrick() {
		try {
			if (getInputStream().available() > 0) {
				int signal = getInputStream().read();

				if (signal == 0) { /* FAHR WEITER */
					colorThread.setEnd(true);
				}
				if (signal == 1) { /* ANDERER ZUG FÄHRT */
					if (getPositionNumber() == 10) { 	}
					if (getPositionNumber() == 20) { colorThread.setEnd(true); setPassed(false); }
					if (getPositionNumber() == 30) { 	}
					if (getPositionNumber() == 40) { colorThread.setEnd(true); setPassed(false); }

				}
				if (signal == 10) { /* Anderer Zug befindet sich an Position 1 */
					if (getPositionNumber() == 10) {		}
					if (getPositionNumber() == 20) { setPassed(true); }
					if (getPositionNumber() == 30) {
						if (isHost()) {
							colorThread.setEnd(true);
						}
					}
					if (getPositionNumber() == 40) { setPassed(true); }
					
					lastPositionOfOtherTrain = signal;
				}
				if (signal == 20) { /* anderer Zug wartet an Position 2 */
					if (getPositionNumber() == 10) {	}
					if (getPositionNumber() == 30) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { colorThread.setEnd(true); }
					lastPositionOfOtherTrain = signal;
				}
				if (signal == 30) { /* anderer Zug wartet an Position 3 */
					if (getPositionNumber() == 10) {
						if (isHost()) {
							colorThread.setEnd(true);
						}
					}
					if (getPositionNumber() == 20) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { setPassed(true); }
					lastPositionOfOtherTrain = signal;
				}
				if (signal == 40) { /* anderer Zug wartet an Position 4 */
					if (getPositionNumber() == 10) { colorThread.setEnd(true); }
					if (getPositionNumber() == 20) {	}
					if (getPositionNumber() == 30) {    }
					if (getPositionNumber() == 40) {	}
					lastPositionOfOtherTrain = signal;
				}

				LCD.drawInt(getPositionNumber(), 3, 4);
				LCD.drawInt(signal, 3, 5);
			}
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("Listening Error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}

	private void talkToOtherBrick() {
		try {
			if (hasPassed()) {
				LCD.drawInt(0, 3, 6);
				getOutputStream().write(0);
				getOutputStream().flush();
			}
			if (isWaiting()) {
				LCD.drawInt(getPositionNumber(), 3, 6);
				getOutputStream().write(getPositionNumber());
				getOutputStream().flush();
			} else {
				LCD.drawInt(1, 3, 6);
				getOutputStream().write(1);
				getOutputStream().flush();
			}

		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("Talking Error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}

	}

	@Override
	public void run() {

		while (Button.readButtons() != Button.ID_ESCAPE) {
			listenToOtherBrick();
			talkToOtherBrick();
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

	private boolean hasPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}
}