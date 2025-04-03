import javax.swing.*;
import java.awt.*;

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
        buttonPanelTop.add(pointButton);
        buttonPanelTop.add(lineButton);
        buttonPanelTop.add(arcButton);
        buttonPanelTop.add(deleteButton); 
        
        // Bottom panel for constraint buttons
        JPanel buttonPanelBottom = new JPanel(new FlowLayout());
        JButton equalLengthButton = new JButton("equal length");
        JButton parallelButton = new JButton("parallel");
        JButton coincideButton = new JButton("coincide");
        JButton perpendicularButton = new JButton("perpendicular");
        // Optional extra button if needed
        JButton tangentButton = new JButton("tangent");
        buttonPanelBottom.add(equalLengthButton);
        buttonPanelBottom.add(parallelButton);
        buttonPanelBottom.add(coincideButton);
        buttonPanelBottom.add(perpendicularButton);
        buttonPanelBottom.add(tangentButton);
        
        // Create and configure the drawing canvas
        canvas = new DrawingCanvas();
        canvas.setBackground(Color.WHITE);
        
        // Assemble the UI
        add(buttonPanelTop, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(buttonPanelBottom, BorderLayout.SOUTH);
        
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Button action listeners
        pointButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.POINT));
        lineButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.LINE));
        arcButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.ARC));
        deleteButton.addActionListener(e -> canvas.setMode(DrawingCanvas.Mode.DELETE));
        
        // Constraint buttons - placeholders for now
        parallelButton.addActionListener(e -> System.out.println("Parallel constraint selected."));
        equalLengthButton.addActionListener(e -> {
            canvas.setMode(DrawingCanvas.Mode.EQUAL_LENGTH); 
            System.out.println("Equal Length constraint selected."); 
        });
        coincideButton.addActionListener(e -> System.out.println("Coincide constraint selected."));
        perpendicularButton.addActionListener(e -> System.out.println("Perpendicular constraint selected."));
        tangentButton.addActionListener(e -> System.out.println("Tangent constraint selected."));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SketchpadUI::new);
    }
}
