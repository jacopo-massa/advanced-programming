package com.ap.dronemassa;

import java.beans.*;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;


public class Drone implements Serializable {
    
    // string with names of drone's properties
    public static final String PROP_LOC_PROPERTY = "location";
    public static final String PROP_FLY_PROPERTY = "flying";
    
    // frequency of drone's movements
    private final int DELTA = 400;
    
    private final Position loc;
    private Boolean fly;
    private Timer timer;
    
    private final PropertyChangeSupport propertySupport;
    
    public Drone() {
        propertySupport = new PropertyChangeSupport(this);
        this.loc = new Position(0, 0);
        this.fly = Boolean.FALSE;
    }
    
    
    public Position getLoc() {
        return this.loc;
    }
    
    public void setLoc(Position newPoint) {
        Position oldValue = (Position) loc.clone();
        this.loc.setLocation(newPoint.getX(), newPoint.getY());
        propertySupport.firePropertyChange(PROP_LOC_PROPERTY, oldValue, this.loc);
    }
    
    public Boolean getFly() {
        return this.fly;
    }
    
    public void setFly(Boolean newFly) {
        Boolean oldValue = fly;
        this.fly = newFly;
        propertySupport.firePropertyChange(PROP_FLY_PROPERTY, oldValue, this.fly);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    /**
     * takeOff: starts a drone, by running a new TimerTask
     * @param initLoc position to assign to the 'loc' property
     */
    public void takeOff(Position initLoc) {
        
        setFly(Boolean.TRUE);
        setLoc(initLoc);
        timer = new Timer();
        timer.schedule( new CustomTimerTask(this), 0, DELTA);
    }
    
    /**
     * land: stops a drone, ending also the TimerTask started in 'takeOff' function
     */
    public void land() {
        setFly(Boolean.FALSE);
        this.timer.cancel();
    }
}

/**
 * Custom class extending a TimerTask, in which we have also a reference to the
 * Drone we are going to move.
 */
class CustomTimerTask extends TimerTask {
    private final Drone drone;

    public CustomTimerTask(Drone drone) {
        super();
        this.drone = drone;
    }
    
    @Override
    public void run() {
        
        /* 
         * create a new random position, bounded within the dimensions
         * of the panel containing all the drones (Dimensions are specified into 'DroneGui', as static variables
         */
        Position delta = Position.generateRandomDelta();
        Position curr = drone.getLoc();
        Position boundedPoint = new Position(
                Position.boundCoord((int) (delta.getX() + curr.getX()), 0, DroneGUI.PNL_WIDTH), 
                Position.boundCoord((int) (delta.getY() + curr.getY()), 0, DroneGUI.PNL_HEIGHT));
        
        
        drone.setLoc(boundedPoint);
    }
}
