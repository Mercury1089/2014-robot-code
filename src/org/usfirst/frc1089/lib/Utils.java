package org.usfirst.frc1089.lib;

public class Utils {
    public static boolean inRange(double num, double bottom, double top) {
        return top >= num && bottom <= num;
    }
    
    /** clamp @num to between bottom and top
     * @param num the number to be clamped
     * @param bottom minimum value
     * @param top maximum value
     * @return the clamped value*/
    public static double coerce(double num, double bottom, double top) {
        if(inRange(num, bottom, top)) {
            return num;
        } else {
            return contain(num, bottom, top);
        }
    }
    
    public static double distanceToBS(double d) {
        double bs = 0;
        if(d != -1.0) {
            double correction = 0;
            if(d > 16.5) {
                correction = d / 6;
            }
            bs = d * d * .3296 - 1.9104 * d + 77.048 + correction;
        } else {
            bs = 115;
        }
        return coerce(bs, 0, 150);
    }
    
    public static double contain(double val, double low, double hi) {
        return Math.min(Math.max(val, low), hi);
    }
    
    public static int contain(int val, int low, int hi) {
        return Math.min(Math.max(val, low), hi);
    }
    
    public static int sign(double num) {
        if(num > 0){return 1;}
        else if(num < 0){return -1;}
        return 0;
    }
}