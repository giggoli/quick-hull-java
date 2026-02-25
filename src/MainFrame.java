import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("QuickHull Visualization");
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        HullPanel hullPanel = new HullPanel();
        ControlPanel controlPanel = new ControlPanel(hullPanel);

        add(controlPanel, BorderLayout.WEST);
        add(hullPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}