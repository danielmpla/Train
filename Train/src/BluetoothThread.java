
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
	
	private boolean host;
	private boolean waiting;
	private boolean passed;
	
	public BluetoothThread(boolean isHost) {
		
		setHost(isHost);
		setWaiting(false);
		setPassed(false);
		
		if ( isHost() ) {
			connectToOtherBrick();
		}
		else {
			waitForConnection();
		}
	}
	
	private void connectToOtherBrick() { 
		LCD.drawString("connecting...", 3, 3);
		
		brickConnector = Bluetooth.getKnownDevice("NXT_3");

		if ( brickConnector == null ) {
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
		
		link = Bluetooth.waitForConnection(5000, NXTConnection.PACKET);
		
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
			
			int signal = getInputStream().read();
			
			if(signal == 0) { /* anderer Zug fährt */
				// mach nichts
			}
			if(signal == 1) { /* anderer Zug wartet an Weiche */
				// mach nichts
			}
			if(signal == 2) { /* anderer Zug fährt vorbei */
				// fahr weiter !!
			}

		} catch (IOException e) {
			LCD.drawString("listening error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}
	
	private void talkToOtherBrick() {
		try {
			if( isWaiting() ) { getOutputStream().write(1); }
			else if ( hasPassed() ) { getOutputStream().write(2); }
			else { getOutputStream().write(0); }
			
			getOutputStream().flush();
		} catch (IOException e) {
			LCD.drawString("talking error", 3, 3);
			Button.waitForAnyPress();
			System.exit(1);
		}
		
	}
	
	@Override
	public void run() {
		
		while(Button.readButtons() != Button.ID_ESCAPE) {
			listenToOtherBrick();
			talkToOtherBrick();
		}
		
		LCD.clear();
		LCD.drawString("closing...", 3, 3);
	}
	
	/* --------------------------------------------------------------------------- */

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
	private boolean isWaiting() {
		return waiting;
	}
	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}
	private boolean hasPassed() {
		return passed;
	}
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
}