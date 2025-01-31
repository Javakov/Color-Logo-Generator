package org.javakov;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ColorMatrixImage extends JPanel {
    public static final int SIZE = 8;
    public static final int CELL_SIZE = 100;
    private BufferedImage image;
    private String inputText;
    private Color accentColor;

    public ColorMatrixImage(String inputText) {
        this.inputText = inputText;
        generateImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
        generateImage();
        repaint();
    }

    private void generateImage() {
        image = new BufferedImage(
                SIZE * CELL_SIZE,
                SIZE * CELL_SIZE,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = image.createGraphics();

        byte[] hashBytes = inputText.getBytes(StandardCharsets.UTF_8);
        int baseSeed = hashCodeFromBytes(hashBytes);

        Random mainRand = new Random(baseSeed);
        accentColor = new Color(
                mainRand.nextInt(256),
                mainRand.nextInt(256),
                mainRand.nextInt(256)
        );

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE / 2; col++) {
                int mirroredCol = SIZE - 1 - col;
                int pairSeed = baseSeed + row * SIZE + col;
                Random pairRand = new Random(pairSeed);

                pairRand.nextInt(256);
                pairRand.nextInt(256);
                pairRand.nextInt(256);

                boolean fillSquare = pairRand.nextBoolean();
                int triangleSide = pairRand.nextInt(4);
                int halfSquareSide = pairRand.nextInt(4);
                boolean drawHalfSquare = pairRand.nextBoolean();

                g2d.setColor(fillSquare ? accentColor : Color.WHITE);
                g2d.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g2d.fillRect(mirroredCol * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                drawTriangle(g2d, col * CELL_SIZE, row * CELL_SIZE, triangleSide);
                drawTriangle(g2d, mirroredCol * CELL_SIZE, row * CELL_SIZE,
                        mirrorSideVertically(triangleSide));

                if(drawHalfSquare) {
                    drawHalfSquare(g2d, col * CELL_SIZE, row * CELL_SIZE, halfSquareSide);
                    drawHalfSquare(g2d, mirroredCol * CELL_SIZE, row * CELL_SIZE,
                            mirrorSideVertically(halfSquareSide));
                }
            }
        }

        Random edgeRand = new Random(baseSeed);
        drawEdgeSquares(g2d, edgeRand, 0, SIZE/2);
        drawEdgeSquares(g2d, edgeRand, SIZE/2, SIZE);

        checkAndReplaceDenseAreas(g2d, baseSeed);

        g2d.dispose();
    }

    private void drawEdgeSquares(Graphics2D g2d,  Random baseSeed, int startRow, int endRow) {
        boolean isTopHalf = (startRow == 0);
        int halfSeed = baseSeed.nextInt() + (isTopHalf ? 0 : 1);
        Random halfRand = new Random(halfSeed);

        boolean useNewVariant = halfRand.nextBoolean();

        int[] edgeColumns;
        if (useNewVariant) {
            int mid1 = (SIZE/2) - 1;
            int mid2 = SIZE/2;
            edgeColumns = new int[]{mid1, mid2};
        } else {
            edgeColumns = new int[]{0, 1, SIZE-2, SIZE-1};
        }

        for (int i = 0; i < 2; i++) {
            int col = edgeColumns[halfRand.nextInt(edgeColumns.length)];
            int mirroredCol = SIZE - 1 - col;

            for (int j = 0; j < 2; j++) {
                int row = startRow + halfRand.nextInt(endRow - startRow);

                if (startRow == 0) {
                    row = Math.min(row, 1);
                } else {
                    row = Math.max(row, SIZE - 2);
                }

                g2d.setColor(Color.WHITE);
                g2d.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g2d.fillRect(mirroredCol * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawTriangle(Graphics2D g2d, int x, int y, int side) {
        Polygon triangle = new Polygon();
        switch (side) {
            case 0: // Bottom
                triangle.addPoint(x, y + CELL_SIZE);
                triangle.addPoint(x + CELL_SIZE, y + CELL_SIZE);
                triangle.addPoint(x + CELL_SIZE/2, y);
                break;
            case 1: // Top
                triangle.addPoint(x, y);
                triangle.addPoint(x + CELL_SIZE, y);
                triangle.addPoint(x + CELL_SIZE/2, y + CELL_SIZE);
                break;
            case 2: // Left
                triangle.addPoint(x, y);
                triangle.addPoint(x, y + CELL_SIZE);
                triangle.addPoint(x + CELL_SIZE, y + CELL_SIZE/2);
                break;
            case 3: // Right
                triangle.addPoint(x + CELL_SIZE, y);
                triangle.addPoint(x + CELL_SIZE, y + CELL_SIZE);
                triangle.addPoint(x, y + CELL_SIZE/2);
                break;
        }
        g2d.setColor(accentColor);
        g2d.fill(triangle);
    }

    private void drawHalfSquare(Graphics2D g2d, int x, int y, int side) {
        g2d.setColor(accentColor);
        switch (side) {
            case 0: // Bottom half
                g2d.fillRect(x, y + CELL_SIZE/2, CELL_SIZE, CELL_SIZE/2);
                break;
            case 1: // Top half
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE/2);
                break;
            case 2: // Left half
                g2d.fillRect(x, y, CELL_SIZE/2, CELL_SIZE);
                break;
            case 3: // Right half
                g2d.fillRect(x + CELL_SIZE/2, y, CELL_SIZE/2, CELL_SIZE);
                break;
        }
    }

    private int mirrorSideVertically(int side) {
        return (side == 2) ? 3 : (side == 3) ? 2 : side;
    }

    private void checkAndReplaceDenseAreas(Graphics2D g2d, int baseSeed) {
        int[][] grid = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int color = image.getRGB(col * CELL_SIZE + CELL_SIZE / 2, row * CELL_SIZE + CELL_SIZE / 2);
                grid[row][col] = (color == accentColor.getRGB()) ? 1 : 0;
            }
        }

        Random rand = new Random(baseSeed);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 1 && countNeighbors(grid, row, col) > 8) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                    if (rand.nextBoolean()) {
                        int halfSquareSide = rand.nextInt(2);
                        drawHalfSquare(g2d, x, y, halfSquareSide);
                    } else {
                        int triangleSide = rand.nextInt(2);
                        drawTriangle(g2d, x, y, triangleSide);
                    }
                }
            }
        }
    }

    private int countNeighbors(int[][] grid, int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE) {
                    count += grid[newRow][newCol];
                }
            }
        }
        return count;
    }

    private static int hashCodeFromBytes(byte[] bytes) {
        int hash = 0;
        for (byte b : bytes) {
            hash = (hash * 31) ^ (b & 0xFF);
        }
        return hash;
    }

    void saveImage() {
        try {
            File outputFile = new File("img/" + inputText + ".png");
            ImageIO.write(image, "png", outputFile);
            JOptionPane.showMessageDialog(this,
                    "Img download to img/" + inputText + ".png");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error via download", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}