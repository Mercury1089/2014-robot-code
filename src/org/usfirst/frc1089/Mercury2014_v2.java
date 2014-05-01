package org.usfirst.frc1089;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.fpga.tGlobal;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc1089.lib.Ports;
import org.usfirst.frc1089.lib.Utils;
import org.usfirst.frc1089.subsystems.Intake;
import org.usfirst.frc1089.subsystems.ShootArm;
import org.usfirst.frc1089.subsystems.Tusks;

public class Mercury2014_v2 extends IterativeRobot {
    //misc constants
    private final static double ACCEL_LIMIT = .25;
    
    //the dashboard given distance
    private double distance;
    
    //misc core things
    private Joystick rightStick, leftStick, gamepad;
    private boolean[] right_stick_prev, left_stick_prev, gamepad_prev;
    private DigitalInput tankIsFull;
    private Relay compressor;
    
    //drive system
    private RobotDrive drive;
    private Encoder leftDrive,
                    rightDrive;
    private double prevLeft,        //used to limit acceleration
                   prevRight;
    
    //subsystems
    private Tusks tusks;            //object to control tusks
    private ShootArm arm;           //object to control launching arm
    private Intake intake;          //object for intake arm and rollers

    private DriverStation ds;       //used to get match time in auton
    
    public void robotInit() {
        ds = DriverStation.getInstance();

        drive = new RobotDrive(Ports.DRIVE_LEFT, Ports.DRIVE_RIGHT);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);

        leftDrive = new Encoder(3, 4, true, Encoder.EncodingType.k4X);
        rightDrive = new Encoder(5, 6, false, Encoder.EncodingType.k4X);
        leftDrive.setDistancePerPulse(6 * Math.PI / 360);
        rightDrive.setDistancePerPulse(6 * Math.PI / 360);
        leftDrive.start();
        rightDrive.start();

        compressor = new Relay(Ports.COMPRESSOR_RELAY);
        tankIsFull = new DigitalInput(Ports.COMPRESSOR_PRESSURE_SWITCH);

        leftStick = new Joystick(Ports.JOYSTICK_LEFT);
        rightStick = new Joystick(Ports.JOYSTICK_RIGHT);
        gamepad = new Joystick(Ports.JOYSTICK_GAMEPAD);
        left_stick_prev = new boolean[13];
        right_stick_prev = new boolean[13];
        gamepad_prev = new boolean[13];

        arm = new ShootArm();
        intake = new Intake();
        tusks = new Tusks();
        
