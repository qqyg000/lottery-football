package com.eason.worldcup.util;

import java.util.SplittableRandom;

public final class PoissonRandom {

    private PoissonRandom() {
    }

    public static int next(double lambda, SplittableRandom random) {
        if (lambda <= 0) {
            return 0;
        }
        double limit = Math.exp(-lambda);
        int k = 0;
        double product = 1.0D;
        do {
            k++;
            product *= random.nextDouble();
        } while (product > limit);
        return k - 1;
    }

}
