package org.usfirst.frc1089.lib;

public class Ports {
    public static final int JOYSTICK_LEFT = 1,              //USB
                            JOYSTICK_RIGHT = 2,             //USB
                            JOYSTICK_GAMEPAD = 3;           //USB

    public static final int DRIVE_LEFT = 10,                //PWM
                            DRIVE_RIGHT = 1,                //PWM
                            DRIVE_LEFT_ENCODER_1 = 3,       //DIO
                            DRIVE_LEFT_ENCODER_2 = 4,       //DIO
                            DRIVE_RIGHT_ENCODER_1 = 5,      //DIO
                            DRIVE_RIGHT_ENCODER_2 = 6;      //DIO
    
    public static final int SHOOTER_LAUNCHER = 6,           //PWM
                            SHOOTER_ENCODER_1 = 1,          //DIO
                            SHOOTER_ENCODER_2 = 2,          //DIO
                            SHOOTER_HOME = 7,               //DIO
                            SHOOTER_ANGLE_POT = 3,          //AI
                            SHOOTER_ANGLE_CONTROL = 5,      //PWM
                            SHOOTER_BRAKE_1 = 3,            //Sol
                            SHOOTER_BRAKE_2 = 4;            //Sol
    
    public static final int COMPRESSOR_PRESSURE_SWITCH = 8, //DIO
                            COMPRESSOR_RELAY = 1;           //Rel
    
    public static final int LOADER_INTAKE_ROLLER = 7,       //PWM
                            LOADER_EXTENDER_1 = 1,          //Sol
                            LOADER_EXTENDER_2 = 2;          //Sol
}