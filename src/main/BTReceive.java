package main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * Receive data from another NXT, a PC, a phone, 
 * or another bluetooth device.
 * 
 * Waits for a connection, receives an int and returns
 * its negative as a reply, 100 times, and then closes
 * the connection, and waits for a new one.
 * 
 * @author Lawrie Griffiths
 *
 */
public class BTReceive {

	private DataInputStream dis;
	private DataOutputStream dos;
	private String connected = "Connected";
	private String waiting = "Waiting...";
	private String closing = "Closing...";
	private BTConnection btc;
	private boolean stop;

	public static void main(String [] args)  throws Exception 
	{
		BTReceive btreceive = new BTReceive();

		LCD.drawString(btreceive.waiting,0,0);
		LCD.refresh();

		btreceive.btc = Bluetooth.waitForConnection();

		LCD.clear();
		LCD.drawString(btreceive.connected,0,0);
		LCD.refresh();	

		btreceive.dis = btreceive.btc.openDataInputStream();
		btreceive.dos = btreceive.btc.openDataOutputStream();

		Motor.B.resetTachoCount();
		Motor.A.resetTachoCount();
		Motor.C.resetTachoCount();

		while (!btreceive.stop)
		{
			btreceive.waitForData();	
		}
	}

	public void waitForData(){

		int n;
		
		try {

			n = dis.readInt();
			if (n == 4000){
				beep();
			}else if (n == 4001){
				beep2();
			}else if (n == -6666){
				endApplication();
			}else if (Math.abs(n) <= 100){
				moveMotorA(n);
			}else{
				moveMotorB(n);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void moveMotorA(final int value)
	{
		new Thread(){
			@Override
			public void run(){
				LCD.clear();
				LCD.drawString(String.valueOf(value), 0, 0);
				if (value > 0){
					Motor.A.forward();
					//Motor.A.setSpeed(720*(value/100));
					Motor.A.setSpeed(value*10);
				}else if (value < 0){
					Motor.A.backward();
					//Motor.A.setSpeed(720*(value/100));
					Motor.A.setSpeed(value*10);
				}else{
					if (!Motor.A.isStalled()){
						Motor.A.stop();
					}
				}
				LCD.refresh();
			}
		}.start();
	}

	public void moveMotorB(final int value)
	{
		new Thread(){
			@Override
			public void run(){
				//LCD.clear();
				LCD.drawString(String.valueOf(value), 5, 5);
				//LCD.drawInt(Motor.B.getTachoCount(), 0, 1);
				if ((value > 0) && (value != 3000)){
					if (Motor.B.getTachoCount() <= 2){
						Motor.B.forward();
						Motor.B.setSpeed(value);
					}
				}else if (value < 0){
					Motor.B.backward();
					Motor.B.setSpeed(value);
				}else{
					if (!Motor.B.isStalled()){
						Motor.B.stop();
					}
				}
				LCD.refresh();
			}
		}.start();
	}

	public void beep2(){
		new Thread(){
			@Override
			public void run(){
				LCD.clear();
				LCD.drawString("BEEP.....2",0,0);
				Sound.playNote(Sound.PIANO,440,500);
				Sound.playNote(Sound.PIANO,880,500);
				Sound.playNote(Sound.PIANO,784,250);
				Sound.playNote(Sound.PIANO,880,250);
				Motor.B.stop(true);
				LCD.refresh();
			}
		}.start();
	}

	public void beep(){

		new Thread(){

			@Override
			public void run(){
				LCD.clear();
				LCD.drawString("BEEP.....1",0,0);
				Sound.playNote(Sound.PIANO,880,250);
				Sound.playNote(Sound.PIANO,784,250);
				Sound.playNote(Sound.PIANO,880,500);
				Sound.playNote(Sound.PIANO,440,500);
				LCD.refresh();		
			}
		}.start();
	}

	public void endApplication(){

		try {
			dis.close();
			dos.close();
			Thread.sleep(100); // wait for data to drain
			LCD.clear();
			LCD.drawString(closing,0,0);
			LCD.refresh();
			btc.close();
			LCD.clear();
			stop = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

