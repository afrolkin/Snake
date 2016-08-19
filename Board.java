import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Stack;

//         FIFO LINKED LIST / STACK:
//              snake grows one square -> push one square on top of the stack (same coordinates as the apple)
//              snake moves one square -> push one element on top and pop one element off the bottom of stack
//          rename playerCell as playercell
//          Instead of having a playerCell object, have a stack of playerCell objects initially containing only one - every time it grows, push a new playerCell onto the stack
// TODO: utils method                               ?
// TODO: make more files for classes + refactor     7

public class Board extends JPanel implements ActionListener {
    // render loop
    private Timer loop;
    // update loop
    private Timer loop2;

    // timer for flashing text
    private Timer loop3;

    private boolean flash = false;

    // height and width in pixels
    private final int PIXEL_WIDTH = 800;
    private final int PIXEL_HEIGHT = 600;

    // each cell is 10x10 pixels
    private final int CELL_SIZE = 10;
    // board size is 80x60 cells
    private final int CELL_WIDTH = 80;
    private final int CELL_HEIGHT = 60;

    // 2d array to keep track of occupied cells by player/obstacles
    private final boolean cellOccupied[][] = new boolean[CELL_WIDTH][CELL_HEIGHT];

    private enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }

    private enum State {
        RUNNING, PAUSED, GAMEOVER, SPLASH
    }

    //framerate
    private long now;
    private int framesCount = 0;
    private int framesCountAvg = 0;
    private long framesTimer = 0;

    private State state = State.SPLASH;

    private int score = 0;

    private int userSpeed;

    // the playerCell on the board
    private Player player;

    // the apple on the board
    private Apple apple;

    // obstacles for advanced mode
    private Obstacle[] obstacles = new Obstacle[52];

    private boolean advancedMode = false;

    public Board(int userFrameRate, int userSpeed) {
        addKeyListener(new KAdapter());
        setBackground(Color.black);
        setFocusable(true);

        this.userSpeed = userSpeed;

        setPreferredSize(new Dimension(PIXEL_WIDTH, PIXEL_HEIGHT));

        // the render loop
        // actionperformed gets called every 1sec/userFrameRate seconds
        loop = new Timer(1000/userFrameRate, this);
        loop.start();

        // logic loop
        loop2 = new Timer(1000/(10 * userSpeed), this);
        loop2.start();

        // flashing loop (for flashing text)
        loop3 = new Timer(500, this);
        loop3.start();
    }

    private int convertCellXToPixelX(int cellX) {
        return cellX * CELL_SIZE;
    }

    private int convertCellYToPixelY(int cellY) {
        return cellY * CELL_SIZE;
    }

    private void init() {
        // create a new playerCell
        state = State.RUNNING;
        score = 0;
        // start player in center with a random direction
        player = new Player(40, 30, getRandomDirection());

        // create obstacles for advanced mode
        if (advancedMode) {
            createNewObstacles();
        }

        // create the apple in a random spot
        generateNewApple();
    }

    // draw obstacles for advanced mode
    private void createNewObstacles() {
        // bottom left obstacles
        obstacles[0] = new Obstacle(0, 53, Color.white);
        obstacles[1] = new Obstacle(0, 54, Color.white);
        obstacles[2] = new Obstacle(0, 55, Color.white);
        obstacles[3] = new Obstacle(0, 56, Color.white);
        obstacles[4] = new Obstacle(0, 57, Color.white);
        obstacles[5] = new Obstacle(0, 58, Color.white);
        obstacles[6] = new Obstacle(0, 59, Color.white);
        obstacles[7] = new Obstacle(1, 59, Color.white);
        obstacles[8] = new Obstacle(2, 59, Color.white);
        obstacles[9] = new Obstacle(3, 59, Color.white);
        obstacles[10] = new Obstacle(4, 59, Color.white);
        obstacles[11] = new Obstacle(5, 59, Color.white);
        obstacles[12] = new Obstacle(6, 59, Color.white);

        // bottom right obstacles
        obstacles[13] = new Obstacle(73, 59, Color.white);
        obstacles[14] = new Obstacle(74, 59, Color.white);
        obstacles[15] = new Obstacle(75, 59, Color.white);
        obstacles[16] = new Obstacle(76, 59, Color.white);
        obstacles[17] = new Obstacle(77, 59, Color.white);
        obstacles[18] = new Obstacle(78, 59, Color.white);
        obstacles[19] = new Obstacle(79, 59, Color.white);
        obstacles[20] = new Obstacle(79, 58, Color.white);
        obstacles[21] = new Obstacle(79, 57, Color.white);
        obstacles[22] = new Obstacle(79, 56, Color.white);
        obstacles[23] = new Obstacle(79, 55, Color.white);
        obstacles[24] = new Obstacle(79, 54, Color.white);
        obstacles[25] = new Obstacle(79, 53, Color.white);

        // top right obstacles
        obstacles[26] = new Obstacle(73, 0, Color.white);
        obstacles[27] = new Obstacle(74, 0, Color.white);
        obstacles[28] = new Obstacle(75, 0, Color.white);
        obstacles[29] = new Obstacle(76, 0, Color.white);
        obstacles[30] = new Obstacle(77, 0, Color.white);
        obstacles[31] = new Obstacle(78, 0, Color.white);
        obstacles[32] = new Obstacle(79, 6, Color.white);
        obstacles[33] = new Obstacle(79, 5, Color.white);
        obstacles[34] = new Obstacle(79, 4, Color.white);
        obstacles[35] = new Obstacle(79, 3, Color.white);
        obstacles[36] = new Obstacle(79, 2, Color.white);
        obstacles[37] = new Obstacle(79, 1, Color.white);
        obstacles[38] = new Obstacle(79, 0, Color.white);

        // top left obstacles
        obstacles[39] = new Obstacle(0, 0, Color.white);
        obstacles[40] = new Obstacle(0, 1, Color.white);
        obstacles[41] = new Obstacle(0, 2, Color.white);
        obstacles[42] = new Obstacle(0, 3, Color.white);
        obstacles[43] = new Obstacle(0, 4, Color.white);
        obstacles[44] = new Obstacle(0, 5, Color.white);
        obstacles[45] = new Obstacle(0, 6, Color.white);
        obstacles[46] = new Obstacle(1, 0, Color.white);
        obstacles[47] = new Obstacle(2, 0, Color.white);
        obstacles[48] = new Obstacle(3, 0, Color.white);
        obstacles[49] = new Obstacle(4, 0, Color.white);
        obstacles[50] = new Obstacle(5, 0, Color.white);
        obstacles[51] = new Obstacle(6, 0, Color.white);

        // cell occupied code
        for (Obstacle o : obstacles) {
            cellOccupied[o.cellX][o.cellY] = true;
        }

    }

    private void drawObstacles(Graphics g) {
        for (Obstacle o : obstacles) {
            o.draw(g);
        }
    }

    private Direction getRandomDirection() {
        int rand = randInt(1,4);
        if (rand == 1) {
            return Direction.DOWN;
        } else if (rand == 2) {
            return Direction.LEFT;
        } else if (rand == 3) {
            return Direction.RIGHT;
        } else { //rand == 4
            return Direction.UP;
        }
    }

    private Color getRandomFruitColor() {
        int rand = randInt(1,5);
        if (rand == 1) {
            return Color.magenta;
        } else if (rand == 2) {
            return Color.red;
        } else if (rand == 3) {
            return Color.yellow;
        } else if (rand == 4) {
            return Color.blue;
        } else { //rand == 5
            return Color.orange;
        }
    }

    private void showSplash() {
        state = State.SPLASH;
    }

    private void restart() {
        player.removeFromBoard();
        init();
    }

    private void quit() {
        System.exit(0);
    }

    private void togglePause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED)  {
            state = State.RUNNING;
        }
    }

    private void gameOver() {
        state = State.GAMEOVER;
        player.gameOver();
    }

    private void generateNewApple() {
        int appleCellX = randInt(0, CELL_WIDTH - 1);
        int appleCellY = randInt(0, CELL_HEIGHT - 1);

        // keep generating coordinates until a free spot is found
        while (cellOccupied[appleCellX][appleCellY]) {
            appleCellX = randInt(0, CELL_WIDTH - 1);
            appleCellY = randInt(0, CELL_HEIGHT - 1);
        }

        apple = new Apple(appleCellX, appleCellY, getRandomFruitColor());

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // splash screen
        if (state == State.SPLASH) {
            int fontSize = 50;

            g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
            g.setColor(Color.green);
            g.drawString("Snake", 330, 200);

            g.setFont(new Font("Helvetica", Font.PLAIN, 10));
            g.setColor(Color.white);
            g.drawString("By Andrew Frolkin", 360, 220);

            if (flash) {
                g.setFont(new Font("Helvetica", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Press 1 to start Classic Mode", 300, 300);

                g.setFont(new Font("Helvetica", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Press 2 to start Advanced Mode", 300, 340);
            }

            g.setFont(new Font("Helvetica", Font.PLAIN, 20));
            g.setColor(Color.white);
            g.drawString("Keys:", 300, 400);
            g.setFont(new Font("Helvetica", Font.PLAIN, 20));
            g.setColor(Color.white);
            g.drawString("P - Pause", 300, 420);
            g.setFont(new Font("Helvetica", Font.PLAIN, 20));
            g.setColor(Color.white);
            g.drawString("R - Restart", 300, 440);
            g.setFont(new Font("Helvetica", Font.PLAIN, 20));
            g.setColor(Color.white);
            g.drawString("Q - Quit", 300, 460);
        }

        // game running
        if (state != State.SPLASH) {
            // draw fps
            long beforeTime = System.nanoTime();
            // DRAW FPS:
            now = System.currentTimeMillis();
            framesCount++;
            if (now - framesTimer > 1000) {
                framesTimer = now;
                framesCountAvg = framesCount;
                framesCount = 0;
            }

            // draw playerCell
            player.draw(g);

            // draw the apple
            apple.draw(g);

            if (advancedMode) {
                drawObstacles(g);
            }

            int fontSize = 20;

            // fps
            g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
            g.setColor(Color.white);
            g.drawString("FPS " + Integer.toString(framesCountAvg), 30, 30);

            // speed
            g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
            g.setColor(Color.white);
            g.drawString("Speed " + Integer.toString(userSpeed), 120, 30);

            // score
            g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
            g.setColor(Color.white);
            g.drawString("Score " + Integer.toString(score), 220, 30);

            // mode
            String mode = advancedMode ? "Advanced" : "Classic";
            g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
            g.setColor(Color.white);
            g.drawString(mode, 310, 30);

            if (state == State.GAMEOVER) {
                g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
                g.setColor(Color.white);
                g.drawString("GAME OVER", 340, 300);

                if (flash) {
                    g.setFont(new Font("Helvetica", Font.PLAIN, 15));
                    g.setColor(Color.white);
                    g.drawString("Press R to play again", 330, 330);
                }
            }

            if (state == State.PAUSED) {
                g.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
                g.setColor(Color.white);
                g.drawString("PAUSED", 340, 300);
            }
        }
    }

    // NEED TO UPDATE LOGIC CONSTANTLY, and repaint variably
    // main loop which gets called every time timer is called
    @Override
    public void actionPerformed(ActionEvent e) {
        // render loop
        if (e.getSource() == loop) {
            repaint();
        }
        // update loop
        else if (e.getSource() == loop2) {
            if (state == State.RUNNING) {
                // check for apple and playerCell collision
                checkAppleCollision();
                checkWallCollision();
                checkPlayerCollision();
                if (advancedMode) {
                    checkObstacleCollision();
                }
                if (state == State.RUNNING) {
                    player.move();
                }
            }
        } else if (e.getSource() == loop3) {
            flash = !flash;
        }
    }

    // APPLE COLLISION IN ADVANCED MODE
    private void checkAppleCollision() {
        if (player.getDirection() == Direction.DOWN) {
            if ((apple.getCellX() == player.getCellX()) && (apple.getCellY() == player.getCellY() + 1)) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }

            if (advancedMode && (apple.getCellX() == player.getCellX()) && (apple.getCellY() == 0) && (player.getCellY() == 59)) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }

        } else if (player.getDirection() == Direction.UP) {
            if ((apple.getCellX() == player.getCellX()) && (apple.getCellY() == player.getCellY() - 1)) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }

            if (advancedMode && (apple.getCellX() == player.getCellX()) && (apple.getCellY() == 59) && (player.getCellY() == 0)) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }
        } else if (player.getDirection() == Direction.RIGHT) {
            if ((apple.getCellX() == player.getCellX() + 1) && (apple.getCellY() == player.getCellY())) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }

            if (advancedMode && (apple.getCellX() == 0) && (player.getCellX() == 79) && (apple.getCellY() == player.getCellY())) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }
        } else if (player.getDirection() == Direction.LEFT) {
            if ((apple.getCellX() == player.getCellX() - 1) && (apple.getCellY() == player.getCellY())) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }

            if (advancedMode && (apple.getCellX() == 79) && (player.getCellX() == 0) && (apple.getCellY() == player.getCellY())) {
                player.eatApple(apple);
                apple = null;
                generateNewApple();
            }
        }
    }

    private void checkObstacleCollision() {
        for (Obstacle o : obstacles) {
            if (player.getDirection() == Direction.DOWN) {
                if ((o.getCellX() == player.getCellX()) && (o.getCellY() == player.getCellY() + 1)) {
                    gameOver();
                }
            } else if (player.getDirection() == Direction.UP) {
                if ((o.getCellX() == player.getCellX()) && (o.getCellY() == player.getCellY() - 1)) {
                    gameOver();
                }
            } else if (player.getDirection() == Direction.RIGHT) {
                if ((o.getCellX() == player.getCellX() + 1) && (o.getCellY() == player.getCellY())) {
                    gameOver();
                }
            } else if (player.getDirection() == Direction.LEFT) {
                if ((o.getCellX() == player.getCellX() - 1) && (o.getCellY() == player.getCellY())) {
                    gameOver();
                }
            }
        }
    }

    private void checkWallCollision() {
        if (player.getDirection() == Direction.DOWN) {
            if (CELL_HEIGHT <= player.getCellY() + 1) {
                if (advancedMode){
                    player.setCellY(0);
                } else {
                    gameOver();
                }
            }
        } else if (player.getDirection() == Direction.UP) {
            if (-1 >= player.getCellY() - 1) {
                if (advancedMode){
                    player.setCellY(59);
                } else {
                    gameOver();
                }            }
        } else if (player.getDirection() == Direction.RIGHT) {
            if (CELL_WIDTH <= player.getCellX() + 1) {
                if (advancedMode){
                    player.setCellX(0);
                } else {
                    gameOver();
                }            }
        } else if (player.getDirection() == Direction.LEFT) {
            if (-1 >= player.getCellX() - 1) {
                if (advancedMode){
                    player.setCellX(79);
                } else {
                    gameOver();
                }
            }
        }
    }

    private void checkPlayerCollision() {
        player.checkSelfCollision();
        if (player.gameOver) {
            gameOver();
        }
    }

    private class KAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            String keyString = "";
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_Q) {
                quit();
            }

            if (state != State.SPLASH) {

                if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && (player.isSingleCell() || (!player.isSingleCell() && player.getDirection() != Direction.RIGHT)) && state == State.RUNNING) {
                    player.setDir(Direction.LEFT);
                    keyString = "LEFT";
                }

                if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && (player.isSingleCell() || (!player.isSingleCell() && player.getDirection() != Direction.LEFT)) && state == State.RUNNING) {
                    player.setDir(Direction.RIGHT);
                    keyString = "RIGHT";
                }

                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && (player.isSingleCell() || (!player.isSingleCell() && player.getDirection() != Direction.DOWN)) && state == State.RUNNING) {
                    player.setDir(Direction.UP);
                    keyString = "UP";
                }

                if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && (player.isSingleCell() || (!player.isSingleCell() && player.getDirection() != Direction.UP)) && state == State.RUNNING) {
                    player.setDir(Direction.DOWN);
                    keyString = "DOWN";
                }

                // restart button for testing
                if (key == KeyEvent.VK_R) {
                    restart();
                }

                // pause button
                if (key == KeyEvent.VK_P) {
                    togglePause();
                }
            } else {
                // start game from splash screen
                if ((key == KeyEvent.VK_1)) {
                    advancedMode = false;
                    init();
                }

                if ((key == KeyEvent.VK_2)) {
                    advancedMode = true;
                    init();
                }
            }

            System.out.println(keyString + " pressed.");
        }
    }

    // TODO: move this to a utils class
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    // TODO: move these classes into seperate files maybe?
    // represents a cell on the board
    abstract class Cell {
        // coordinates of block
        int cellX;
        int cellY;


        // initializer - places cell in specified position on specified board
        public Cell(int cellX, int cellY) {
            this.cellX = cellX;
            this.cellY = cellY;
        }

        public int getCellX() {
            return cellX;
        }

        public int getCellY() {
            return cellY;
        }
    }

    // represents the player (some number of playerCells)
    private class Player {
        Stack<PlayerCell> cells;
        Direction dir;
        Boolean gameOver;

        public Player(int cellX, int cellY, Direction dir) {
            cells = new Stack<PlayerCell>();
            cells.push(new PlayerCell(cellX, cellY));
            this.dir = dir;
            this.gameOver = false;
            cellOccupied[cellX][cellY] = true;
        }

        public boolean isSingleCell() {
            return cells.size() == 1;
        }

        public int getCellX() {
            return cells.peek().cellX;
        }

        public int getCellY() {
            return cells.peek().cellY;
        }

        public void setCellY(int y) {
            cells.peek().cellY = y;
        }

        public void setCellX(int x) {
            cells.peek().cellX = x;
        }

        // eat apple and append it to front of player
        public void eatApple(Apple apple) {
            score++;
            cells.push(new PlayerCell(apple.cellX, apple.cellY));
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }

        public Direction getDirection() {
            return dir;
        }

        public void gameOver() {
            this.gameOver = true;
        }

        private void moveRight() {
                PlayerCell topCell = cells.peek();
                PlayerCell cellToAdd = cells.firstElement();

                cells.remove(cells.indexOf(cellToAdd));
                cellToAdd.setCellX(topCell.cellX + 1);
                cellToAdd.setCellY(topCell.cellY);
                cells.push(cellToAdd);
                System.out.println(Integer.toString(cells.size()));
        }

        private void moveLeft() {
                PlayerCell topCell = cells.peek();
                PlayerCell cellToAdd = cells.firstElement();

                cells.remove(cells.indexOf(cellToAdd));
                cellToAdd.setCellX(topCell.cellX - 1);
                cellToAdd.setCellY(topCell.cellY);
                cells.push(cellToAdd);
        }

        private void moveUp() {
                PlayerCell topCell = cells.peek();
                PlayerCell cellToAdd = cells.firstElement();

                cells.remove(cells.indexOf(cellToAdd));
                cellToAdd.setCellX(topCell.cellX);
                cellToAdd.setCellY(topCell.cellY - 1);
                cells.push(cellToAdd);
        }

        private void moveDown() {
                PlayerCell topCell = cells.peek();
                PlayerCell cellToAdd = cells.firstElement();

                cells.remove(cells.indexOf(cellToAdd));
                cellToAdd.setCellX(topCell.cellX);
                cellToAdd.setCellY(topCell.cellY + 1);
                cells.push(cellToAdd);
        }

        public void move() {
            PlayerCell lastCell = cells.firstElement();
            cellOccupied[lastCell.cellX][lastCell.cellY] = false;

            switch(this.dir) {
                case DOWN:
                    moveDown();
                    break;
                case UP:
                    moveUp();
                    break;
                case LEFT:
                    moveLeft();
                    break;
                case RIGHT:
                    moveRight();
                    break;
            }

            PlayerCell topCell = cells.peek();
            cellOccupied[topCell.cellX][topCell.cellY] = true;
        }

        public void removeFromBoard() {
            for (PlayerCell c : cells) {
                cellOccupied[c.cellX][c.cellY] = false;
            }
        }

        public void checkSelfCollision() {
            for (PlayerCell c : cells) {
                if (dir == Direction.DOWN) {
                    if (c.cellY == player.getCellY() + 1 && c.cellX == player.getCellX()) {
                        gameOver();
                    }
                } else if (dir == Direction.UP) {
                    if (c.cellY == player.getCellY() - 1 && c.cellX == player.getCellX()) {
                        gameOver();
                    }
                } else if (dir == Direction.RIGHT) {
                    if (c.cellX == player.getCellX() + 1 && c.cellY == player.getCellY()) {
                        gameOver();
                    }
                } else if (dir == Direction.LEFT) {
                    if (c.cellX == player.getCellX() - 1 && c.cellY == player.getCellY()) {
                        gameOver();
                    }
                }
            }
        }

        public void draw(Graphics g) {
            for (PlayerCell c : cells) {
                if(gameOver && cells.peek() == c) {
                    c.drawWithColor(g, Color.red);
                } else {
                    c.draw(g);
                }
            }
        }
    }

    // represents the playerCell on the board
    private class PlayerCell extends Cell {

        public PlayerCell(int cellX, int cellY) {
            super(cellX, cellY);
        }

        public void setCellX(int x) {
            this.cellX = x;
        }

        public void setCellY(int y) {
            this.cellY = y;
        }

        public void draw(Graphics g) {
            g.setColor(Color.green);
            g.fillRect(convertCellXToPixelX(cellX), convertCellYToPixelY(cellY), CELL_SIZE, CELL_SIZE);
            System.out.println("PlayerCell Drawn.");
        }

        public void drawWithColor(Graphics g, Color c) {
            g.setColor(c);
            g.fillRect(convertCellXToPixelX(cellX), convertCellYToPixelY(cellY), CELL_SIZE, CELL_SIZE);
            System.out.println("PlayerCell Drawn.");
        }
    }

    private class Apple extends Cell {
        private Color color;

        public Apple(int cellX, int cellY, Color c) {
            super(cellX, cellY);
            this.color = c;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRoundRect(convertCellXToPixelX(cellX), convertCellYToPixelY(cellY), CELL_SIZE, CELL_SIZE, 10, 10);
            System.out.println("Apple Drawn.");
        }
    }

    private class Obstacle extends Cell {
        private Color color;

        public Obstacle(int cellX, int cellY, Color c) {
            super(cellX, cellY);
            this.color = c;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(convertCellXToPixelX(cellX), convertCellYToPixelY(cellY), CELL_SIZE, CELL_SIZE);
        }
    }

}
