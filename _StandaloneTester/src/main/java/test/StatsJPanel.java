package test;

import javax.swing.JButton;
import javax.swing.JPanel;

public class StatsJPanel extends JPanel
{
    public StatsJPanel() {
        setLayout(null);

        JButton btnNewButton = new JButton("New button");
        btnNewButton.setBounds(327, 6, 117, 29);
        add(btnNewButton);
    }
}
