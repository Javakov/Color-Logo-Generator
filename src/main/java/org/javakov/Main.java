package org.javakov;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Logo Generator");

        JTextField inputField = new JTextField("javakov", 15);
        ColorMatrixImage panel = new ColorMatrixImage(inputField.getText());

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(e -> {
            panel.setInputText(inputField.getText());
            panel.repaint();
        });

        JButton saveButton = new JButton("Download Image");
        saveButton.addActionListener(e -> panel.saveImage());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.add(inputField);
        inputPanel.add(generateButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);

        controlPanel.add(inputPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 0)));
        controlPanel.add(buttonPanel);

        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.pack();

        Insets insets = frame.getInsets();
        int width = ColorMatrixImage.SIZE * ColorMatrixImage.CELL_SIZE + insets.left + insets.right;
        int height = ColorMatrixImage.SIZE * ColorMatrixImage.CELL_SIZE + insets.top + insets.bottom + controlPanel.getPreferredSize().height;
        frame.setSize(width, height);

        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
