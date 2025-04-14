import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SketchpadUI extends JFrame {
    private DrawingCanvas canvas;

    public SketchpadUI() {
        super("Sketchpad Emulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // top panel includes the buttons for geometric objects 
        JPanel buttonPanelTop = new JPanel(new FlowLayout());
        JButton pointButton = new JButton("point");
        JButton lineButton = new JButton("line");
        JButton arcButton = new JButton("arc");
        JButton deleteButton = new JButton("delete");
        JButton moveButton = new JButton("move"); 
        // disable the buttons initially 
        pointButton.setEnabled(false); 
        lineButton.setEnabled(false); 
        arcButton.setEnabled(false); 
        deleteButton.setEnabled(false); 
        moveButton.setEnabled(false); 
        buttonPanelTop.add(pointButton);
        buttonPanelTop.add(lineButton);
        buttonPanelTop.add(arcButton);
        buttonPanelTop.add(deleteButton); 
        buttonPanelTop.add(moveButton); 
        
        // Bottom panel for constraint buttons
        JPanel buttonPanelBottom = new JPanel(new FlowLayout());
        JButton horizontalButton = new JButton("horizontal");
        JButton verticalButton = new JButton("vertical");
        JButton equalLengthButton = new JButton("equal length");
        JButton parallelButton = new JButton("parallel");
        JButton groupButton = new JButton("group");
        JButton perpendicularButton = new JButton("perpendicular");
        JButton tangentButton = new JButton("tangent");
        horizontalButton.setEnabled(false); 
        verticalButton.setEnabled(false); 
        equalLengthButton.setEnabled(false); 
        parallelButton.setEnabled(false); 
        groupButton.setEnabled(false); 
        perpendicularButton.setEnabled(false); 
        tangentButton.setEnabled(false); 
        buttonPanelBottom.add(horizontalButton);
        buttonPanelBottom.add(verticalButton);
        buttonPanelBottom.add(equalLengthButton);
        buttonPanelBottom.add(parallelButton);
        buttonPanelBottom.add(groupButton);
        buttonPanelBottom.add(perpendicularButton);
        buttonPanelBottom.add(tangentButton);
        
        // Create and configure the drawing canvas
        canvas = new DrawingCanvas();
        canvas.setBackground(Color.WHITE);
        
        // Assemble the UI
        add(buttonPanelTop, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(buttonPanelBottom, BorderLayout.SOUTH);
        
        setSize(1000, 800);
        setLocationRelativeTo(null);
        // setVisible(true);
        
        // Button action listeners
        pointButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.POINT));
        lineButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.LINE));
        arcButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.ARC));
        deleteButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.DELETE));
        moveButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.MOVE));
        
        // Constraint buttons - placeholders for now
        parallelButton.addActionListener(e -> System.out.println("Parallel constraint selected."));
        equalLengthButton.addActionListener(e -> {
            if (canvas.getMode() == DrawingCanvas.Mode.EQUAL_LENGTH) {
                // already in the mode just need to finalize 
                canvas.finalizeEqualLengthConstraint(); 
                canvas.setMode(DrawingCanvas.Mode.NONE); 
            } else {
                canvas.setMode(DrawingCanvas.Mode.EQUAL_LENGTH); 
                System.out.println("Equal Length constraint selected."); 
            }
            
        });
        groupButton.addActionListener(e -> {
            System.out.println("Group constraint selected."); 
            canvas.setMode(DrawingCanvas.Mode.GROUP_SELECT);
        });
        perpendicularButton.addActionListener(e -> System.out.println("Perpendicular constraint selected."));
        tangentButton.addActionListener(e -> System.out.println("Tangent constraint selected."));
        horizontalButton.addActionListener(e -> System.out.println("Horizontal constraint selected."));
        verticalButton.addActionListener(e -> System.out.println("Vertical constraint selected.")); 

        // create a glass pane overlay that will display INK 
        JPanel overlay = new JPanel(new GridBagLayout()); 
        overlay.setOpaque(true); 
        // background 
        overlay.setBackground(new Color(255, 255, 255, 230)); 
        JLabel inkLabel = new JLabel("INK"); 
        inkLabel.setFont(new Font("Serif", Font.BOLD, 72)); 
        inkLabel.setForeground(Color.BLACK); 
        overlay.add(inkLabel); 

        // add mouse listener to INK 
        // so that after user interacts, the overlay is removed 
        // and the canvas and buttons are useable 
        inkLabel.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) {
                overlay.setVisible(false); 
                // enable all buttons 
                pointButton.setEnabled(true); 
                lineButton.setEnabled(true);
                arcButton.setEnabled(true); 
                moveButton.setEnabled(true); 
                deleteButton.setEnabled(true); 
                equalLengthButton.setEnabled(true); 
                horizontalButton.setEnabled(true); 
                verticalButton.setEnabled(true); 
                parallelButton.setEnabled(true); 
                perpendicularButton.setEnabled(true); 
                groupButton.setEnabled(true); 
                tangentButton.setEnabled(true);
            }
        }); 

        setGlassPane(overlay); 
        overlay.setVisible(true); 

        setVisible(true); 

    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SketchpadUI::new);
    }
}
