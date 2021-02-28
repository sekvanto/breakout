/*
 * File: BreakoutExt.java
 * ----------------------------
 * This program extends the standard one.
 * Several features were added:
 * - Ball collide, game win, game lost, attempt lost sound
 * - Add label which counts attempts remaining
 * - Update notifications style
 */

package com.shpp.p2p.cs.sekvanto.game;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class BreakoutExt extends WindowProgram {
    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of game board (usually the same) */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 8;

    /** Diameter of the ball in pixels */
    private static final int BALL_DIAMETER = BALL_RADIUS * 2;

    /** Ball vertical speed on the start */
    private static final double STARTING_VY = 3.0;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Pause between updates of ball location in milliseconds */
    private static final int PAUSE_TIME = 10;

    /** Number of turns */
    private static final int NTURNS = 3;

    /** Moves the paddle when mouse moves */
    public void mouseMoved(MouseEvent mouseEvent) {
        /* Leaves paddle on the screen if mouse is beyond the borders */
        if (mouseEvent.getX() > getWidth() - PADDLE_WIDTH) {
            return;
        }
        paddle.setLocation(
                mouseEvent.getX(),
                getHeight() - PADDLE_Y_OFFSET
        );
    }

    public void run() {
        addMouseListeners();
        addBall();
        addPaddle();
        addBrickWall();
        addAttemptsLabel();
        play();
    }

    /**
     * Main method for playing.
     * Each iteration of the internal loop = one attempt.
     * You lose after it ends. And you can win during
     * some of the iterations
     */
    private void play() {
        /* Wait for click at the start of the game */
        waitForClick();
        /* Main cycle, each iteration = one attempt */
        for (int i = 0; i < NTURNS; i++) {
            /* Internal cycle, each iteration updates ball */
            while (!isAttemptLost) {
                updateBallLocation();
                /* Breaks internal loop (an attempt) if you won */
                if (isGameWon) break;
                pause(PAUSE_TIME);
            }
            /* Breaks external loop (main game) too if you won */
            if (isGameWon) break;
            /* Updating the label */
            updateAttemptsLabel();
            /* This attempt was unsuccessful, but not the last one */
            if (!(i == NTURNS - 1)) {
                attemptLost(NTURNS - i - 1);
                isAttemptLost = false;
            }
        }
        if (isGameWon) win();
        else lost();
    }

    /**
     * This function waits for click after each
     * unsuccessful attempt. It removes the center
     * label after click ang game continues
     */
    private void attemptLost(int attemptsLeft) {
        /* Adds new ball */
        addBall();
        addNotification("You have " + attemptsLeft + " more attempt(s)");
        /* Play sound */
        double[] attemptLostSound = StdAudio.read("assets/attempt_lost.wav");
        StdAudio.play(attemptLostSound);
        waitForClick();
        remove(notification);
    }

    /** Puts a label when the game is lost */
    private void lost() {
        addNotification("Game lost");
        /* Play sound */
        double[] gameOverSound = StdAudio.read("assets/game_over.wav");
        StdAudio.play(gameOverSound);
    }

    /** Puts a label when you win */
    private void win() {
        addNotification("You won");
        /* Play sound */
        double[] winSound = StdAudio.read("assets/win.wav");
        StdAudio.play(winSound);
    }

    /** Paddle instance */
    private GRect paddle = null;

    /** Ball instance */
    private GOval ball = null;

    /** Attempts counter label on screen */
    private GLabel attemptsLabel = null;

    /** Current notification label on the screen (e.g. "You won") */
    private GLabel notification = null;

    /** Speed components of the ball */
    private double vx, vy;

    /** The current number of bricks on the field */
    private int currentBrickNumber = NBRICKS_PER_ROW * NBRICK_ROWS;

    /** How many attempts left */
    private int attemptsLeft = NTURNS;

    /** Is set to true when you lose an attempt */
    private boolean isAttemptLost = false;

    /** Is set to true when you win */
    private boolean isGameWon = false;

    /** Adds initial paddle to screen */
    private void addPaddle() {
        int x = (getWidth() - PADDLE_WIDTH) / 2;
        int y = getHeight() - PADDLE_Y_OFFSET;
        paddle = new GRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        paddle.setColor(Color.BLACK);
        add(paddle);
    }

    /** Adds initial ball to screen */
    private void addBall() {
        /* Put ball on the racket */
        int x = (getWidth() - BALL_DIAMETER) / 2;
        int y = getHeight() - PADDLE_Y_OFFSET - BALL_DIAMETER;
        ball = new GOval(x, y, BALL_DIAMETER, BALL_DIAMETER);
        ball.setFilled(true);
        ball.setColor(Color.BLACK);
        add(ball);

        /* Set initial ball speed */
        vy = STARTING_VY;
        RandomGenerator rgen = RandomGenerator.getInstance();
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
    }

    /** Adds initial label which tracks attempts number */
    private void addAttemptsLabel() {
        attemptsLabel = new GLabel("Attempts remaining: " + NTURNS);
        attemptsLabel.setFont("Comic Sans-15");
        attemptsLabel.setColor(Color.MAGENTA);
        attemptsLabel.setLocation(
                attemptsLabel.getWidth(),
                attemptsLabel.getHeight()
        );
        add(attemptsLabel);
    }

    /** Updates the label and the variable which tracks attempts number */
    private void updateAttemptsLabel() {
        attemptsLeft--;
        attemptsLabel.setLabel("Attempts remaining: " + attemptsLeft);
    }

    /** Adds the wall of bricks to the screen */
    private void addBrickWall() {
        /* Array of row colors */
        ArrayList<Color> colors = new ArrayList<>();
        /* Filling the array */
        colors.add(Color.RED);
        colors.add(Color.ORANGE);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        /* Building rows one by one in a loop */
        for (int i = 0; i < NBRICK_ROWS; i++) {
            /* Finding color of current row */
            Color currentColor = colors.get((i / 2) % colors.size());
            buildBrickRaw(i, currentColor);
        }
    }

    /**
     * builds a row of bricks
     * @param rowNumber The number of the row
     * @param currentColor The color of the row
     */
    private void buildBrickRaw(int rowNumber, Color currentColor) {
        /* Building bricks one by one in a loop */
        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            /* Finding coordinates */
            int x = i * (BRICK_WIDTH + BRICK_SEP);
            int y = BRICK_Y_OFFSET + (rowNumber * (BRICK_HEIGHT + BRICK_SEP));
            GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
            brick.setFilled(true);
            brick.setColor(currentColor);
            add(brick);
        }
    }

    /** Updates ball location */
    private void updateBallLocation() {
        testBallTouchWalls();
        testBallTouchObjects();
        /* Check if ball fell under the floor */
        if (ball.getY() >= getHeight()) {
            isAttemptLost = true;
        }
        ball.move(vx, vy);
    }

    /** Tests if ball touches walls and updates speed */
    private void testBallTouchWalls() {
        /* Checks if ball touches walls */
        if (ball.getX() <= 0 || ball.getX() >= getWidth() - BALL_DIAMETER) {
            vx = -vx;
            audioJump();
        }
        /* Checks if ball touches ceiling */
        else if (ball.getY() < 0) {
            vy = -vy;
            audioJump();
        }
    }

    /**
     * Tests if ball touches objects and updates speed.
     * For doing it, tests its corners
     */
    private void testBallTouchObjects() {
        /* Left-upper corner */
        if (checkPoint(ball.getX(), ball.getY())) return;
        /* Right-upper corner */
        if (checkPoint(ball.getX() + BALL_DIAMETER, ball.getY())) return;
        /* Left-lower corner */
        if (checkPoint(ball.getX(), ball.getY() + BALL_DIAMETER)) return;
        /* Right-lower corner */
        checkPoint(ball.getX() + BALL_DIAMETER, ball.getY() + BALL_DIAMETER);
    }

    /**
     * Tests if a point touches an object.
     * If it doesn't, the function returns false.
     * If it touches paddle or brick, updates speed.
     * If it touches brick, removes it
     * @param x X coordinate of point
     * @param y Y coordinate of point
     * @return Returns true if point touches an object
     */
    private boolean checkPoint(double x, double y) {
        GObject collider = getElementAt(x, y);
        if (collider == null || collider == attemptsLabel) {
            return false;
        }
        if (collider == paddle) {
            processPaddle(x, y);
        }
        /* Otherwise collider is a brick */
        else {
            removeBrick(x, y);
        }
        audioJump();
        return true;
    }

    /**
     * Reverts vertical speed if collider is the top of the paddle.
     * Reverts horizontal speed if collider is right/left side of the paddle
     * Does nothing if the collider is lower side of the paddle
     */
    private void processPaddle(double x, double y) {
        if (y == paddle.getY()) {
            vy = -vy;
        }
        else if (x == paddle.getX() || x == paddle.getX() + PADDLE_WIDTH) {
            vx = -vx;
        }
    }

    /**
     * This function is called when a point touches a brick.
     * It removes the brick, but also checks if the user won
     * and this was the last brick
     * @param x X coordinate of point
     * @param y Y coordinate of point
     */
    private void removeBrick(double x, double y) {
        remove(getElementAt(x, y));
        vy = -vy;
        currentBrickNumber--;
        if (currentBrickNumber == 0) isGameWon = true;
    }

    /**
     * Plays bounce audio when ball touches an object
     */
    private void audioJump() {
        double[] sound = StdAudio.read("assets/jump.wav");
        StdAudio.play(sound);
    }

    /**
     * Adds a red notification of specified parameters to center
     * @param s Notification text
     */
    private void addNotification(String s) {
        notification = new GLabel(s);
        notification.setFont("Comic Sans-32");
        notification.setColor(Color.RED);
        notification.setLocation(
                (getWidth() - notification.getWidth()) / 2,
                (getHeight() - notification.getHeight()) / 2
        );
        add(notification);
    }

}
