package com.ap.dronemassa;

import java.awt.Point;
import java.util.Random;

/**
 * Auxiliary class which represents a pair (a point in screen coordinates)
 */
public class Position extends Point{
    
    public Position(int x, int y)
    {
        super(x,y);
    }
    
    /**
     * generateRandomDelta: generates a new randomly pair of integers
     * (deltaX, deltaY)
     */
    public static Position generateRandomDelta() {
        Random r = new Random();
        int newX = (int) (Math.random() * 20) - 10;
        int newY = (int) (Math.random() * 20) - 10;

        return new Position(newX, newY);
    }
    
    /**
     * bounds the passed coordinate within the specified limits
     */
    public static int boundCoord(int v, int minBound, int maxBound) {
        if(v < minBound)
            return minBound;
        else if(v > maxBound)
            return maxBound;
        else
            return v;       
    }
}
