import lejos.nxt.Button;
import lejos.nxt.Motor;


public class Main {
	static final int RED = 0;
	static final int YELLOW = 3;
	static final int BLUE = 4;
	static final int GREEN = 1;
	static final int speed = 600;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}
	
	public int getSpeed(){
		return speed;
	}
	
	
	public Main(){
		//int actualSpeed = 300;
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);
		
		ColorThread colorThread = new ColorThread();
		colorThread.setDaemon(true);
		colorThread.start();
		
		GreenThread greenThread = new GreenThread(colorThread);
		greenThread.setDaemon(true);
		greenThread.start();
		
		Motor.B.forward();
		Motor.C.forward();
		
		while(Button.readButtons() != Button.ID_ESCAPE){
		}
		System.exit(0);
		try {
			colorThread.join();
			greenThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
