package online;

import java.net.*;
import java.util.ConcurrentModificationException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.GameInstance;
import main.Player;
import mainGUI.MainMenu;
import mainGUI.PlayerData;


public class ClientGame extends Thread{


	public ClientBoard CB;
	final static int pingPerSeconds = 80; //초당 핑 80

	DatagramSocket socket; //udp 연결할때 쓰임
	ByteArrayInputStream byteIS;
	ObjectInputStream objectIS;
	ByteArrayOutputStream byteOS;
	ObjectOutputStream objectOS; //직렬화 연결시 쓰임(자세한내용 모름)
	byte[] playerData = new byte[8192]; //플레이어 데이터 8192바이트?
	byte[] gameData = new byte[100000]; // 게임데이터 100000바이트??

	InetAddress serverIP; 
	int serverPort;

	public static Thread writer;
	public static Thread reader; 

	public ClientGame(String serverIP, int serverPort) throws SocketException, InterruptedException, UnknownHostException{
		this.serverIP = InetAddress.getByName(serverIP);
		this.serverPort = serverPort;

		// 게임 정보 초기화
		socket = new DatagramSocket();
		GameInstance game = new GameInstance();

		String conPort = Integer.toString(socket.getLocalPort());
		Player p = new Player(PlayerData.playerName);
		game.addPlayer(p);
		p.playable = true;
		CB = new ClientBoard(game, p);	

		// 스레드 시작.
		writer = new ClientWriterThread();
		writer.start();
		reader = new ClientReaderThread();
		reader.start();
	}

	class ClientReaderThread extends Thread{
		public void run(){
			while(!Thread.currentThread().isInterrupted()){
				try {
					//Thread.sleep(pingRate);
					DatagramPacket incomingPacket = new DatagramPacket(gameData, gameData.length);
					socket.receive(incomingPacket);
					gameData = incomingPacket.getData();
					byteIS = new ByteArrayInputStream(gameData);
					objectIS = new ObjectInputStream(byteIS);
					GameInstance g = (GameInstance) objectIS.readObject();
					CB.updateGame(g);
				} catch (IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	class ClientWriterThread extends Thread{
		public void run(){
			while(!Thread.currentThread().isInterrupted()){
				//System.out.println(this.toString());
				try {		
					// 서버를 기다림.
					byteOS = new ByteArrayOutputStream();
					objectOS = new ObjectOutputStream(byteOS);
					objectOS.writeObject(CB.p);
					objectOS.flush();
					playerData = byteOS.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(playerData, playerData.length, serverIP, serverPort);
					socket.send(sendPacket);
					Thread.sleep(1000 / pingPerSeconds);

				} catch (ConcurrentModificationException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					//e.printStackTrace();
				}

			}	
		}
	}


}
