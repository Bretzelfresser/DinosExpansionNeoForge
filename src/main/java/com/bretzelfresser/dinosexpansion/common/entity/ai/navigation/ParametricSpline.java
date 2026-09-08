package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ParametricSpline {
    private final List<Vec3> controlPoints;
    private final double[] segmentLengths;
    private final double[] cumulativeDistances;
    private final double totalLength;

    public ParametricSpline(List<Vec3> keypoints) {
        this.controlPoints = keypoints != null ? keypoints : new ArrayList<>();
        int n = this.controlPoints.size();
        if (n < 2) {
            this.segmentLengths = new double[0];
            this.cumulativeDistances = new double[0];
            this.totalLength = 0.0D;
            return;
        }

        this.segmentLengths = new double[n - 1];
        this.cumulativeDistances = new double[n];
        this.cumulativeDistances[0] = 0.0D;

        double distSum = 0.0D;
        for (int i = 0; i < n - 1; i++) {
            double len = this.controlPoints.get(i).distanceTo(this.controlPoints.get(i + 1));
            this.segmentLengths[i] = len;
            distSum += len;
            this.cumulativeDistances[i + 1] = distSum;
        }
        this.totalLength = distSum;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public boolean isEmpty() {
        return controlPoints.size() < 2;
    }

    /**
     * Evaluates continuous 3D position P(s) along the curve at total arc-length distance s.
     */
    public Vec3 getPositionAtDistance(double s) {
        if (isEmpty()) return controlPoints.isEmpty() ? Vec3.ZERO : controlPoints.get(0);
        if (s <= 0.0D) return controlPoints.get(0);
        if (s >= totalLength) return controlPoints.get(controlPoints.size() - 1);

        int segmentIndex = findSegmentIndex(s);
        double localDist = s - cumulativeDistances[segmentIndex];
        double segLen = segmentLengths[segmentIndex];
        double t = segLen > 0.0001D ? localDist / segLen : 0.0D;

        return getPositionOnSegment(segmentIndex, t);
    }

    /**
     * Evaluates analytical tangent vector P'(s) (velocity direction) at total arc-length distance s.
     */
    public Vec3 getTangentAtDistance(double s) {
        if (isEmpty()) return new Vec3(1, 0, 0);

        double delta = 0.05D;
        Vec3 p1 = getPositionAtDistance(Math.max(0, s - delta));
        Vec3 p2 = getPositionAtDistance(Math.min(totalLength, s + delta));
        Vec3 dir = p2.subtract(p1);
        return dir.lengthSqr() > 1.0E-6D ? dir.normalize() : new Vec3(1, 0, 0);
    }

    private int findSegmentIndex(double s) {
        for (int i = 0; i < segmentLengths.length; i++) {
            if (s <= cumulativeDistances[i + 1]) {
                return i;
            }
        }
        return segmentLengths.length - 1;
    }

    private Vec3 getPositionOnSegment(int i, double t) {
        int n = controlPoints.size();
        Vec3 p1 = controlPoints.get(i);
        Vec3 p2 = controlPoints.get(i + 1);
        Vec3 p0 = (i > 0) ? controlPoints.get(i - 1) : p1.scale(2.0D).subtract(p2);
        Vec3 p3 = (i < n - 2) ? controlPoints.get(i + 2) : p2.scale(2.0D).subtract(p1);

        double t2 = t * t;
        double t3 = t2 * t;

        double f0 = -0.5D * t3 + t2 - 0.5D * t;
        double f1 = 1.5D * t3 - 2.5D * t2 + 1.0D;
        double f2 = -1.5D * t3 + 2.0D * t2 + 0.5D * t;
        double f3 = 0.5D * t3 - 0.5D * t2;

        return new Vec3(
                p0.x * f0 + p1.x * f1 + p2.x * f2 + p3.x * f3,
                p0.y * f0 + p1.y * f1 + p2.y * f2 + p3.y * f3,
                p0.z * f0 + p1.z * f1 + p2.z * f2 + p3.z * f3
        );
    }
}
