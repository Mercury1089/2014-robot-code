package org.usfirst.frc1089.subsystems;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc1089.lib.Ports;
import org.usfirst.frc1089.lib.Utils;

public class Tusks {
    private final static int UP = 1,
                             DOWN = 2,
                             STABLE = 0,
                             START = 3;
    
    public final static double HOME = 4.4,
                               SHOOT = 3.77,
                               DUNK = 2.0,
                               TRUSS = 3.65,
                               SUPER_TRUSS = 3.77;
    
    private DoubleSolenoid brake;
    private AnalogChannel anglePot;
    private Talon angler;
    
    private double goal;
    
    private int state;
    
    public Tusks(){
        brake = new DoubleSolenoid(Ports.SHOOTER_BRAKE_1, Ports.SHOOTER_BRAKE_2);
        anglePot = new AnalogChannel(Ports.SHOOTER_ANGLE_POT);
        angler = new Talon(Ports.SHOOTER_ANGLE_CONTROL);
        state = STABLE;
    }
    
    public boolean isHome() {
        return anglePot.getVoltage() > HOME - .1;
    }
    
    public void setExtension(double goal){
        this.goal = goal;
        state = START;
    }
    
    public boolean isHighEnough() {
        return anglePot.getVoltage() < 3.85;
    }
    
    public boolean isMoving(){
        return brake.get() == DoubleSolenoid.Value.kReverse;
    }
    
    public boolean isAtGoal(){
        return (!this.isMoving() && Utils.inRange(anglePot.getVoltage(), goal - .05, goal + .05)) || (isHome() && goal == HOME);
    }
    
    public double getRawExtension(){
        return anglePot.getVoltage();
    }
    
    public double getGoalExtension(){
        return goal;
    }
    
    public void runTusks(){
        switch(state){
            case UP:
                angler.set((Math.abs(anglePot.getVoltage() - goal) > 1) ? .3 : .23);
                if(anglePot.getVoltage() < goal){
                    state = STABLE;
                    brake.set(DoubleSolenoid.Value.kForward);
                    angler.set(0.0);
                }
                break;
            case DOWN:
                angler.set(0.0);
                if(anglePot.getVoltage() > goal - .03){
                    state = STABLE;
                    brake.set(DoubleSolenoid.Value.kForward);
                }
                break;
            case STABLE:
                break;
            case START:
                if(!this.isAtGoal()){
                    brake.set(DoubleSolenoid.Value.kReverse);
                    state = goal < anglePot.getVoltage() ? UP : DOWN;
                } else {
                    state = STABLE;
                }
                break;
        }
    }

    public void debug() {
        SmartDashboard.putNumber("tusk goal", goal);
        SmartDashboard.putNumber("angler speed", angler.get());
        SmartDashboard.putNumber("angle pot", anglePot.getVoltage());
        SmartDashboard.putBoolean("brake engaged", brake.get() == DoubleSolenoid.Value.kForward);
        SmartDashboard.putBoolean("tusks home", isHome());
    }
}