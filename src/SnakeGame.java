import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;

public class SnakeGame extends JFrame {
    private JPanel cardPanel;
    private JPanel homepagePanel;
    private Board board;

    SnakeGame() {
        // Set up the main frame
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Create the homepage panel with the play button
        homepagePanel = createHomepagePanel();

        // Create the game board
        board = new Board();
        board.setVisible(false); // Hide the game board initially

        // Create a card layout panel to switch between homepage and game board
        cardPanel = new JPanel(new CardLayout());
        cardPanel.add(homepagePanel, "HomepagePanel");
        cardPanel.add(board, "GamePanel");

        setContentPane(cardPanel);

        // Set the logo image for the MainFrame
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png")));
        setIconImage(logo.getImage());

        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Show a confirmation dialog when closing the window
                int confirm = JOptionPane.showConfirmDialog(SnakeGame.this, "Confirm if you want to exit the game", "Snake Game", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    private JPanel createHomepagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Create a panel to hold the play button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Create a play button label
        JLabel playButton = new JLabel();
        ImageIcon playButtonImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/playbutton.png")));
        playButton.setIcon(playButtonImage);
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                playGame();
            }

            private void playGame() {
                // Switch to the game board and start the game
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "GamePanel");
                board.startGame();

                try {
                    // Load the audio file
                    InputStream audioStream = getClass().getResourceAsStream("/Snake.wav");
                    assert audioStream != null;
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttonPanel.add(playButton);

        // Create a background label
        JLabel backgroundLabel = new JLabel();
        ImageIcon backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/homepage.png")));
        backgroundLabel.setIcon(backgroundImage);

        // Add the button panel and background label to the homepage panel
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(backgroundLabel, BorderLayout.CENTER);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeGame game = new SnakeGame();
            game.setVisible(true);
        });
    }
}

class Board extends JPanel implements ActionListener {
    // Start the game
    public void startGame() {
        inGame = true;
        initGame();
        loadImages();
        Score = 0;
        DOTS = 3;
        DELAY = 250;
        timer.start();
    }
    // Game board dimensions
    int height = 400;
    int width = 400;

    // Maximum number of dots (body segments) in the game
    int MAX_DOTS = 1600;

    // Size of each dot (body segment) and initial number of dots
    int DOT_SIZE = 10;
    int DOTS = 3;

    // Arrays to store the x and y coordinates of each dot (body segment)
    int[] x = new int[MAX_DOTS];
    int[] y = new int[MAX_DOTS];

    // Coordinates of the apple
    int apple_x;
    int apple_y;

    Random random;

    // Images for the snake body, head, and apple
    Image body, head, apple;

    Timer timer;
    int DELAY = 250;

    // Direction flags for snake movement
    boolean leftDirection = true;
    boolean rightDirection = false;
    boolean upDirection = false;
    boolean downDirection = false;

    // Game state flag
    boolean inGame = true;


    Board() {
        random = new Random();
        setFocusable(true);
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        initGame();
        loadImages();

        // Bind keys for controlling the snake
        bindKeys();

    }

    // Initialize the game logic
    public void initGame() {
        DOTS = 3;

        // Initialize snake's position
        x[0] = 300;
        y[0] = 50;

        // Set initial positions for the rest of the snake's body segments
        for (int i = 0; i < DOTS; i++) {
            x[i] = x[0] + DOT_SIZE * i;
            y[i] = y[0];
        }

        // Initialize apple's position
        locateApple();

        // Start the game timer
        timer = new Timer(DELAY, this);
        timer.start();

        // Request focus for the Board panel to receive keyboard events
        requestFocusInWindow();
    }

