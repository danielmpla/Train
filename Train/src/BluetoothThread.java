
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

	private static OutputStream outputStream; 
	private static InputStream inputStream;
	private static RemoteDevice brickConnector;
	private static BTConnection link;
	private static boolean host;
	
	private static String connecting = "connecting...";
	private static String waiting = "waiting...";
	private static String closing = "closing...";
	private static String connected = "connected :)";
	private static String failed = "failed :(";
	
	public BluetoothThread(boolean isHost) {
		
		setHost(isHost);
		
		if ( isHost() ) {
			connectToOtherBrick();
		}
		else {
			waitForConnection();
		}
	}
	
	private static void connectToOtherBrick() { 
		LCD.drawString(connecting, 3, 3);
		
		brickConnector = Bluetooth.getKnownDevice("NXT_3");

		if ( brickConnector == null ) {
		  LCD.clear();
		  LCD.drawString("No such device", 0, 0);
		  Button.waitForAnyPress();
		  System.exit(1);
		}

		link = Bluetooth.connect(brickConnector);

		if (link == null) {
		  LCD.clear();
		  LCD.drawString(failed, 0, 0);
		  Button.waitForAnyPress();
		  System.exit(1);
		}
		
		setOutputStream(link.openDataOutputStream()); 
		setInputStream(link.openDataInputStream());
		
		LCD.clear();
		LCD.drawString(connected, 3, 3);
	}
	private static void waitForConnection() { 
		LCD.drawString(waiting, 3, 3); 
		
		link = Bluetooth.waitForConnection(5000, NXTConnection.PACKET);
		
		if (link == null) {
			  LCD.clear();
			  LCD.drawString(failed, 0, 0);
			  Button.waitForAnyPress();
			  System.exit(1);
			}
		
		setOutputStream(link.openDataOutputStream()); 
		setInputStream(link.openDataInputStream());
		
		LCD.clear();
		LCD.drawString(connected, 3, 3);
	}
	
	private static void listenToOtherBrick() {
		try {
			
			int signal = getInputStream().read();
			
			if(signal == 0) { /* anderer Zug fährt */ }
			if(signal == 1) { /* anderer Zug wartet an Weiche */ }
			if(signal == 2) { /* anderer Zug ist vorbei gefahren */ }

		} catch (IOException e) {
			LCD.drawString("Communication error", 0, 0);
			Button.waitForAnyPress();
			System.exit(1);
		}
	}
	
	private static void talkToOtherBrick() {
		try {

			if(true /* Zug fährt */ ) { getOutputStream().write(0); }
			if(true /* Warte an Weiche */ ) { getOutputStream().write(1); }
			if(true /* Zug fährt vorbei */ ) { getOutputStream().write(2); }
			
		} catch (IOException e) {
			LCD.drawString("Communication error", 0, 0);
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
		LCD.drawString(closing, 3, 3);
	}
	
	/* --------------------------------------------------------------------------- */

	public static InputStream getInputStream() {
		return inputStream;
	}

	public static void setInputStream(InputStream inputStream) {
		BluetoothThread.inputStream = inputStream;
	}

	public static OutputStream getOutputStream() {
		return outputStream;
	}

	public static void setOutputStream(OutputStream outputStream) {
		BluetoothThread.outputStream = outputStream;
	}

	public static boolean isHost() {
		return host;
	}

	public static void setHost(boolean host) {
		BluetoothThread.host = host;
	}
	
}
