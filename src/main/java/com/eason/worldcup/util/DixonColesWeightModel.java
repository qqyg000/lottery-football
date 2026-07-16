package com.eason.worldcup.util;

public final class DixonColesWeightModel {

    private static final long[] ANCHOR_DAYS = {0L, 7L, 30L, 90L, 180L, 365L, 730L};

    private static final double[] ANCHOR_WEIGHTS = {1.000D, 0.987D, 0.946D, 0.844D, 0.712D, 0.507D, 0.258D};

    private DixonColesWeightModel() {

    }

    public static double weightForDays(long elapsedDays) {
        long days = Math.max(0L, elapsedDays);
        for (int index = 1; index < ANCHOR_DAYS.length; index++) {
            if (days <= ANCHOR_DAYS[index]) {
                return interpolateLogarithmically(
                        days,
                        ANCHOR_DAYS[index - 1],
                        ANCHOR_DAYS[index],
                        ANCHOR_WEIGHTS[index - 1],
                        ANCHOR_WEIGHTS[index]);
            }
        }
        int lastIndex = ANCHOR_DAYS.length - 1;
        return interpolateLogarithmically(
                days,
                ANCHOR_DAYS[lastIndex - 1],
                ANCHOR_DAYS[lastIndex],
                ANCHOR_WEIGHTS[lastIndex - 1],
                ANCHOR_WEIGHTS[lastIndex]);
    }

    private static double interpolateLogarithmically(
            long days,
            long startDay,
            long endDay,
            double startWeight,
            double endWeight) {
        double progress = (days - startDay) / (double) (endDay - startDay);
        double logWeight = Math.log(startWeight) + progress * (Math.log(endWeight) - Math.log(startWeight));
        return Math.exp(logWeight);
    }

}
