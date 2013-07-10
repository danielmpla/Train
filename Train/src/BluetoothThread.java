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
	private boolean gotAcknowledge;
	private boolean gotSignal;
	private int positionNumber; // 4 verschiedene Haltepunkte
	private boolean passed;

	public BluetoothThread(boolean isHost, ColorThread colorThread) {
		LCD.clear();
		setHost(isHost);
		setColorThread(colorThread);
		setWaiting(false);
		setPassed(false);
		setAcknowledge(true);
		gotSignal = false;

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
					gotSignal = true;
				}
				if (signal == 1) { /* ANDERER ZUG FÄHRT */
					if (getPositionNumber() == 10) { colorThread.setEnd(false);	}
					if (getPositionNumber() == 20) { colorThread.setEnd(true); setPassed(false); }
					if (getPositionNumber() == 30) { colorThread.setEnd(false);	}
					if (getPositionNumber() == 40) { colorThread.setEnd(true); setPassed(false); }
					gotSignal = true;
				}
				if (signal == 2) { // ANDERER ZUG HAT SIGNAL ERHALTEN UND BESTÄTIGUNG GESENDET
					setAcknowledge(true);
				}
				if (signal == 10) { /* Anderer Zug befindet sich an Position 1 */
					if (getPositionNumber() == 10) { colorThread.setEnd(false); }
					if (getPositionNumber() == 20) { colorThread.setEnd(false); setPassed(true); }
					if (getPositionNumber() == 30) {
						if (isHost()) {
							colorThread.setEnd(true);
						} else {
							colorThread.setEnd(false);	
						}
					}
					if (getPositionNumber() == 40) { colorThread.setEnd(true); setPassed(true); }
					gotSignal = true;
				}
				if (signal == 20) { /* Anderer Zug befindet sich an Position 2 */
					if (getPositionNumber() == 10) { colorThread.setEnd(true); }
					if (getPositionNumber() == 20) { colorThread.setEnd(false); }
					if (getPositionNumber() == 30) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { colorThread.setEnd(true); }
					gotSignal = true;
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
					if (getPositionNumber() == 30) { colorThread.setEnd(false); }
					if (getPositionNumber() == 40) { colorThread.setEnd(false); setPassed(true); }
					gotSignal = true;
				}
				if (signal == 40) { /* Anderer Zug befindet sich an Position 4 */
					if (getPositionNumber() == 10) { colorThread.setEnd(false); }
					if (getPositionNumber() == 20) { colorThread.setEnd(true); }
					if (getPositionNumber() == 30) { colorThread.setEnd(true); }
					if (getPositionNumber() == 40) { colorThread.setEnd(false); }
					gotSignal = true;
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
		if (gotAcknowledge == true) {
			sendControlSignal();
		}
		if (gotSignal == true) {
			sendAcknowledge();
		}
	}
	
	private void sendControlSignal() {
		try {
			if (hasPassed()) {
				getOutputStream().write(0);
			} else if (isWaiting()) {
				getOutputStream().write(getPositionNumber());
			} else {
				getOutputStream().write(1);
			}
			getOutputStream().flush();
			setAcknowledge(false);

		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("control signal error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}
	private void sendAcknowledge() {
		try {
			if (gotSignal) {
				getOutputStream().write(2);
				gotSignal = false;
			}
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("acknowledge error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		while (Button.readButtons() != Button.ID_ESCAPE) {
			Long time = System.currentTimeMillis();
			
			// Wartet 0,5s auf ein Acknowledge, sonst sendet er das Signal erneut
			while (System.currentTimeMillis() >= time + 500) {
				if (gotAcknowledge == false) {
					listenToOtherBrick();
				}
			}
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
	
	private void setAcknowledge(boolean acknowledge) {
		gotAcknowledge = acknowledge;
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