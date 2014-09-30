package ru.izebit;

import java.io.*;
import java.util.Properties;

public class ConfigureFile {

    final private String FILENAME = "configure.dat";
    private Properties configure;

    public ConfigureFile() throws IOException {
        configure = new Properties();
        FileInputStream fr = null;
        try {
            fr = new FileInputStream(FILENAME);
        } catch (FileNotFoundException e) {
            configure.setProperty("mapHeight", "200");
            configure.setProperty("mapWidth", "200");
            configure.setProperty("amountIteration", "1000");
            configure.setProperty("sizeSegment", "3");
            configure.setProperty("badColour", "8912655");
            configure.setProperty("percentDamage", "1");
            FileOutputStream f = new FileOutputStream(FILENAME);
            configure.store(f, "configure Neuron Network");
        }
        if (fr != null) {
            configure.load(fr);
        }
    }

    public Properties getProperties() {
        return configure;
    }

    public void saveProperties() {
        try {
            FileOutputStream f = new FileOutputStream("configure.dat");
            configure.store(f, "configure Neuron Network");
        } catch (IOException ex) {
            System.out.println("невозможно сохранить файл");
        }
    }

    public void setProperties(String key, String value) {
        configure.setProperty(key, value);
    }

    public String getProperties(String key) {
        return configure.getProperty(key);
    }
}
