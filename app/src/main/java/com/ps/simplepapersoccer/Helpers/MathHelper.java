package com.ps.simplepapersoccer.Helpers;

public final class MathHelper
{
    public static double euclideanDistance(int x1, int x2, int y1, int y2)
    {
        int ycoord = Math.abs(y1 - y2);
        int xcoord = Math.abs(x1 - x2);
        double distance = Math.sqrt((ycoord)*(ycoord) + (xcoord)*(xcoord));
        return distance;
    }

    public static double distance(int x1, int x2, int y1, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int min = Math.min(dx, dy);
        int max = Math.max(dx, dy);

        int diagonalSteps = min;
        int straightSteps = max - min;

        return Math.sqrt(2) * diagonalSteps + straightSteps;
    }
}
