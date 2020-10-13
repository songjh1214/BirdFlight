package online;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import main.Birdbot;
import main.Bullet;
import main.CoinDrop;
import main.GameInstance;
import main.Player;
import mainGUI.MainMenu;
import mainGUI.PlayerData;
import mainGUI.SystemData;
import birds.*;

public class ClientBoard extends JPanel implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9096180433067674090L; //����ȭ ����.

	public Image backgroundImg; //���ȭ��

	Timer time;  //�ð�

	int backgroundPos1; //���ȭ��ũ��
	int backgroundPos2; //���ȭ��ũ��
	final int scrollSpeed = 2; //���ȭ�� �������� �ӵ�

	public Player p; //������Ű�� player p�� ����.
	public GameInstance game; //������Ű�� gameinstance���� game�� ����.


	// ������
	public ClientBoard(GameInstance game, Player p) throws InterruptedException { //���ܹ߻��� interruptedexception ���� ���� 
		this.game = game;
		this.p = p;
		setupGame();	
		time = new Timer(1000/80, this);
		time.start();
	} // ��Ŭ���̾�Ʈ ���尡 �����ɶ� �Ű����� 2���� �����ͼ� ���� �Ѱ���.

	public void setupGame(){
		this.backgroundPos1 = 0;
		this.backgroundPos2 = 1200;
		addKeyListener(new AL()); // Ű�����ʸ� �߰���. (Ű������,Ű������)
	} //���ȭ�� ũ�⼳���ϴ� �κ��ε�.



	// �� Ÿ�̸� ƽ.
	public void actionPerformed(ActionEvent e) {
		if (game.gameStatus >= 0){
			p.update(); //�÷��̾� ���¾�����Ʈ
			updateBackground(); // ���ȭ�� ���� �ؿ� �Լ� ����.
			repaint(); //�ٽ� �׸��°�.
		}
		else{
			time.stop(); //������ �װų� ������
			PlayerData.coins += game.coinsCollected; //�������ΰ���
			if (game.score > PlayerData.highscore) PlayerData.highscore = game.score; //���ھ��
			PlayerData.saveData(); //�÷��̾���� ���̺�
			MainMenu.jtp.setSelectedIndex(1); //�����𸣰���.
			MainMenu.jtp.setSelectedIndex(0); //�����𸣰���.
		}
		//System.out.println(this.p.ID);
	}


	public void paint(Graphics g) {
		//���ȭ��
		g.clearRect(0, 0, MainMenu.XFRAME, MainMenu.YFRAME);
		g.drawImage(SystemData.backgroundImg, 1200 - backgroundPos1, 0, null); //�ֿ��������� �𸣰���.
		g.drawImage(SystemData.backgroundImg, 1200 - backgroundPos2, 0, null);

		//ü�¹�/������
		g.setColor(Color.red);
		g.fillRect(750, 20, (int) p.maxMana, 10);
		g.fillRect(50, 20, (int) p.maxHealth, 10);
		g.setColor(Color.blue);
		g.fillRect(750, 20, (int) p.mana, 10);
		g.setColor(Color.green);
		g.fillRect(50, 20, (int) p.health, 10);

		// �ؽ�Ʈ ũ��,�۲� ����
		g.setColor(Color.black);
		g.setFont(new Font("Serif", Font.PLAIN, 24)); 
		g.drawImage(SystemData.trophyIcon, 10, 40, null);
		g.drawString(Integer.toString(game.score), 65, 75);
		g.drawImage(SystemData.coinImage, 10, 100, null);
		g.drawString(Integer.toString(game.coinsCollected), 65, 135);

		// �÷��̾�
		for (Player p : game.players){
			p.draw(g, SystemData.playerImages, SystemData.bulletImages);
		}

		// ��
		for (EnemyUnit e : game.birds){
			e.draw(g, SystemData.birdImages);
		}

		// ��
		for (Birdbot b : game.bots){
			b.draw(g, SystemData.playerImages, SystemData.bulletImages);
		}

		// ����
		for (CoinDrop d : game.drops){
			d.draw(g, SystemData.coinImage);
		}

		// �����ٸ�
		if (game.gameStatus == 0){
			g.setColor(Color.BLACK);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Connecting to server...", 475, 350);
		}

		// ������ٸ� , ����
		if (game.gameStatus == 1){
			g.setColor(Color.BLACK);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Waiting for players...", 475, 350);
		}

		// ��� �÷��̾� �����
		if (game.gameStatus == 3){
			g.setColor(Color.BLACK);
			g.drawImage(SystemData.defeatIcon, 500, 150, null);
			g.drawImage(SystemData.trophyIcon, 550, 375, null);
			g.drawImage(SystemData.coinImage, 550, 445, null);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Defeat", 560, 345);
			g.setFont(new Font("Arial", Font.PLAIN, 24)); 
			g.drawString(Integer.toString(game.score), 610, 410);
			g.drawString(Integer.toString(game.coinsCollected), 610, 480);
		}
		if (game.gameStatus == 4){
			g.setColor(Color.BLACK);
			g.drawImage(SystemData.victoryIcon, 500, 150, null);
			g.drawImage(SystemData.trophyIcon, 550, 375, null);
			g.drawImage(SystemData.coinImage, 550, 445, null);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Victory", 555, 345);
			g.setFont(new Font("Arial", Font.PLAIN, 24)); 
			g.drawString(Integer.toString(game.score), 610, 410);
			g.drawString(Integer.toString(game.coinsCollected), 610, 480);
		}
	}

	public void updateGame(GameInstance gi){
		for (int i = 0; i < gi.players.size(); i++){
			Player PI = gi.players.get(i);
			if(PI.ID.equals(p.ID)){
				// �ǰ� ������ �Ѿ��� ����.??
				if (PI.health < p.health)	p.health = PI.health;
				ArrayList<Bullet> bulletRemoveList = new ArrayList<Bullet>();
				for (Bullet b : p.bullets){
					for (Bullet pb : PI.bullets){
						if (b.BPID == pb.BPID && !pb.live){
							bulletRemoveList.add(b);
						}
					}
				}
				p.bullets.removeAll(bulletRemoveList);
			}
		}

		// ���� ������Ʈ ������Ʈ
		this.game.birds = gi.birds;
		this.game.players = gi.players;
		this.game.bots = gi.bots;
		this.game.drops = gi.drops;
		this.game.gameStatus = gi.gameStatus;
		this.game.coinsCollected = gi.coinsCollected;
		this.game.score = gi.score;
	}

	public void updateBackground(){
		if (backgroundPos1 > 1200){
			backgroundPos1 = 0;
			backgroundPos2 = 1200;
		}
		backgroundPos1 += scrollSpeed;
		backgroundPos2 += scrollSpeed;
	} // ���ȭ�� �ӵ��� ���鰥���� �������µ�. Ȯ����������.

	private class AL extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			p.keyReleased(e);
		} // Ű�� ������ ������

		public void keyPressed(KeyEvent e) {
			p.keyPressed(e);
		} //Ű�� ��������
	}

}