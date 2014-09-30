package ru.izebit.backend;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/*
 * класс изображение, используется при создании карты, а так же при работе с
 * испорченым изображением
 */
public class Image {

    private Properties config;
    private Colour picture[][];
    private int width;
    private int heigth;
    private int sizeSection;
    private int badColour;

    public Image(Properties configure) {
        config = configure;
        badColour = new Colour(Integer.parseInt(config.getProperty("badColour"))).getColour();
        sizeSection = Integer.parseInt(config.getProperty("sizeSegment"));

        File f = new File(config.getProperty("openImage"));
        BufferedImage bi = null;
        try {
            bi = javax.imageio.ImageIO.read(f);
        } catch (IOException ex) {
            System.out.println("изображение не было найдено");
        }
        if (bi != null) {
            width = bi.getWidth();
            heigth = bi.getHeight();
            picture = new Colour[heigth][width];
            for (int x = 0; x < heigth; x++) {
                for (int y = 0; y < width; y++) {
                    picture[x][y] = new Colour(bi.getRGB(y, x));
                }
            }
        }
    }

    public BufferedImage mutable(final JProgressBar jpb) {
        int percent = Integer.parseInt(config.getProperty("percentDamage"));
        int oneHundredPercent = 100;
        int amountError = (width * heigth * percent) / oneHundredPercent;
        Random r = new Random();
        int x, y;
        Colour replace = new Colour(badColour);

        int step = amountError / 100;
        int progress = 1;
        while (amountError > 0) {
            x = r.nextInt(heigth);
            y = r.nextInt(width);
            if (picture[x][y].getColour() != badColour) {
                picture[x][y] = replace;
                amountError--;
                if (amountError % step == 0) {
                    final int VAL = ++progress;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            jpb.setValue(VAL);
                        }
                    });
                }
            }
        }
        return getBufferedImage();
    }

    public BufferedImage restoreImage(Properties config, final JProgressBar jpb) {
        Map m = new Map(config);
        m.open();
        int countError = getAmountError();
        int step = countError / 100;
        int progress = 1;
        while (true) {
            int error = 0;
            for (int i = sizeSection / 2; i < getHeigth() - sizeSection / 2; i++) {
                for (int j = sizeSection / 2; j < getWidth() - sizeSection / 2; j++) {
                    Colour[] s = getSegment(i, j);
                    if (isHaveBadColour(s) == 1) {
                        error++;
                        countError--;
                        if (countError % step == 0) {
                            final int VAL = progress++;
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    jpb.setValue(VAL);
                                }
                            });
                        }
                        Colour[] replace = m.findBMU(s);
                        for (int l = 0; l < replace.length; l++) {
                            if (s[l].getColour() == badColour) {
                                setColourInSegment(i, j, l, replace[l]);
                            }
                        }
                    }
                }
            }
            if (error == 0) {
                break;
            }
        }
        return getBufferedImage();
    }

    public Colour[] getSegment(int x, int y) {
        Colour result[];
        result = new Colour[sizeSection * sizeSection];
        int l = x - sizeSection / 2;
        int t = y - sizeSection / 2;
        for (int i = 0; i < sizeSection * sizeSection; i++) {
            result[i] = picture[l + i / sizeSection][t + i % sizeSection];
        }
        return result;
    }

    public Colour[][] getTopTenSegment() {
        boolean hasBadColour;
        TreeMap<Integer, Colour[]> topTen = new TreeMap<Integer, Colour[]>();
        for (int i = sizeSection / 2; i < heigth - sizeSection / 2; i++) {
            for (int j = sizeSection / 2; j < width - sizeSection / 2; j++) {
                int bufMax = 0;
                hasBadColour = false;
                Colour segment[] = this.getSegment(i, j);
                for (int l = 0; l < segment.length; l++) {
                    if (segment[l].getColour() == badColour) {
                        hasBadColour = true;
                        break;
                    }
                    bufMax += segment[l].getDifference(segment[segment.length / 2]);
                }
                if (!hasBadColour) {
                    if (topTen.size() < 9) {
                        topTen.put(bufMax, segment);
                    } else {
                        int min = topTen.firstKey();
                        if (min < bufMax) {
                            topTen.remove(min);
                            topTen.put(bufMax, segment);
                        }
                    }
                }
            }
        }

        Colour[][] result = new Colour[9][];
        int i = 0;
        for (Integer key : topTen.keySet()) {
            result[i++] = topTen.get(key);
        }
        return result;
    }

    public void setColourInSegment(int x, int y, int position, Colour insertColour) {
        int l = x - sizeSection / 2;
        int t = y - sizeSection / 2;
        picture[l + position / sizeSection][t + position % sizeSection] = insertColour;
    }

    public int isHaveBadColour(Colour[] segment) {
        if (segment == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < segment.length; i++) {
            if (segment[i].getColour() == badColour) {
                count++;
            }
        }
        return count;
    }

    public int getAmountError() {
        int count = 0;
        for (int x = 0; x < heigth - 1; x++) {
            for (int y = 0; y < width; y++) {
                if (picture[x][y].getColour() == badColour) {
                    count++;
                }
            }
        }
        return count;
    }

    private BufferedImage getBufferedImage() {
        BufferedImage im = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < heigth; x++) {
            for (int y = 0; y < width; y++) {
                int colour = picture[x][y].getColour();
                im.setRGB(y, x, colour);
            }
        }
        return im;
    }

    public int getHeigth() {
        return heigth;
    }

    public int getWidth() {
        return width;
    }
}
