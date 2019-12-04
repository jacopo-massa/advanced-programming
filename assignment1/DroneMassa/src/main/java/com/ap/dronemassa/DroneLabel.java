/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ap.dronemassa;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;

/**
 *
 * @author Jacopo
 */
public class DroneLabel extends JLabel implements PropertyChangeListener {    
    public DroneLabel() {
        super();
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        super.setForeground(new Color(r,g,b));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.err.println(evt.getPropertyName());
        switch(evt.getPropertyName()) {
            case Drone.PROP_LOC_PROPERTY:
            {
                Position p = (Position) evt.getNewValue();
                this.setLocation((int) p.getX(), (int) p.getY());
                this.setText(">" + buildText() + "<");
                break;
            }
            
            case Drone.PROP_FLY_PROPERTY:
            {
                if((Boolean) evt.getNewValue())
                    this.setText(">" + buildText() + "<");
                else
                    this.setText("<" + buildText() + ">");
                break;
            }
            
            default:
                System.err.println(evt.getPropertyName() + "not suppoted yet!");
        }
    } 
    
    private String buildText() {
        return this.getX() + "," + this.getY();
    }
}
