package ru.izebit;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ConfigDialog {

    private ConfigureFile conf;
    private Color badColour;

    public ConfigDialog(int width, int heigth, final ConfigureFile config) {
        conf = config;
        badColour = new Color(Integer.parseInt(conf.getProperties("badColour")));

        JFrame jf = new JFrame("options");
        jf.setSize(width, heigth);
        jf.setResizable(false);
        jf.setLayout(null);

        //ползунок для выбора размера сегмента
        JLabel showSizeSegment = new JLabel("segment size");
        showSizeSegment.setBounds(10, 7, width / 3, 40);
        jf.add(showSizeSegment);

        final JSlider selectSizeSegment = new JSlider(1, 11);
        selectSizeSegment.setMajorTickSpacing(2);
        selectSizeSegment.setSnapToTicks(true);
        selectSizeSegment.setPaintLabels(true);
        selectSizeSegment.setBounds(width / 2 - 20, 10, width / 2, 40);
        selectSizeSegment.setValue(Integer.parseInt(conf.getProperties("sizeSegment")));
        selectSizeSegment.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                if (!selectSizeSegment.getValueIsAdjusting()) {
                    int size = selectSizeSegment.getValue();
                    size = (size % 2 == 0) ? size + 1 : size;
                    conf.setProperties("sizeSegment", Integer.toString(size));
                    config.saveProperties();
                }
            }
        });

        jf.add(selectSizeSegment);

        //ползунок для выбора колличества нейронов
        JLabel showCountNeuron = new JLabel("<html>number of neurons<br> x10000</html>");
        showCountNeuron.setBounds(10, heigth / 8, width / 3, 40);
        jf.add(showCountNeuron);

        final JSlider selectCountNeuron = new JSlider(0, 25);
        selectCountNeuron.setMajorTickSpacing(5);
        selectCountNeuron.setPaintTicks(true);
        selectCountNeuron.setSnapToTicks(true);
        selectCountNeuron.setPaintLabels(true);
        selectCountNeuron.setBounds(width / 2 - 20, heigth / 8, width / 2, 45);
        selectCountNeuron.setValue((Integer.parseInt(conf.getProperties("mapWidth")) * Integer.parseInt(conf.getProperties("mapHeight"))) / 10000);
        selectCountNeuron.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                if (!selectCountNeuron.getValueIsAdjusting()) {
                    int size = (int) Math.sqrt(selectCountNeuron.getValue() * 10000);
                    conf.setProperties("mapHeight", Integer.toString(size));
                    conf.setProperties("mapWidth", Integer.toString(size));
                    config.saveProperties();
                }
            }
        });
        jf.add(selectCountNeuron);

        //ползунок для выбора колличества итераций
        JLabel showCountIteration = new JLabel("<html>number of iterations<br>x1000</html>");
        showCountIteration.setBounds(10, heigth / 4, width / 3, 40);
        jf.add(showCountIteration);

        final JSlider selectCountIteration = new JSlider(1, 10);
        selectCountIteration.setMajorTickSpacing(1);
        selectCountIteration.setSnapToTicks(true);
        selectCountIteration.setPaintLabels(true);
        selectCountIteration.setBounds(width / 2 - 20, heigth / 4, width / 2, 40);
        selectCountIteration.setValue(Integer.parseInt(conf.getProperties("amountIteration")) / 1000);
        selectCountIteration.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                if (!selectCountIteration.getValueIsAdjusting()) {
                    int size = selectCountIteration.getValue() * 1000;
                    conf.setProperties("amountIteration", Integer.toString(size));
                    config.saveProperties();
                }
            }
        });
        jf.add(selectCountIteration);

        //выбор процента повреждения изображения
        JLabel showPercentDamage = new JLabel("percent of damage");
        showPercentDamage.setBounds(10, 3 * heigth / 8, width / 3, 40);
        jf.add(showPercentDamage);

        final JSlider selectPercentDamage = new JSlider(1, 10);
        selectPercentDamage.setMajorTickSpacing(1);
        selectPercentDamage.setSnapToTicks(true);
        selectPercentDamage.setPaintLabels(true);
        selectPercentDamage.setBounds(width / 2 - 20, 3 * heigth / 8, width / 2, 40);
        selectPercentDamage.setValue(Integer.parseInt(conf.getProperties("percentDamage")) / 10);
        selectPercentDamage.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                if (!selectPercentDamage.getValueIsAdjusting()) {
                    int size = selectPercentDamage.getValue() * 10;
                    conf.setProperties("percentDamage", Integer.toString(size));
                    config.saveProperties();
                }
            }
        });
        jf.add(selectPercentDamage);

        //выбор плохого цвета
        JLabel showBadColour = new JLabel("damage colour");
        showBadColour.setBounds(10, heigth / 2, width / 2, 40);
        jf.add(showBadColour);

        final JLabel bColour = new JLabel();
        bColour.setBackground(badColour);
        bColour.setBounds(width / 2 - 20, heigth / 2, 80, 30);
        bColour.setOpaque(true);
        bColour.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                badColour = JColorChooser.showDialog(null, "select damage colour", badColour);
                if (badColour != null) {
                    bColour.setBackground(badColour);
                    conf.setProperties("badColour", Integer.toString(badColour.getRGB()));
                    config.saveProperties();
                }
            }
        });
        jf.add(bColour);


        jf.setVisible(true);
    }
}
