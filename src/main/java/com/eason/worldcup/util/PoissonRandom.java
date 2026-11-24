package com.eason.worldcup.util;

import java.util.SplittableRandom;

public final class PoissonRandom {

    private PoissonRandom() {
    }

    public static int next(double lambda, SplittableRandom random) {
        return new Sampler(lambda).next(random);
    }

    public static final class Sampler {

        private final double limit;

        private final boolean nonPositive;

        public Sampler(double lambda) {
            limit = Math.exp(-lambda);
            nonPositive = lambda <= 0;
        }

        public int next(SplittableRandom random) {
            if (nonPositive) {
                return 0;
            }
            int k = 0;
            double product = 1.0D;
            do {
                k++;
                product *= random.nextDouble();
            } while (product > limit);
            return k - 1;
        }

    }

}
