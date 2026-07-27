package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class RandomUtils {


    /**
     * Generates a level using a split-normal (non-symmetric Gaussian) distribution.
     */
    public static int generateLevel(RandomSource random, int minLevel, int maxLevel, int averageLevel) {
        // Calculate standard deviations so the 0.025 (2.5%) tail cutoffs land exactly on min and max
        double sigma1 = (averageLevel - minLevel) / 1.96D;
        double sigma2 = (maxLevel - averageLevel) / 1.96D;
        double levelSample;
        int maxAttempts = 100; // Safeguard loop
        int attempt = 0;
        do {
            // nextGaussian() returns a standard normal variable (mean=0.0, std=1.0)
            double gaussian = random.nextGaussian();

            if (gaussian < 0) {
                // Left side of the peak (uses sigma1)
                levelSample = averageLevel + (gaussian * sigma1);
            } else {
                // Right side of the peak (uses sigma2)
                levelSample = averageLevel + (gaussian * sigma2);
            }
            attempt++;
        } while ((levelSample < minLevel || levelSample > maxLevel) && attempt < maxAttempts);
        
        // Fallback to averageLevel if it fails to converge within range
        if (levelSample < minLevel || levelSample > maxLevel) {
            levelSample = averageLevel;
        }
        
        return (int) Math.round(Mth.clamp(levelSample, minLevel, maxLevel));
    }
}
