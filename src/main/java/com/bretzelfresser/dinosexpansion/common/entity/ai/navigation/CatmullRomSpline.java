package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CatmullRomSpline {

    /**
     * Interpolates a list of 3D control points (pruned keypoints) into a smooth Catmull-Rom spline path.
     * @param controlPoints Pruned 3D keypoints from string-pulling.
     * @param samplesPerSegment Minimum number of interpolated sub-waypoint samples per segment.
     * @return List of smooth interpolated 3D waypoints.
     */
    public static List<Vec3> generateSpline(List<Vec3> controlPoints, int samplesPerSegment) {
        if (controlPoints == null || controlPoints.size() <= 2) {
            return controlPoints != null ? new ArrayList<>(controlPoints) : new ArrayList<>();
        }

        List<Vec3> splinePoints = new ArrayList<>();
        int n = controlPoints.size();

        for (int i = 0; i < n - 1; i++) {
            Vec3 p1 = controlPoints.get(i);
            Vec3 p2 = controlPoints.get(i + 1);

            // Extend endpoints smoothly for edge control points
            Vec3 p0 = (i > 0) ? controlPoints.get(i - 1) : p1.scale(2.0D).subtract(p2);
            Vec3 p3 = (i < n - 2) ? controlPoints.get(i + 2) : p2.scale(2.0D).subtract(p1);

            // Adjust samples based on distance so long segments get sufficient resolution
            double segmentLen = p1.distanceTo(p2);
            int samples = Math.max(samplesPerSegment, (int) Math.ceil(segmentLen * 2.0D));

            for (int step = 0; step < samples; step++) {
                double t = (double) step / (double) samples;
                Vec3 interpolated = interpolateCatmullRom(p0, p1, p2, p3, t);
                splinePoints.add(interpolated);
            }
        }

        // Add final destination point
        splinePoints.add(controlPoints.get(n - 1));

        return splinePoints;
    }

    /**
     * Standard 3D Catmull-Rom spline interpolation formula.
     */
    private static Vec3 interpolateCatmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double f0 = -0.5D * t3 + t2 - 0.5D * t;
        double f1 = 1.5D * t3 - 2.5D * t2 + 1.0D;
        double f2 = -1.5D * t3 + 2.0D * t2 + 0.5D * t;
        double f3 = 0.5D * t3 - 0.5D * t2;

        double x = p0.x * f0 + p1.x * f1 + p2.x * f2 + p3.x * f3;
        double y = p0.y * f0 + p1.y * f1 + p2.y * f2 + p3.y * f3;
        double z = p0.z * f0 + p1.z * f1 + p2.z * f2 + p3.z * f3;

        return new Vec3(x, y, z);
    }
}
