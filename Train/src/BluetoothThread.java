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
 * Cares about the communication between the NXT-Bricks.
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
	private boolean acknowledgeToSend;
	private boolean signalTransmitted;
	private boolean needToSend;
	private int positionNumber; // there are four different waiting points
	private long currentTime;

	public BluetoothThread(boolean isHost, ColorThread colorThread) {
		LCD.clear();

		setHost(isHost);
		setColorThread(colorThread);
		setWaiting(false);

		setUpConnection();
	}

	/**
	 * Sets up the connection between the NXT-Bricks.
	 * 
	 * */
	private void setUpConnection() {
		if (isHost()) {
			connectToOtherBrick("NXT4");
		} else {
			waitForConnection();
		}

		checkConnection();

		setOutputStream(link.openDataOutputStream());
		setInputStream(link.openDataInputStream());

		LCD.clear();
		LCD.drawString("connected", 3, 3);
	}

	/**
	 * Active method which connects to another NXT-Brick.
	 * 
	 * @param String
	 *            nxtName
	 * */
	private void connectToOtherBrick(String nxtName) {
		LCD.drawString("connecting...", 3, 3);

		brickConnector = Bluetooth.getKnownDevice(nxtName);

		if (brickConnector == null) {
			LCD.clear();
			LCD.drawString("Unknown device", 3, 3);
			LCD.drawString("NXT4", 3, 4);
			Button.waitForAnyPress();
			System.exit(0);
		}

		link = Bluetooth.connect(brickConnector);
	}

	/**
	 * Passive method which waits for another NXT-Brick to connect.
	 * 
	 * */
	private void waitForConnection() {
		LCD.clear();
		LCD.drawString("waiting...", 3, 3);

		link = Bluetooth.waitForConnection(15000, NXTConnection.PACKET);
	}

	/**
	 * Checks if there is a link between the NXT-Bricks.
	 * 
	 * */
	private void checkConnection() {
		if (link == null) {
			LCD.clear();
			LCD.drawString("No Connection", 3, 3);
			Button.waitForAnyPress();
			System.exit(0);
		}
	}

	/**
	 * Method which reads the signals from inputStream.
	 * 
	 * */
	private void listenToOtherBrick() {
		try {
			if (getInputStream().available() > 0) {
				int signal = getInputStream().read();
				setInputStream(link.openInputStream());
				identify(signal);
			}
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("listening error", 2, 3);
		}
	}

	/**
	 * Identifies the given signal and decides if the train can roll on.
	 * 
	 * @param int givenSignal
	 * */
	private void identify(int givenSignal) throws IOException {
		if (givenSignal == 1) { /* Anderer Zug befindet sich im Großhandelsbereich */
			if (getPositionNumber() == 2) { }
			if (getPositionNumber() == 3) { }
			if (getPositionNumber() == 10) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 20) { colorThread.setSignalGo(false); }
			if (getPositionNumber() == 30) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 40) { colorThread.setSignalGo(true);	setPositionNumber(3); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 2) { /* Anderer Zug befindet sich im Kreuzungsbereich */
			if (getPositionNumber() == 1) { }
			if (getPositionNumber() == 3) { }
			if (getPositionNumber() == 10) { colorThread.setSignalGo(false); }
			if (getPositionNumber() == 20) { colorThread.setSignalGo(true); setPositionNumber(1); }
			if (getPositionNumber() == 30) { colorThread.setSignalGo(false); } 
			if (getPositionNumber() == 40) { colorThread.setSignalGo(true);	setPositionNumber(3); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 3) { /* Anderer Zug befindet sich im Kundenbereich */
			if (getPositionNumber() == 1) { }
			if (getPositionNumber() == 2) { }
			if (getPositionNumber() == 10) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 20) { colorThread.setSignalGo(true); setPositionNumber(1); }
			if (getPositionNumber() == 30) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 40) { colorThread.setSignalGo(false); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 10) { /* Anderer Zug befindet sich an Position 1 */
			if (getPositionNumber() == 1) { }
			if (getPositionNumber() == 2) { }
			if (getPositionNumber() == 3) { }
			if (getPositionNumber() == 20) { colorThread.setSignalGo(true); setPositionNumber(1); }
			if (getPositionNumber() == 30) {
				if (isHost()) {
					colorThread.setSignalGo(true);
					setPositionNumber(2); 
				} else {
					colorThread.setSignalGo(false);
				}
			}
			if (getPositionNumber() == 40) { colorThread.setSignalGo(true); setPositionNumber(3); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 20) { /* Anderer Zug befindet sich an Position 2 */
			if (getPositionNumber() == 1) { }
			if (getPositionNumber() == 2) { }
			if (getPositionNumber() == 3) { }
			if (getPositionNumber() == 10) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 30) { colorThread.setSignalGo(true);	setPositionNumber(2); }
			if (getPositionNumber() == 40) { colorThread.setSignalGo(true); setPositionNumber(3); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 30) { /* Anderer Zug befindet sich an Position 3 */
			if (getPositionNumber() == 1) { }
			if (getPositionNumber() == 2) { }
			if (getPositionNumber() == 3) { }
			if (getPositionNumber() == 10) {
				if (isHost()) {
					colorThread.setSignalGo(true);
					setPositionNumber(2); 
				} else {
					colorThread.setSignalGo(false);
				}
			}
			if (getPositionNumber() == 20) { colorThread.setSignalGo(true);	setPositionNumber(1); }
			if (getPositionNumber() == 40) { colorThread.setSignalGo(true); setPositionNumber(3); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 40) { /* Anderer Zug befindet sich an Position 4 */
			if (getPositionNumber() == 10) { colorThread.setSignalGo(true); setPositionNumber(2); }
			if (getPositionNumber() == 20) { colorThread.setSignalGo(true); setPositionNumber(1); }
			if (getPositionNumber() == 30) { colorThread.setSignalGo(true); setPositionNumber(2); }
			setAcknowledgeToSend(true);
		}
		if (givenSignal == 666) {
			setSignalTransmitted(true);
			setNeedToSend(false);
		}
	}

	/**
	 * Method which decides what should be send.
	 * 
	 * */
	private void talkToOtherBrick() {
		if (isWaiting()) {
			sendSignal();
		} else if (((System.currentTimeMillis() / 1000) % 5) == 0) {
			sendSignal();
		} else {}
	}

	/**
	 * Sends all the necessary position signals to the other NXT-Brick.
	 * 
	 * */
	private void sendSignal() {
		try {
			getOutputStream().write(getPositionNumber());
			getOutputStream().flush();
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("signal error", 2, 3);
		}
	}

	/**
	 * Sends the acknowledge if there was a signal from the other NXT-Brick.
	 * 
	 * */
	private void sendAcknowledge() {
		try {
			getOutputStream().write(666);
			getOutputStream().flush();
			setAcknowledgeToSend(false);
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString("acknowledge error", 2, 3);
		}
	}

	/** 
	 * 
	 * */
	@Override
	public void run() {
		while (Button.readButtons() != Button.ID_ESCAPE) {

			currentTime = System.currentTimeMillis();

			while (currentTime + 500 > System.currentTimeMillis()) {
				listenToOtherBrick();
				if (haveAnAcknowledgeToSend() == true) {
					sendAcknowledge();
				}
				if (isSignalTransmitted() == true) {
					break;
				}
			}

			if (isNeedToSend() == true) {
				talkToOtherBrick();
			}
		}
	}

	/* ------------------------Getter & SETTER --------------------------- */

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
		if (waiting == true) {
			this.needToSend = true;
		}
	}

	private boolean haveAnAcknowledgeToSend() {
		return acknowledgeToSend;
	}

	private void setAcknowledgeToSend(boolean acknowledge) {
		this.acknowledgeToSend = acknowledge;
	}
	
	private boolean isSignalTransmitted() {
		return signalTransmitted;
	}
	
	private void setSignalTransmitted(boolean signalTransmitted) {
		this.signalTransmitted = signalTransmitted;
	}

	private boolean isNeedToSend() {
		return needToSend;
	}

	private void setNeedToSend(boolean needToSend) {
		this.needToSend = needToSend;
	}

	public void setPositionNumber(int positionNumber) {
		this.positionNumber = positionNumber;
	}

	private int getPositionNumber() {
		return positionNumber;
	}



}