        new Thread() {
            public void run() {
                while(true) {
                    //run the right state machines
                    tusks.runTusks();
                    arm.runArm();

                    //output to smartdash
                    debug();
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException e) {}
                }
            }
        }.start();
    }
    
    private boolean hotSnapshot;
    private boolean twoBallAuton;
    public void autonomousInit() {
        intake.setExtended(true);
        leftDrive.reset();
        rightDrive.reset();
        SmartDashboard.putNumber("auton timer", 0);
        autonState = twoBallAuton ? MOVE_1 : INTAKE_DOWN_2;
    }
    
    private int autonState;
    private final static int MOVE_1 = 0, //auton states
                             HOT_1 = 1,
                             RAISE_1 = 2,
                             SHOOT_1 = 3,
                             INTAKE_DOWN_2 = 4,
                             DRIVE_2 = 5,
                             RAISE1_2 = 6,
                             SHOOT1_2 = 7,
                             LOWER1_2 = 8,
                             RAISE2_2 = 9,
                             SHOOT2_2 = 10,
                             DONE = 11;
    public void autonomousPeriodic() {
        switch(autonState){
            case MOVE_1:
                if((leftDrive.getDistance() < 72 && rightDrive.getDistance() < 72)) {
                    drive.drive(-.6, 0);
                }
                else{
                    drive.drive(0,0);
                    autonState = HOT_1;
                }
                break;
            case HOT_1:
                Timer.delay(.75);
                hotSnapshot = SmartDashboard.getBoolean("hot", false);
                tusks.setExtension(Tusks.SHOOT + .03);
                autonState = RAISE_1;
                Timer.delay(0.5);
            case RAISE_1:
                if(tusks.isAtGoal()){
                    autonState = SHOOT_1;
                }
                break;
            case SHOOT_1:
                double timeLeft = ds.getMatchTime();
                if (hotSnapshot || timeLeft >= 6) {
                    arm.shoot(ShootArm.SHOOT, -5 + 107);
                    autonState = DONE;
                }
                break;
                //start 2 ball auton
            case INTAKE_DOWN_2:
                if(intake.isExtended()){
                    intake.setRoller(.25);
                    drive.drive(-.6, 0);
                    autonState = DRIVE_2;
                }
                break;
            case DRIVE_2:
                if((leftDrive.getDistance() >= 72 || rightDrive.getDistance() >= 72)) {
                    drive.drive(0, 0);
                    tusks.setExtension(Tusks.SHOOT);
                    intake.setRoller(.5);
                    Timer.delay(.25);
                    intake.setRoller(0);
                    autonState = RAISE1_2;
                }
                break;
            case RAISE1_2:
                if(tusks.isAtGoal()) {
                    arm.shoot(ShootArm.SHOOT, -5 + 107);
                    autonState = SHOOT1_2;
                }
                break;
            case SHOOT1_2:
                if(!arm.isShooting()){
                    tusks.setExtension(Tusks.HOME);
                    autonState = LOWER1_2;
                }
                break;
            case LOWER1_2:
                if(tusks.isHome()){
                    intake.setRoller(-1);
                    Timer.delay(2);
                    intake.setRoller(0);
                    tusks.setExtension(Tusks.SHOOT);
                    autonState = RAISE2_2;
                }
                break;
            case RAISE2_2:
                if(tusks.isAtGoal()) {
                    arm.shoot(ShootArm.SHOOT, -5 + 107);
                    autonState = SHOOT2_2;
                }
                break;
            case SHOOT2_2:
                if(!arm.isShooting()){
                    tusks.setExtension(Tusks.HOME);
                    autonState = DONE;
                }
                break;
            case DONE:
                break;
        }
    }   
    
    public void teleopInit() {
        arm.abort();
        tusks.setExtension(tusks.getRawExtension());
        intake.setRoller(0);
    }
    
    private long teleopTimer;
    public void teleopPeriodic() {
        teleopTimer = System.currentTimeMillis(); //set the start time
        
        if(intake.isExtended()) { //limit the acceleration, depending if the intake is up
            drive.tankDrive(getJoystickVal(leftStick, true, ACCEL_LIMIT), getJoystickVal(rightStick, false, ACCEL_LIMIT));
        } else {
            drive.tankDrive(getJoystickVal(leftStick, true, ACCEL_LIMIT / 2), getJoystickVal(rightStick, false, ACCEL_LIMIT / 2));
        }
        //get distance if possible
        if(tusks.isHome()) {
            distance = SmartDashboard.getNumber("distance", -1);
        }
        
        //set the compressor on if necessary
        compressor.set(tankIsFull.get() ? Relay.Value.kOff : Relay.Value.kForward);
        
        
        //buttons
        //left drive stick trigger
        if(leftStick.getRawButton(1)) {
            if (!left_stick_prev[1]) {
                if(tusks.isHome()) {
                    intake.setExtended(true);
                    intake.setRoller(-1);
                    left_stick_prev[1] = true;
                } else {
                    //shooter.resetAngler();
                }
            }
        } else {
            left_stick_prev[1] = false;
        }
        //left drive stick top right button
        if (leftStick.getRawButton(5)) {
            if (!left_stick_prev[5]) {
                if(tusks.isHome()) {
                    intake.setRoller(!intake.isRollerOn() ? -1 : 0);
                    left_stick_prev[5] = true;
                } else {
                    //shooter.resetAngler();
                }
            }
        } else {
            left_stick_prev[5] = false;
        }
        //right drive stick trigger
        if (rightStick.getRawButton(1)) {
            if (!right_stick_prev[1]) {
                if(tusks.isHome()) {
                    intake.setExtended(false);
                    intake.setRoller(0);
                    right_stick_prev[1] = true;
                } else {
                    //shooter.resetAngler();
                }
            }
        } else {
            right_stick_prev[1] = false;
        }
        //gamepad X
        if(gamepad.getRawButton(1)){
            if(!gamepad_prev[1]){
                if(intake.isExtended()) {
                    gamepad_prev[1] = true;
                    if(!arm.isShooting()) {
                        tusks.setExtension(Tusks.SHOOT);
                    }
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        }else{
            gamepad_prev[1] = false;
        }
        //gamepad A
        if(gamepad.getRawButton(2)){
            if(!gamepad_prev[2]){
                if(intake.isExtended()) {
                    if(!arm.isShooting()){
                        tusks.setExtension(Tusks.HOME);
                    }
                    gamepad_prev[2] = true;
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        }else{
            gamepad_prev[2] = false;
        }
        //gamepad B
        if(gamepad.getRawButton(3)){
            if(!gamepad_prev[3]){
                if(intake.isExtended()) {
                    if(!arm.isShooting()) {
                        gamepad_prev[3] = true;
                        tusks.setExtension(Tusks.SHOOT);
                        arm.shoot(ShootArm.HUMAN_PLAYER, 0);
                    }
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        }else{
            gamepad_prev[3] = false;
        }
        //gamepad Y
        if(gamepad.getRawButton(4)){
            if(!gamepad_prev[4]){
                if(intake.isExtended()) {
                    if(!arm.isShooting()) {
                       tusks.setExtension(Tusks.DUNK);
                    }
                    gamepad_prev[4] = true;
                } else {
                     //loader.setExtender(true);
                     //loader.setRoller(false);
               }
            }
        }else{
            gamepad_prev[4] = false;
        }
        //gamepad left bumper boops the ball
        if(gamepad.getRawButton(5)){
            if(!gamepad_prev[5]){
                if(tusks.isHome()) {
                    gamepad_prev[5] = true;
                    if(!intake.isExtended() && !arm.isShooting()) {
                        arm.shoot(ShootArm.PASS, 0);
                    } else {
                        intake.setExtended(false);
                    }
                } else {
                    tusks.setExtension(Tusks.HOME);
                }
            }
        }else{
            gamepad_prev[5] = false;
        }
        //gamepad right bumper super boop
        if(gamepad.getRawButton(6)){
            if(!gamepad_prev[6]){
                if(intake.isExtended()) {
                    if(!arm.isShooting()) {
                        gamepad_prev[6] = true;
                        tusks.setExtension(Tusks.SHOOT);
                        arm.shoot(ShootArm.SHOOT, Utils.distanceToBS(distance));
                    }
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        }else{
            gamepad_prev[6] = false;
        }
        //gamepad left trigger field goal boop
        if(gamepad.getRawButton(7)){
            if(!gamepad_prev[7]){
                if(intake.isExtended()) {
                    if(tusks.isHighEnough() && !arm.isShooting()) {
                        gamepad_prev[7] = true;
                        arm.shoot(ShootArm.TRUSS, 0);
                    } else {
                        tusks.setExtension(Tusks.TRUSS);
                    }
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        }else{
            gamepad_prev[7] = false;
        }
        //gamepad right trigger super field goal boop
        if(gamepad.getRawButton(8)){
            if(!gamepad_prev[8]){
                if(intake.isExtended()) {
                    if(tusks.isHighEnough() && !arm.isShooting()) {
                        gamepad_prev[8] = true;
                        arm.shoot(ShootArm.SUPER_TRUSS, 0);
                    } else {
                        tusks.setExtension(Tusks.SUPER_TRUSS);// + .05);
                    }
                } else {
                    //loader.setExtender(true);
                    //loader.setRoller(false);
                }
            }
        } else {
            gamepad_prev[8] = false;
        }
        //gamepad start button
        if(gamepad.getRawButton(10)){
            if(!gamepad_prev[10]){
                if(intake.isExtended()) {
                    if(!arm.isShooting()) {
                        if(tusks.getRawExtension() > 3) {
                            tusks.setExtension(Tusks.DUNK);
                        } else {
                            arm.shoot(ShootArm.DUNK, 0);
                        }
                    }
                    gamepad_prev[10] = true;
                } else {
                     //loader.setExtender(true);
                     //loader.setRoller(false);
               }
            }
        }else{
            gamepad_prev[10] = false;
        }
        //gamepad left stick
        if(gamepad.getRawButton(11)){
            if(!gamepad_prev[11]){
                arm.setManual(!arm.isManual());
                gamepad_prev[11] = true;
            }
        }else{
            gamepad_prev[11] = false;
        }
        //gamepad left stick
        if(Math.abs(gamepad.getRawAxis(2)) > 0.2) {
            arm.setManual(gamepad.getRawAxis(2));
        } else{
            arm.setManual(0);
        }
        //gamepad right stick
        if(gamepad.getRawButton(12)){
            if(!gamepad_prev[12]){
                if(intake.isExtended()) {
                    arm.abort();
                    gamepad_prev[12] = true;
                } else {
                     //loader.setExtender(true);
                     //loader.setRoller(false);
               }
            }
        }else{
            gamepad_prev[12] = false;
        }
        
        SmartDashboard.putNumber("teleop time", System.currentTimeMillis() - teleopTimer);
    }

    public void disabledInit() {
        drive.drive(0, 0);
    }

    private long lastAutonSelect;
    public void disabledPeriodic() {
        if(leftStick.getRawButton(10) && System.currentTimeMillis() - lastAutonSelect > 500) {
            twoBallAuton = !twoBallAuton;
            lastAutonSelect = System.currentTimeMillis();
        }
    }
    
    public void debug() {
        tGlobal.writeFPGA_LED(System.currentTimeMillis() / 1000 % 2 == 0);
        SmartDashboard.putNumber("left encoder distance", leftDrive.getDistance());
        SmartDashboard.putNumber("right encoder distance", rightDrive.getDistance());
        SmartDashboard.putBoolean("2 balls", twoBallAuton);
        SmartDashboard.putBoolean("is at goal", tusks.isAtGoal());
        intake.debug();
        arm.debug();
        tusks.debug();
    }
    
    public double getJoystickVal(Joystick stick, boolean isLeft, double limit) {
        double prev = isLeft ? prevLeft : prevRight;
        double ret = Utils.coerce(stick.getY(), prev - limit, prev + limit);
        if(isLeft) {
            prevLeft = ret;
        } else {
            prevRight = ret;
        }
        return ret;
    }
}