import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {

    class block {
        int x, y, width, height;
        Image img;
        boolean alive = true;
        boolean used = false;

        block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    // board
    int tileSize = 32;
    int rows = 16;
    int columns = 16;
    int boardWidth = tileSize * columns;
    int boardHeight = tileSize * rows;

    Image starbgImg;
    Image shipImg;
    Image alien1Img;
    Image alien2Img;
    Image alien3Img;
    Image alien4Img;
    ArrayList<Image> alienImgArray;

    // ship
    int shipwidth = tileSize * 2;
    int shipheight = tileSize;
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = boardHeight - tileSize * 2;
    int shipvelX = tileSize;

    block ship;

    // aliens
    ArrayList<block> alienArray;
    int alienwidth = tileSize * 2;
    int alienheight = tileSize;
    int alienvelX = 1;

    // bullet
    ArrayList<block> bullArray;
    int bullwidth = tileSize / 8;
    int bullheight = tileSize / 2;
    int bullvelY = -10;
    int alienRows = 3;    
    int alienColumns = 3;

    Timer gameLoop;
    int score = 0;
    boolean gameOver= false;
    private int alienCount;

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);


        starbgImg= new ImageIcon(getClass().getResource("./starbg.png")).getImage();
        shipImg = new ImageIcon(getClass().getResource("./ship.png")).getImage();
        alien1Img = new ImageIcon(getClass().getResource("./alien-1.png")).getImage();
        alien2Img = new ImageIcon(getClass().getResource("./alien-2.png")).getImage();
        alien3Img = new ImageIcon(getClass().getResource("./alien-3.png")).getImage();
        alien4Img = new ImageIcon(getClass().getResource("./alien-4.png")).getImage();

        alienImgArray = new ArrayList<>();
        alienImgArray.add(alien1Img);
        alienImgArray.add(alien2Img);
        alienImgArray.add(alien3Img);
        alienImgArray.add(alien4Img);

        ship = new block(shipX, shipY, shipwidth, shipheight, shipImg);
        alienArray = new ArrayList<>();
        bullArray = new ArrayList<>();

        // game timer
        gameLoop = new Timer(1000 / 60, this);
        createAliens();
        repaint();
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {


        g.drawImage(starbgImg, 0, 0, boardWidth, boardHeight, null);

        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        // draw aliens
        for (block alien : alienArray) {
            if (alien.alive) {
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        // draw bullets
        g.setColor(Color.WHITE);
        for (block bullet : bullArray) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score),10,35);
        } else {
            g.drawString(String.valueOf(score), 10,32);
        }
    }

    public void move() {
        // aliens
        for (int i = 0; i < alienArray.size(); i++) {
            block alien = alienArray.get(i);
            if (alien.alive) {
                alien.x += alienvelX;

                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienvelX *= -1;
                    alien.x += alienvelX * 2;

                    // move all aliens down by one row
                    for (block a : alienArray) {
                        a.y += alienheight;
                    }
                }
                if(alien.y >= ship.y){
                    gameOver = true;
                }
            }
        }

        // bullets
        for (int i = 0; i < bullArray.size(); i++) {
            block bullet = bullArray.get(i);
             bullet.y += bullvelY;

            // bullet-alien collision
            for(block alien : alienArray){
                if(!bullet.used && alien.alive && detectCollision(alien, bullet)){
                    bullet.used = true;
                    alien.alive = false;
                    alienCount--;
                    score += 100; 
                }
            }

            // remove used or offscreen bullets
            while (!bullArray.isEmpty() && (bullArray.get(0).used || bullArray.get(0).y < 0)) {
                bullArray.remove(0);
            }
        }

        // next level
        if(alienCount == 0){
            score += alienColumns * alienRows * 100;
            alienColumns = Math.min(alienColumns + 1, columns / 2 - 2);
            alienRows = Math.min(alienRows + 1, rows - 6);
            alienArray.clear();
            bullArray.clear();
            alienvelX = 1;
            createAliens();
        }
    }

    public void createAliens() {
        Random random = new Random();
        int alienX = 10;      
        int alienY = 10;
        int alienWidth = tileSize;  // smaller width for better spacing
        int alienHeight = tileSize;

        alienArray = new ArrayList<>();

        for (int r = 0; r < alienRows; r++) {
            for (int c = 0; c < alienColumns; c++) {
                int randomImgIndex = random.nextInt(alienImgArray.size());
                block alien = new block(
                    alienX + c * (alienWidth + 8), // spacing between aliens
                    alienY + r * (alienHeight + 8),
                    alienWidth,
                    alienHeight,
                    alienImgArray.get(randomImgIndex)
                );
                alienArray.add(alien);
            }
        }
        alienCount = alienArray.size();
    }

    // MAIN METHOD
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders");
        SpaceInvaders gamePanel = new SpaceInvaders();
    
        frame.setContentPane(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public boolean detectCollision(block a, block b){
        return a.x < b.x + b.width &&
               a.x + a.width > b.x && 
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if(gameOver){
            ship.x = shipX;
            alienArray.clear();
            bullArray.clear();
            score=0;
            alienvelX=1;
            alienColumns = 3;
            alienRows = 2;
            gameOver = false;
            createAliens();
            gameLoop.start();
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            ship.x -= shipvelX;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            ship.x += shipvelX;
        }

        // clamp ship position
        if (ship.x < 0) ship.x = 0;
        if (ship.x > boardWidth - ship.width) ship.x = boardWidth - ship.width;

        // shoot bullets
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            block bullet = new block(ship.x + shipwidth * 15 / 32, ship.y, bullwidth, bullheight, null);
            bullArray.add(bullet);
        }
    }
}
