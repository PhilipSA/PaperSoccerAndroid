package com.example.papersoccer.papersoccer.Helpers;

public final class MathHelper
{
    public static double euclideanDistance(int x1, int x2, int y1, int y2)
    {
        int ycoord = Math.abs(y1 - y2);
        int xcoord = Math.abs(x1 - x2);
        double distance = Math.sqrt((ycoord)*(ycoord) + (xcoord)*(xcoord));
        return distance;
    }
}
