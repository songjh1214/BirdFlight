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
	private static final long serialVersionUID = -9096180433067674090L; //직렬화 연결.

	public Image backgroundImg; //배경화면

	Timer time;  //시간

	int backgroundPos1; //배경화면크기
	int backgroundPos2; //배경화면크기
	final int scrollSpeed = 2; //배경화면 지나가는 속도

	public Player p; //메인페키지 player p를 생성.
	public GameInstance game; //메인패키지 gameinstance에서 game를 생성.


	// 생성자
	public ClientBoard(GameInstance game, Player p) throws InterruptedException { //예외발생시 interruptedexception 으로 던짐 
		this.game = game;
		this.p = p;
		setupGame();	
		time = new Timer(1000/80, this);
		time.start();
	} // 이클라이언트 보드가 생성될때 매개변수 2개를 가져와서 값을 넘겨줌.

	public void setupGame(){
		this.backgroundPos1 = 0;
		this.backgroundPos2 = 1200;
		addKeyListener(new AL()); // 키리스너를 추가함. (키누를때,키눌릴때)
	} //배경화면 크기설정하는 부분인듯.



	// 각 타이머 틱.
	public void actionPerformed(ActionEvent e) {
		if (game.gameStatus >= 0){
			p.update(); //플레이어 상태업데이트
			updateBackground(); // 배경화면 관련 밑에 함수 참조.
			repaint(); //다시 그리는것.
		}
		else{
			time.stop(); //게임이 죽거나 끝낼때
			PlayerData.coins += game.coinsCollected; //누적코인갱신
			if (game.score > PlayerData.highscore) PlayerData.highscore = game.score; //스코어갱신
			PlayerData.saveData(); //플레이어데이터 세이브
			MainMenu.jtp.setSelectedIndex(1); //뭔지모르겟음.
			MainMenu.jtp.setSelectedIndex(0); //뭔지모르겟음.
		}
		//System.out.println(this.p.ID);
	}


	public void paint(Graphics g) {
		//배경화면
		g.clearRect(0, 0, MainMenu.XFRAME, MainMenu.YFRAME);
		g.drawImage(SystemData.backgroundImg, 1200 - backgroundPos1, 0, null); //왜오류나는지 모르겟음.
		g.drawImage(SystemData.backgroundImg, 1200 - backgroundPos2, 0, null);

		//체력바/마나바
		g.setColor(Color.red);
		g.fillRect(750, 20, (int) p.maxMana, 10);
		g.fillRect(50, 20, (int) p.maxHealth, 10);
		g.setColor(Color.blue);
		g.fillRect(750, 20, (int) p.mana, 10);
		g.setColor(Color.green);
		g.fillRect(50, 20, (int) p.health, 10);

		// 텍스트 크기,글꼴 지정
		g.setColor(Color.black);
		g.setFont(new Font("Serif", Font.PLAIN, 24)); 
		g.drawImage(SystemData.trophyIcon, 10, 40, null);
		g.drawString(Integer.toString(game.score), 65, 75);
		g.drawImage(SystemData.coinImage, 10, 100, null);
		g.drawString(Integer.toString(game.coinsCollected), 65, 135);

		// 플레이어
		for (Player p : game.players){
			p.draw(g, SystemData.playerImages, SystemData.bulletImages);
		}

		// 적
		for (EnemyUnit e : game.birds){
			e.draw(g, SystemData.birdImages);
		}

		// 봇
		for (Birdbot b : game.bots){
			b.draw(g, SystemData.playerImages, SystemData.bulletImages);
		}

		// 코인
		for (CoinDrop d : game.drops){
			d.draw(g, SystemData.coinImage);
		}

		// 연결기다림
		if (game.gameStatus == 0){
			g.setColor(Color.BLACK);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Connecting to server...", 475, 350);
		}

		// 서버기다림 , 시작
		if (game.gameStatus == 1){
			g.setColor(Color.BLACK);
			g.setFont(new Font("Calibri", Font.BOLD, 28)); 
			g.drawString("Waiting for players...", 475, 350);
		}

		// 모든 플레이어 사망시
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
				// 피가 많을때 총알이 제거.??
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

		// 게임 오브잭트 업데이트
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
	} // 배경화면 속도가 가면갈수록 빨라지는듯. 확실하진않음.

	private class AL extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			p.keyReleased(e);
		} // 키를 누르고 있을때

		public void keyPressed(KeyEvent e) {
			p.keyPressed(e);
		} //키를 눌럿을때
	}

}