/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ap.dronemassa;

import java.awt.Point;
import java.util.Random;

/**
 *
 * @author Jacopo
 */
public class Position extends Point{
    
    public Position(int x, int y)
    {
        super(x,y);
    }

    public static Position generateRandomDelta() {
        Random r = new Random();
        int newX = (int) (Math.random() * 20) - 10;
        int newY = (int) (Math.random() * 20) - 10;

        return new Position(newX, newY);
    }
    
    public static int boundCoord(int v, int minBound, int maxBound) {
        if(v < minBound)
            return minBound;
        else if(v > maxBound)
            return maxBound;
        else
            return v;       
    }
}
