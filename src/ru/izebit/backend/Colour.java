package ru.izebit.backend;

public class Colour {

    public int rgb[];
    private static int bufResult[] = new int[3];

    public Colour(int value) {
        rgb = new int[3];
        rgb[0] = (value >> 16) & 0x0ff; //red
        rgb[1] = (value >> 8) & 0x0ff;  //green
        rgb[2] = value & 0x0ff;         //blue
    }

    public int getColour() {
        return (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]);
    }

    public int getDifference(Colour other) {
        int res = 0;
        res += Math.abs(rgb[0] - other.rgb[0]);
        res += Math.abs(rgb[1] - other.rgb[1]);
        res += Math.abs(rgb[2] - other.rgb[2]);
        return res;
    }

    public void setRGB(int[] colours) {
        rgb[0] = colours[0];
        rgb[1] = colours[1];
        rgb[2] = colours[2];
    }

    public static int[] multiply(int[] colours, float numeric) {
        bufResult[0] = (int) (colours[0] * numeric);
        bufResult[1] = (int) (colours[1] * numeric);
        bufResult[2] = (int) (colours[2] * numeric);
        return bufResult;
    }

    public int[] subtract(Colour other) {
        bufResult[0] = rgb[0] - other.rgb[0];
        bufResult[1] = rgb[1] - other.rgb[1];
        bufResult[2] = rgb[2] - other.rgb[2];
        return bufResult;
    }

    public int[] add(int[] colours) {
        bufResult[0] = (rgb[0] + colours[0] > 255) ? 255 : rgb[0] + colours[0];
        bufResult[0] = (bufResult[0] < 0) ? 0 : bufResult[0];

        bufResult[1] = (rgb[1] + colours[1] > 255) ? 255 : rgb[1] + colours[1];
        bufResult[1] = (bufResult[1] < 0) ? 0 : bufResult[1];

        bufResult[2] = (rgb[2] + colours[2] > 255) ? 255 : rgb[2] + colours[2];
        bufResult[2] = (bufResult[2] < 0) ? 0 : bufResult[2];

        return bufResult;
    }
}
