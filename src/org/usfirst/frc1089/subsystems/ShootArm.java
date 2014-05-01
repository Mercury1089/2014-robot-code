package org.usfirst.frc1089.subsystems;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc1089.lib.Ports;
import org.usfirst.frc1089.lib.Utils;

public class ShootArm {
    public final static int SHOOT = 0,
                            PASS = 1,
                            TRUSS = 2,
                            SUPER_TRUSS = 3,
                            DUNK = 4,
                            HUMAN_PLAYER = 5;
    
    private static final int REST = 0,
                             BACK = 1,
                             FORE = 2,
                             WAIT = 3,
                             RESET = 4;
    
    private double forward_speed, backward_speed, forward_swing, backward_swing;
    
    private Talon arm;
    private Encoder launcherEncoder;
    private DigitalInput armHome;
    private Counter armCounter;
    
    private boolean manual;
    private int state;
    
    public ShootArm() {
        arm = new Talon(Ports.SHOOTER_LAUNCHER);
        arm.set(0);
        launcherEncoder = new Encoder(Ports.SHOOTER_ENCODER_1, Ports.SHOOTER_ENCODER_2, false, Encoder.EncodingType.k4X);
        launcherEncoder.start();
        launcherEncoder.setDistancePerPulse(0.96);
        launcherEncoder.setMinRate(1);

        armHome = new DigitalInput(Ports.SHOOTER_HOME);
        armCounter = new Counter(armHome);
        armCounter.start();
        state = 0;
    }
    
    public void setManual(boolean m) {
        manual = m;
    }
    
    public void setManual(double p) {
        if(manual) {
            arm.set(p * .6);
        }
    }
    
    public boolean isManual() {
        return manual;
    }

    public void shoot(int type, double backswing) {
        switch(type) {
            case SHOOT:
                shoot(.7, .4, 100, backswing + (debugs && enableOffset ? debugBackswing : 0));
                break;
            case PASS:
                shoot(.7, .4, 50, 0);
                break;
            case HUMAN_PLAYER:
                shoot(.80, .4, 75, 0);
                break;
            case TRUSS:
                shoot(1, .4, 100, 20);
                break;
            case SUPER_TRUSS:
                shoot(1, .4, 100, 100);
                break;
            case DUNK:
                shoot(.82, .4, 130, 0);
                break;
        }
    }

    private void shoot(double forespeed, double backspeed, double foreswing, double backswing) {   
        manual = false;
        if(debugs) {
            if(enableForeswingSpeed) {
                forespeed = debugForeswingSpeed;
            }
            if(enableBackswingSpeed) {
                backspeed = debugBackswingSpeed;
            }
            if(enableBackswing && !enableOffset) {
                backswing = debugBackswing;
            }
        }
        this.forward_speed = forespeed;
        this.backward_speed = backspeed;
        this.forward_swing = foreswing;
        this.backward_swing = backswing;
        if(backswing == 0) {
	    state = FORE;
	} else {
	    state = BACK;
	}
    }

    public boolean isShooting() {
        return state != REST;
    }
    
    public void abort() {
        state = RESET;
    }
    
    private long waitTime;
    public void runArm() {
        if (armCounter.get() > 0) {
            armCounter.reset();
            launcherEncoder.reset();
        }
        
        updateDebugParams();
        
        switch(state) {
            case REST:
                break;
            case BACK:
                if (launcherEncoder.get() > -(backward_swing)) {
                    arm.set(backward_speed);
                } else {
                    arm.set(.15);
                    state = WAIT;
                    waitTime = System.currentTimeMillis();
                }
                break;
            case WAIT:
                if(System.currentTimeMillis() - waitTime > 100) {
                    state = FORE;
                }
                break;
            case FORE:
                if (launcherEncoder.get() < forward_swing) {
                    arm.set(-forward_speed);
                } else {
                    arm.set(0);
                    state = RESET;
                }
                break;
            case RESET:
                arm.set(0);
                state = REST;
                break;
        }
    }
    
    private boolean debugs,
                    enableBackswing,
                    enableOffset,
                    enableBackswingSpeed,
                    enableForeswingSpeed;
    private double debugBackswing,
                   debugBackswingSpeed,
                   debugForeswingSpeed;
    public void updateDebugParams() {
        debugs = SmartDashboard.getBoolean("enable debugs", false);
        enableOffset = SmartDashboard.getBoolean("offset backswing", false);
        enableBackswing = SmartDashboard.getBoolean("enable backswing", false);
        debugBackswing = SmartDashboard.getNumber("backswing widget", 115);
        enableBackswingSpeed = SmartDashboard.getBoolean("enable backswing speed", false);
        debugBackswingSpeed = SmartDashboard.getNumber("backswing speed", .4);
        enableForeswingSpeed = SmartDashboard.getBoolean("enable foreswing speed", false);
        debugForeswingSpeed = SmartDashboard.getNumber("foreswing speed", .7);
    }

    public void debug() {
        SmartDashboard.putNumber("launcher speed", arm.get());
        SmartDashboard.putNumber("launcher encoder count", launcherEncoder.get());
        SmartDashboard.putNumber("launcher encoder distance", launcherEncoder.getDistance());
        SmartDashboard.putBoolean("launcher home", armHome.get());
        SmartDashboard.putNumber("backswing bot", backward_swing);
        SmartDashboard.putNumber("backswing cam", Utils.distanceToBS(SmartDashboard.getNumber("distance", -1)));
        SmartDashboard.putBoolean("manual", manual);
    }
}