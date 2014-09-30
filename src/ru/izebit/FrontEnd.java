package ru.izebit;/*
 * графическая оболочка для программы восстановления изображения
 */


import ru.izebit.backend.Map;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class FrontEnd extends JComponent implements ActionListener {

    private final int WIDTH = 800;
    private final int HEGTH = 600;
    //кнопочки
    private JButton saveImage;
    private JButton openImage;
    private JButton mutableImage;
    private JButton restoreImage;
    private JButton openMap;
    private JButton createMap;
    private JButton configButton;
    private HashMap<String, Boolean> buttonEnable = new HashMap<String, Boolean>();
    private ConfigureFile config;
    private JFileChooser openDialog;
    //прогресс бар
    private JProgressBar jpb;
    //картинки 
    private BufferedImage topImage;
    private ImageIcon topIcon;
    private JLabel topImageLabel;
    private BufferedImage bottomImage;
    private ImageIcon bottomIcon;
    private JLabel bottomImageLabel;
    private BufferedImage mapImage;
    private ImageIcon mapIcon;
    private JLabel mapImageLabel;
    private BufferedImage currentImage;
    private BufferedImage currentMap;

    public FrontEnd() {
        try {
            config = new ConfigureFile();
        } catch (IOException ex) {
            System.out.println("не могу открыть файл с настройками");
        }

        JFrame mainWindow = new JFrame("restoration of images");
        mainWindow.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        mainWindow.setLayout(null);
        mainWindow.setSize(WIDTH, HEGTH);
        mainWindow.setResizable(false);

        //создание кнопочек        
        int buttonHeigth = HEGTH / 20;
        int buttonWidth = WIDTH / 6;

        openImage = new JButton("open image");
        openImage.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50, buttonWidth, buttonHeigth);
        openImage.addActionListener(this);
        mainWindow.add(openImage);

        mutableImage = new JButton("mutable image");
        mutableImage.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth + 3, buttonWidth, buttonHeigth);
        mutableImage.addActionListener(this);
        mainWindow.add(mutableImage);

        restoreImage = new JButton("restore image");
        restoreImage.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth * 2 + 6, buttonWidth, buttonHeigth);
        restoreImage.addActionListener(this);
        mainWindow.add(restoreImage);

        saveImage = new JButton("save image");
        saveImage.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth * 3 + 9, buttonWidth, buttonHeigth);
        saveImage.addActionListener(this);
        mainWindow.add(saveImage);

        openMap = new JButton("open map");
        openMap.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth * 4 + 18, buttonWidth, buttonHeigth);
        openMap.addActionListener(this);
        mainWindow.add(openMap);

        createMap = new JButton("create map");
        createMap.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth * 5 + 21, buttonWidth, buttonHeigth);
        createMap.addActionListener(this);
        mainWindow.add(createMap);

        configButton = new JButton("options");
        configButton.setBounds(WIDTH - buttonWidth - 10, HEGTH / 50 + buttonHeigth * 6 + 30, buttonWidth, buttonHeigth);
        configButton.addActionListener(this);
        mainWindow.add(configButton);

        buttonEnable.put(openImage.getText(), false);
        buttonEnable.put(restoreImage.getText(), false);
        buttonEnable.put(mutableImage.getText(), false);
        buttonEnable.put(openMap.getText(), false);
        buttonEnable.put(saveImage.getText(), false);
        buttonEnable.put(createMap.getText(), false);
        enablePressButton();

        jpb = new JProgressBar();
        jpb.setBounds(10, HEGTH - 57, WIDTH - 20, WIDTH / 40);
        jpb.setValue(0);
        jpb.setStringPainted(true);
        mainWindow.add(jpb);

        topIcon = new ImageIcon();
        topImageLabel = new JLabel(topIcon, SwingConstants.CENTER);
        topImageLabel.setBorder(new TitledBorder("original picture"));
        topImageLabel.setBounds(10, 0, WIDTH / 2 + WIDTH / 12 - 20, 270);
        mainWindow.add(topImageLabel);

        bottomIcon = new ImageIcon();
        bottomImageLabel = new JLabel(bottomIcon, SwingConstants.CENTER);
        bottomImageLabel.setBorder(new TitledBorder("processed picture"));
        bottomImageLabel.setBounds(10, 270, WIDTH / 2 + WIDTH / 12 - 20, 270);
        mainWindow.add(bottomImageLabel);

        mapIcon = new ImageIcon();
        mapImageLabel = new JLabel(mapIcon, SwingConstants.CENTER);
        mapImageLabel.setBorder(new TitledBorder("map"));
        mapImageLabel.setBounds(WIDTH / 2 + WIDTH / 12 - 3, 270, 330, 270);
        mainWindow.add(mapImageLabel);

        mainWindow.setVisible(true);

        openDialog = new JFileChooser();

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JButton com = (JButton) ae.getSource();
        buttonEnable.put(com.getText(), true);
        enablePressButton();

        if (com.equals(configButton)) {
            new ConfigDialog(400, 400, config);
        }
        if (com.equals(openImage) || com.equals(openMap)) {
            openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
            openDialog.setDialogTitle("select a file to open");
            int returnVal = openDialog.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String key = (com.equals(openImage)) ? "openImage" : "openMap";
                config.setProperties(key, openDialog.getSelectedFile().getPath());
                config.saveProperties();
                if (com.equals(openImage)) {
                    String name = openDialog.getSelectedFile().getPath();
                    repaintImage(getBufferedImage(name), 0);
                } else {
                    String name = openDialog.getSelectedFile().getPath();
                    if (name != null) {
                        repaintImage(getBufferedImage(name), 2);
                    }
                }
            }
        }
        if (com.equals(createMap)) {
            openDialog.setDialogType(JFileChooser.SAVE_DIALOG);
            openDialog.setDialogTitle("select a location to save");
            int returnVal = openDialog.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                config.setProperties("openMap", openDialog.getSelectedFile().getPath());
                config.saveProperties();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Map m = new Map(config.getProperties());
                        currentMap = m.create(jpb);
                        mapImage = currentMap;
                        saveCurrentImage(config.getProperties("openMap"), mapImage);
                        repaintImage(mapImage, 2);
                    }
                }).start();
            }
        }
        if (com.equals(mutableImage)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ru.izebit.backend.Image im = new ru.izebit.backend.Image(config.getProperties());
                    currentImage = im.mutable(jpb);
                    bottomImage = currentImage;
                    repaintImage(bottomImage, 1);
                }
            }).start();
        }
        if (com.equals(saveImage)) {
            openDialog.setDialogType(JFileChooser.SAVE_DIALOG);
            openDialog.setDialogTitle("select a location to save");
            int returnVal = openDialog.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                saveCurrentImage(openDialog.getSelectedFile().getPath(), currentImage);
            }
        }
        if (com.equals(restoreImage)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ru.izebit.backend.Image im = new ru.izebit.backend.Image(config.getProperties());
                    currentImage = im.restoreImage(config.getProperties(), jpb);
                    bottomImage = currentImage;
                    repaintImage(bottomImage, 1);
                }
            }).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new FrontEnd();
            }
        });
    }

    private void enablePressButton() {
        saveImage.setEnabled(buttonEnable.get(restoreImage.getText()) || buttonEnable.get(mutableImage.getText()));
        mutableImage.setEnabled(buttonEnable.get(openImage.getText()));
        restoreImage.setEnabled(buttonEnable.get(openImage.getText()) && buttonEnable.get(openMap.getText()));
        createMap.setEnabled(buttonEnable.get(openImage.getText()));
        openImage.setEnabled(true);
    }

    private BufferedImage scaledImage(final BufferedImage im, int width, int heigth) {
        float scaled = 0;
        if (heigth > width) {
            scaled = ((float) width) / ((float) im.getWidth());
        } else {
            scaled = ((float) heigth) / ((float) im.getHeight());
        }
        int scaledWidth = (int) (im.getWidth() * scaled);
        int scaledHeigth = (int) (im.getHeight() * scaled);
        Image scaledImage = im.getScaledInstance(scaledWidth, scaledHeigth, Image.SCALE_FAST);
        BufferedImage picture = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = picture.getGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        return picture;
    }

    private void repaintImage(BufferedImage picture, int pos) {
        if (pos == 0 || pos == 1) {
            BufferedImage im = scaledImage(picture, WIDTH / 2 + WIDTH / 12, 250);
            if (pos == 0) {
                topImage = im;
                topIcon.setImage(topImage);
                topImageLabel.repaint();
            } else {
                bottomImage = im;
                bottomIcon.setImage(bottomImage);
                bottomImageLabel.repaint();
            }
        } else {
            mapImage = scaledImage(picture, 310, 250);
            mapIcon.setImage(mapImage);
            mapImageLabel.repaint();
        }
    }

    private BufferedImage getBufferedImage(String name) {
        BufferedImage picture = null;
        try {
            picture = javax.imageio.ImageIO.read(new File(name));
        } catch (IOException ex) {
            System.out.println("не могу открыть изображение");
        }
        return picture;
    }

    private void saveCurrentImage(String name, BufferedImage currentImage) {
        File f = new File(name);
        try {
            javax.imageio.ImageIO.write(currentImage, "PNG", f);
        } catch (IOException ex) {
            System.out.println("не могу сохранить изображение");
        }
    }
}
