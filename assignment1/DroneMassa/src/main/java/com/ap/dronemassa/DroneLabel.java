package com.ap.dronemassa;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;

/**
 * Extended JLabel, that also can manage listened properties of a Drone.
 */
public class DroneLabel extends JLabel implements PropertyChangeListener {    
    
    // initialized with a random foreground color
    public DroneLabel() {
        super();
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        super.setForeground(new Color(r,g,b));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch(evt.getPropertyName()) {
            /* if a 'loc change' is catched, also the
             * position of the label is changed
            */
            case Drone.PROP_LOC_PROPERTY:
            {
                Position p = (Position) evt.getNewValue();
                this.setLocation((int) p.getX(), (int) p.getY());
                this.setText(">" + buildText() + "<");
                break;
            }
            
            /* if a 'fly change' is catched, also the
             * text of the label is changed, to simulate the stop/start of a drone.
            */
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
