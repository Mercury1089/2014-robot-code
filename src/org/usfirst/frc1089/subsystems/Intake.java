package org.usfirst.frc1089.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc1089.lib.Ports;

public class Intake {
    
    private Victor intakeRoller;
    private DoubleSolenoid extender;
    private long extenderDown;
    
    public Intake() {        
        intakeRoller = new Victor(Ports.LOADER_INTAKE_ROLLER);
        intakeRoller.set(0);
        extender = new DoubleSolenoid(Ports.LOADER_EXTENDER_1, Ports.LOADER_EXTENDER_2);
    }
    
    public void debug() {
        SmartDashboard.putBoolean("loader extended", isExtended());
        SmartDashboard.putBoolean("intake on", isRollerOn());
    }
    
    public void setExtended(boolean out) {
        extender.set(out ? DoubleSolenoid.Value.kReverse : DoubleSolenoid.Value.kForward);
        //setRoller(out);
        extenderDown = System.currentTimeMillis();
    }
    
    public void setRoller(double val) {
        intakeRoller.set(val);
    }
    
    public boolean isRollerOn() {
        return intakeRoller.get() != 0;
    }
    
    public boolean isExtended() {
        return extender.get() == DoubleSolenoid.Value.kReverse &&
               System.currentTimeMillis() - extenderDown > 1500;
    }
}