/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ap.dronemassa;

import java.beans.*;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Jacopo
 */
public class Drone implements Serializable {
    
    public static final String PROP_LOC_PROPERTY = "location";
    public static final String PROP_FLY_PROPERTY = "flying";
    
    private Position locProperty;
    private Boolean flyProperty;
    private Timer timer;
    
    private final PropertyChangeSupport propertySupport;
    
    public Drone() {
        propertySupport = new PropertyChangeSupport(this);
        this.locProperty = new Position(0, 0);
        this.flyProperty = Boolean.FALSE;
    }
    
    public Position getLocProperty() {
        return this.locProperty;
    }
    
    public void setLocProperty(Position newPoint) {
        Position oldValue = (Position) locProperty.clone();
        this.locProperty.setLocation(newPoint.getX(), newPoint.getY());
        propertySupport.firePropertyChange(PROP_LOC_PROPERTY, oldValue, this.locProperty);
    }
    
    public Boolean getFlyProperty() {
        return this.flyProperty;
    }
    
    public void setFlyProperty(Boolean newFly) {
        Boolean oldValue = flyProperty;
        this.flyProperty = newFly;
        propertySupport.firePropertyChange(PROP_FLY_PROPERTY, oldValue, this.flyProperty);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public void takeOff(Position initLoc) {
        
        setFlyProperty(Boolean.TRUE);
        setLocProperty(initLoc);
        timer = new Timer();
        timer.schedule( new CustomTimerTask(this), 0, 600);
    }
    
    public void land() {
        setFlyProperty(Boolean.FALSE);
        this.timer.cancel();
    }
}

class CustomTimerTask extends TimerTask {
    private final Drone drone;

    public CustomTimerTask(Drone drone) {
        super();
        this.drone = drone;
    }
    
    @Override
    public void run() {
        Position delta = Position.generateRandomDelta();
        Position curr = drone.getLocProperty();
        Position boundedPoint = new Position(
                Position.boundCoord((int) (delta.getX() + curr.getX()), 0, DroneGUI.PNL_WIDTH), 
                Position.boundCoord((int) (delta.getY() + curr.getY()), 0, DroneGUI.PNL_HEIGHT));
        
        drone.setLocProperty(boundedPoint);
    }
    
    
}
