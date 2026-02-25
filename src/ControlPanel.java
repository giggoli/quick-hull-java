import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Hashtable;

public class ControlPanel extends JPanel {

    private final HullPanel hullPanel;
    private final JTextField pointCountField;
    private final JSlider speedSlider;
    private final JLabel areaLabel;

    public ControlPanel(HullPanel hullPanel) {
        this.hullPanel = hullPanel;

        setPreferredSize(new Dimension(230, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        setBackground(new Color(245, 245, 248));

        add(sectionLabel("Input"));
        add(Box.createVerticalStrut(6));

        add(smallLabel("Number of points:"));
        pointCountField = new JTextField("20", 5);
        pointCountField.setMaximumSize(new Dimension(80, 26));
        pointCountField.setAlignmentX(LEFT_ALIGNMENT);
        add(pointCountField);
        add(Box.createVerticalStrut(6));

        JButton generateBtn = button("Generate Points");
        add(generateBtn);

        add(Box.createVerticalStrut(18));


        add(sectionLabel("Animation"));
        add(Box.createVerticalStrut(6));

        add(smallLabel("Speed:"));
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 800);
        speedSlider.setInverted(true);   // left = fast, right = slow
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMaximumSize(new Dimension(200, 45));
        speedSlider.setAlignmentX(LEFT_ALIGNMENT);
        speedSlider.setBackground(new Color(245, 245, 248));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(100, new JLabel("Fast"));
        labelTable.put(2000, new JLabel("Slow"));

        speedSlider.setLabelTable(labelTable);

        add(speedSlider);

        add(Box.createVerticalStrut(8));

        JButton animateBtn = button("Start Animation");
        JButton fastBtn = button("Fast Execution");
        add(animateBtn);
        add(Box.createVerticalStrut(6));
        add(fastBtn);

        add(Box.createVerticalStrut(18));

        add(sectionLabel("Result"));
        add(Box.createVerticalStrut(6));
        areaLabel = new JLabel("Area: —");
        areaLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        areaLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(areaLabel);

        generateBtn.addActionListener(e -> {
            int n = parseCount();
            if (n < 3) {
                JOptionPane.showMessageDialog(this,
                        "Please enter at least 3 points.", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            hullPanel.generatePoints(n);
            areaLabel.setText("Area: —");
        });

        animateBtn.addActionListener(e -> {
            hullPanel.clear();
            areaLabel.setText("Area: —");
            hullPanel.startAnimation(speedSlider.getValue());

            javax.swing.Timer poll = new javax.swing.Timer(200, null);
            poll.addActionListener(ev -> {
                double a = hullPanel.getArea();
                if (a > 0) {
                    areaLabel.setText(String.format("Area: %.4f", a));
                    poll.stop();
                }
            });
            poll.start();
        });

        fastBtn.addActionListener(e -> {
            hullPanel.clear();
            hullPanel.computeFast();
            double a = hullPanel.getArea();
            areaLabel.setText(a > 0 ? String.format("Area: %.4f", a) : "Area: —");
        });
    }

    private int parseCount() {
        try {
            return Integer.parseInt(pointCountField.getText().trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 30));
        return btn;
    }

    private JLabel smallLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(60, 60, 80));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }
}