    // Load images from resources folder into Image objects
    public void loadImages() {
        // Use ClassLoader to load resources from JAR file
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            InputStream bodyIconStream = classLoader.getResourceAsStream("dot.png");
            assert bodyIconStream != null;
            body = ImageIO.read(bodyIconStream);
            InputStream headIconStream = classLoader.getResourceAsStream("head.png");
            assert headIconStream != null;
            head = ImageIO.read(headIconStream);
            InputStream appleIconStream = classLoader.getResourceAsStream("apple.png");
            assert appleIconStream != null;
            apple = ImageIO.read(appleIconStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Paint the snake and apple on the game board
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the snake and apple on the game board
        if (inGame) {
            // Draw the apple
            g.drawImage(apple, apple_x, apple_y, this);

            // Draw the snake's body
            for (int i = 0; i < DOTS; i++) {
                if (i == 0) {
                    // Draw the snake's head
                    g.drawImage(head, x[0], y[0], this);
                } else {
                    // Draw the snake's body segments
                    g.drawImage(body, x[i], y[i], this);
                }
            }
        } else {
            // If the game is over, display the Game Over message
            GameOver(g);
            timer.stop();
        }
    }

    // Draw the snake and apple on the game board

    // Randomize the apple's position on the game board
    public void locateApple() {
        apple_x = ((int) (Math.random() * 39)) * DOT_SIZE;
        apple_y = ((int) (Math.random() * 39)) * DOT_SIZE;
    }

    // Check for collisions with the snake's body and the game board borders
    public void checkCollision() {
        // Check for collisions with the snake's body
        for (int i = 1; i < DOTS; i++) {
            if (i > 4 && x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
                break;
            }
        }

        // Check for collisions with the game board borders
        if (x[0] < 0) {
            inGame = false;
        }
        if (x[0] >= width) {
            inGame = false;
        }
        if (y[0] < 0) {
            inGame = false;
        }
        if (y[0] >= height) {
            inGame = false;
        }
    }


    int Score = 0;

    // Display the Game Over message
    public void GameOver(Graphics g) {
        String msg = "GameOver";
        Score = (DOTS - 3);
        String scoreMessage = " Score : " + Score;
        String restart = "Press SPACE to Restart";
        Font small = new Font("Helvetica", Font.BOLD, 25);
        FontMetrics fontmetrics = getFontMetrics(small);

        g.setColor(Color.WHITE);
        g.setFont(small);

        // Draw the Game Over message, score, and restart instructions on the screen
        g.drawString(msg, (width - fontmetrics.stringWidth(msg)) / 2, height / 4);
        g.drawString(scoreMessage, (width - fontmetrics.stringWidth(scoreMessage)) / 2, 2 * (height / 4));
        g.drawString(restart, (width - fontmetrics.stringWidth(restart)) / 2, 3 * (height / 4));
    }


    // Restart the game
    public void restart() {
        inGame = true;
        initGame();
        loadImages();
        Score = 0;
        DOTS = 3;
        DELAY = 250;
        timer.start();
    }

    // Handle the game timer events
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    // Move the snake on the game board
    public void move() {
        // Move the snake's body segments
        for (int i = DOTS - 1; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the snake's head based on the direction flags
        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }
        if (rightDirection) {
            x[0] += DOT_SIZE;
        }
        if (upDirection) {
            y[0] -= DOT_SIZE;
        }
        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    // Check if the snake has eaten the apple
    public void checkApple() {
        if (apple_x == x[0] && apple_y == y[0]) {
            // Increase the length of the snake and relocate the apple
            DOTS++;
            locateApple();
        }
    }

    // Bind keys for controlling the snake
    private void bindKeys() {
        // Key binding for left arrow key
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!rightDirection) {
                    leftDirection = true;
                    upDirection = false;
                    downDirection = false;
                }
            }
        });

        // Key binding for right arrow key
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!leftDirection) {
                    rightDirection = true;
                    upDirection = false;
                    downDirection = false;
                }
            }
        });

        // Key binding for up arrow key
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!downDirection) {
                    upDirection = true;
                    rightDirection = false;
                    leftDirection = false;
                }
            }
        });

        // Key binding for down arrow key
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!upDirection) {
                    downDirection = true;
                    rightDirection = false;
                    leftDirection = false;
                }
            }
        });

        // Key binding for spacebar
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space");
        getActionMap().put("space", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restart();
            }
        });
    }
}

