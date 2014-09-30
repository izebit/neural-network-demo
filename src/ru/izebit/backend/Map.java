package ru.izebit.backend;
/*
 * класс представляющий собой карту по которой происходит восстановление
 * изображения
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class Map {

    private Colour[][][] map;
    private Properties config;
    private int badColour;
    private int sizeSection;
    private int width;
    private int heigth;
    private int maxIteration;
    private static int[] winnerCoordinates = new int[2];

    public Map(Properties configure) {
        config = configure;
        sizeSection = Integer.parseInt(config.getProperty("sizeSegment"));
        width = Integer.parseInt(config.getProperty("mapWidth"));
        heigth = Integer.parseInt(config.getProperty("mapHeight"));
        map = new Colour[heigth][width][sizeSection * sizeSection];

        maxIteration = Integer.parseInt(config.getProperty("amountIteration"));
        badColour = Integer.parseInt(config.getProperty("badColour"));
    }

    public void open() {
        BufferedImage im = null;
        try {
            File f = new File(config.getProperty("openMap"));
            im = javax.imageio.ImageIO.read(f);
        } catch (IOException ex) {
            System.out.println("карта не найдена!");
        }
        if (im != null) {
            for (int x = 0; x < heigth; x++) {
                for (int y = 0; y < width; y++) {
                    for (int i = 0; i < map[0][0].length; i++) {
                        map[x][y][i] = new Colour(im.getRGB(y * sizeSection + i % sizeSection, x * sizeSection + i / sizeSection));
                    }
                }
            }
        }
    }

    public BufferedImage create(final JProgressBar jpb) {
        Image im = new Image(config);
        int backgroundColour = 0;
        for (int x = 0; x < heigth; x++) {
            for (int y = 0; y < width; y++) {
                for (int i = 0; i < map[0][0].length; i++) {
                    map[x][y][i] = new Colour(backgroundColour);
                }
            }
        }
        Colour[][] bestTen = im.getTopTenSegment();

        if (bestTen[8] == null) {
            System.out.println("не могу создать инициализирующие вектора,изображение очень сильно повреждено");
            return null;
        }

        int t = 1;
        for (int i = heigth / 10; i < heigth; i = i + heigth / 2 - heigth / 10) {
            for (int j = width / 10; j < width; j = j + width / 2 - width / 10) {
                map[i][j] = bestTen[t - 1];
                winnerCoordinates[0] = i;
                winnerCoordinates[1] = j;
                updateMap(bestTen[t - 1], t);
                t++;
            }
        }
        int x, y;
        Random r = new Random();

        int step = maxIteration / 100;
        int progress = 1;
        while (t < maxIteration) {
            x = r.nextInt(im.getHeigth() - sizeSection) + sizeSection / 2;
            y = r.nextInt(im.getWidth() - sizeSection) + sizeSection / 2;
            Colour[] seg = im.getSegment(x, y);
            if (im.isHaveBadColour(seg) == 0) {
                findBMU(seg);
                updateMap(seg, t);
                t++;
                if (t % step == 0) {
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

    private BufferedImage getBufferedImage() {
        BufferedImage im = new BufferedImage(width * sizeSection, heigth * sizeSection, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < heigth; x++) {
            for (int y = 0; y < width; y++) {
                for (int i = 0; i < map[0][0].length; i++) {
                    int colour = map[x][y][i].getColour();
                    im.setRGB(y * sizeSection + i % sizeSection, x * sizeSection + i / sizeSection, colour);
                }
            }
        }
        return im;
    }

    public Colour[] findBMU(Colour[] inputVector) {
        int minError = Integer.MAX_VALUE;
        int bufError;
        for (int i = 0; i < heigth; i++) {
            for (int j = 0; j < width; j++) {
                bufError = 0;
                for (int l = 0; l < map[0][0].length; l++) {
                    if (map[i][j][l].getColour() == badColour) {
                        continue;
                    }
                    bufError += map[i][j][l].getDifference(inputVector[l]);
                    if (bufError >= minError) {
                        break;
                    }
                }
                if (minError > bufError) {
                    minError = bufError;
                    winnerCoordinates[0] = i;//x
                    winnerCoordinates[1] = j;//y
                }
            }
        }
        return map[winnerCoordinates[0]][winnerCoordinates[1]];
    }

    private void updateMap(Colour[] inputVector, int iteration) {
        for (int i = 0; i < heigth; i++) {
            for (int j = 0; j < width; j++) {
                float factor = h(i, j, iteration);
                for (int l = 0; l < map[0][0].length; l++) {
                    int[] result = inputVector[l].subtract(map[i][j][l]);
                    result = Colour.multiply(result, factor);
                    result = map[i][j][l].add(result);
                    map[i][j][l].setRGB(result);
                }
            }
        }
    }

    private float h(int iX, int iY, int iteration) {
        float result = 0;
        result = (float) (alf(iteration) * Math.exp(-Math.pow(d(iX, iY), 2) / (2 * delta(iteration))));
        return result;
    }

    private double alf(int iteration) {
        double result = 1;
        if (iteration > 9) {
            result = 1 / (Math.pow((iteration - 9), 0.2));
        }
        return result;
    }

    private double delta(int iteration) {
        return (Math.sqrt(width * heigth) * 5) / Math.sqrt(iteration);
    }

    private int d(int iX, int iY) {
        int subtractX = winnerCoordinates[0] - iX;
        int subtractY = winnerCoordinates[1] - iY;

        int firstMin = (int) Math.sqrt(Math.pow((subtractX + heigth), 2) + Math.pow((subtractY + width), 2));
        int secondMin = (int) Math.sqrt(Math.pow((subtractX - heigth), 2) + Math.pow((subtractY - width), 2));
        int thirdMin = (int) Math.sqrt(Math.pow(subtractX, 2) + Math.pow(subtractY, 2));
        int result = (int) Math.min(firstMin, Math.min(secondMin, thirdMin));

        return result;
    }
}
