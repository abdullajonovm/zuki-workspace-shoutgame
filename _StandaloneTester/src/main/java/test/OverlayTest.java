package test;

import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OverlayTest
{

    protected static int posX;
    protected static int posY;

    public static void main(String[] args) throws IOException
    {
        JFrame frame = new JFrame("JFrame Example");

        JPanel panel = new JPanel();
        panel.setLayout(null);

        //close button
        Image closeImg = ImageIO.read(ClassLoader.getSystemResource("close3.png"));

        JButton closeBtn = new JButton(new ImageIcon(closeImg));
        closeBtn.setBounds(300-32, 0, 32, 32);
        closeBtn.setMargin(new Insets(0,0,0,0));
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //https://stackoverflow.com/questions/1234912/how-to-programmatically-close-a-jframe
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        panel.add(closeBtn);

        panel.setBackground(Color.BLACK);

        //https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });
        frame.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                //sets frame position when mouse dragged
                Rectangle rectangle = frame.getBounds();
                frame.setBounds(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY, rectangle.width, rectangle.height);
            }
        });


        //frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);

        frame.add(panel);
        frame.setSize(300,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

}
