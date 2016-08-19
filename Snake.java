import java.awt.EventQueue;
import javax.swing.JFrame;

public class Snake extends JFrame {
        public Snake(int frameRate, int snakeSpeed) {
            add(new Board(frameRate, snakeSpeed));
            setResizable(false);
            // remove?
            pack();
            setTitle("Snake");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         }
            

        public static void main(String[] args) { 
            final int frameRate;
            final int snakeSpeed;
            
            // get command line args

            int frameRateArg = 30;
            try {
                frameRateArg = Integer.parseInt(args[0]);
                if (frameRateArg > 100 || frameRateArg < 1) {
                    System.err.println("Invalid frame rate argument passed: " + frameRateArg);
                    System.exit(1);
                }
                System.out.println("Frame rate argument passed: " + frameRateArg);
            } catch (NumberFormatException e) {
                System.err.println("Frame rate argument must be an integer.");
                System.exit(1);
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            frameRate = frameRateArg;

            int snakeSpeedArg = 2;
            try {
                snakeSpeedArg = Integer.parseInt(args[1]);
                if (snakeSpeedArg > 10 || snakeSpeedArg < 1) {
                    System.err.println("Invalid Snake speed argument passed: " + snakeSpeedArg);
                    System.exit(1);
                }
                System.out.println("Snake speed argument passed: " + snakeSpeedArg);
            } catch (NumberFormatException e) {
                System.err.println("Snake speed  argument must be an integer.");
                System.exit(1);
            } catch (ArrayIndexOutOfBoundsException e) {

            }

            snakeSpeed = snakeSpeedArg;

            EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {                
                            JFrame ex = new Snake(frameRate, snakeSpeed);

                            ex.setVisible(true);                
                    }
            });
        }
}
