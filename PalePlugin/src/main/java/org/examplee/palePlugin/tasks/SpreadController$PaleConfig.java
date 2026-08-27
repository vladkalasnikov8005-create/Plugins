package org.examplee.palePlugin.tasks;

final class SpreadController$PaleConfig {
    private SpreadController$PaleConfig() {
        super();
    }

    static double lerp(double a, double b, double t) {
        if (Double.compare(t, 0.0) > 0) {
            if (Double.compare(t, 1.0) >= 0) {
                return b;
            }
        }
        return a;
    }

}
