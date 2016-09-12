
package org.usfirst.frc.team5854.robot;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.*;



public class Robot extends SampleRobot {
    RobotDrive myDrive;
    Joystick xboxController;
    VictorSP frontLeft, frontRight, rearLeft, rearRight, launchAngle, climbMotor, climbLock;
    TalonSRX launchLeft, launchRight;
	Servo launchLoader;
	Joystick driverControl, shooterControl;
	AnalogInput pot; // obj for the potentiometer
	DigitalInput tLimit, bLimit, decswitch0, decswitch1;
	ADXRS450_Gyro gyro;
	Latch revlatch;
	Timer robotimer;
	Latch ramplatch;
    
    
	double currleft = 0.0, currright = 0.0;
	final double SPEED_INC = 0.02;
	boolean revbutton = false, revstate = false; // bools associated with josh's ridiculous  needs
	boolean rampbutton = false, rampstate = false;
	boolean driveStraight = false, automode = false;
	boolean usesock = false;
	final double TURN_SCALING = 0.03f;
	final double MIN_SHOOTER_ANGLE = -50.00; // minimum angle the shooter can go down
	final double MAX_SHOOTER_ANGLE = 175.00; // maximum angle the shooter can go up
	//char joybuff[sizeof(int)];
	int conn_desc;
	

    public Robot() {
    	
    	launchAngle = new VictorSP(8); 
    	climbMotor = new VictorSP(6);
    	climbLock = new VictorSP(7); 
    	launchLeft = new TalonSRX(0);
		launchRight = new TalonSRX(5);
		launchLoader = new Servo(9); 
		driverControl = new Joystick(0); 
		shooterControl = new Joystick(1);
		pot = new AnalogInput(0);
		tLimit = new DigitalInput(2); 
		bLimit = new DigitalInput(3); 
		decswitch0 = new DigitalInput(0);
		decswitch1 = new DigitalInput(1); 
		gyro = new ADXRS450_Gyro();
		revlatch = new Latch(); 
		robotimer = new Timer();
		ramplatch = new Latch();
		
		
    	frontLeft = new VictorSP(1); // drive motor
		frontRight = new VictorSP(2); // drive motor
		rearLeft = new VictorSP(3); // drive motor
		rearRight = new VictorSP(4); // drive motor
		myDrive = new RobotDrive(frontLeft, frontRight, rearLeft, rearRight); // declare the robot drive
		myDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, false);
		myDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, false);
		myDrive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, false);
		myDrive.setInvertedMotor(RobotDrive.MotorType.kRearRight, false);
		myDrive.setExpiration(0.1);
    }
    
    public void robotInit() {
    	SmartDashboard.putString("Robot Messages", "Entering RobotInit()");
		gyro.reset();
		SmartDashboard.putString("Robot Messages", "Gyro Reset");
    }

    double ShooterAngle()	//calculates the angle of the shooter.
	{
		double y = (pot.getAverageVoltage() - 0.94) / 0.021;
		y -= 9.995 - 7.954;
		y += 4.078;
		System.out.print(y); 
		return y;
	}

	void BallControl(double joy, boolean shoot, boolean suck, boolean up, boolean down, boolean pshoot) //controlls what happens to the ball.
	{
		if (shoot)
			launchLoader.setAngle(0);
		else
			launchLoader.setAngle(50);

		if (pshoot) {
			launchLeft.set(-1.0);
			launchRight.set(1.0); 
		} else if (suck) {
			launchLeft.set(0.4);
			launchRight.set(-0.4);
		} else {
			launchLeft.set(0.0);
			launchRight.set(0.0);
		}

		if (down) {
			if (ShooterAngle() >= MIN_SHOOTER_ANGLE)
				launchAngle.set(-1.0);
			else
				launchAngle.set(0.0);
		}
		else if (up) {
			if (ShooterAngle() <= MAX_SHOOTER_ANGLE)
				launchAngle.set(1.0);
			else
				launchAngle.set(0.0);
		}
		else
			launchAngle.set(0.0);

		joy *= 160;
		joy += 160;
		joy += 0.5;
		SmartDashboard.putNumber("Pixel Cord", (int)joy);

		double turnangle = 155.0 - (int)joy;
		turnangle *= 0.166;
		SmartDashboard.putNumber("Angle To Turn", (int)turnangle);
		//System.out.printf(joybuff, sizeof(joybuff), "%d", 0);
		//send(conn_desc, joybuff, sizeof(joybuff), 0);
	}

	void ClimbControl(boolean down, boolean up) //controlls the climbing system.
	{
		if (down) {
			if (!bLimit.get()) {
				climbMotor.set(0.0);
				climbLock.set(0.0);
			} else {
				climbLock.set(0.0);
				climbMotor.set(-1.0);
			}
		} else if (up) {
			if (!tLimit.get()) {
				climbLock.set(0.0);
				climbMotor.set(0.0);
			} else {
				climbLock.set(1.0);
				climbMotor.set(1.0);
			}
		} else {
			climbLock.set(0.0);
			climbMotor.set(0.0);
		}
	}

	void RampSpeed(double leftjoy, double rightjoy, double l, double r)	//code for ramp spped.
	{
		if (leftjoy > l)
			l += SPEED_INC;
		else if (leftjoy < l)
			l -= SPEED_INC;

		if (rightjoy > r)
			r += SPEED_INC;
		else if (rightjoy < r)
			r -= SPEED_INC;
	}

	void PublishDash()	//publishes all values to the smartdashboard.
	{
		SmartDashboard.putNumber("Gyro Angle", gyro.getAngle());
		SmartDashboard.putNumber("Pot Voltage", pot.getAverageVoltage());
		SmartDashboard.putNumber("Shooter Angle", (double)ShooterAngle());
		SmartDashboard.putBoolean("Reversed", revstate);
		SmartDashboard.putBoolean("Ramp", rampstate);
	}
	
	
	
    public void autonomous() {
    	
    	SmartDashboard.putString("Robot Messages", "AutoInit()");
		robotimer.reset(); //resets the robot's timer. 
		robotimer.start();	// starts the robot timer.
		gyro.reset();	//resets the gyro
		
		SmartDashboard.putString("Robot Messages", "Entering AutoPeridoic()");
		PublishDash();
		double gyroangle = gyro.getAngle();	//sets variable gyroangle to the angle of the Gyro.
		if (decswitch0.get()) {
			if (robotimer.get() < 6.0) {	//for "6.0" seconds drive forward.
				RampSpeed(0.0, 0.6, currleft, currright);
				myDrive.drive(currright, -gyroangle * TURN_SCALING);
				BallControl(0.0, false, false, false, false, false);
			}
			else {	//after time has elapsed stop moving forward.
				myDrive.drive(0.0, 0.0);
			}
		}
		else {	//if the switch is in position 2 then do auto shot.
			myDrive.drive(0.0, 0.0);	//do not drive forward.
			if (ShooterAngle() > 55)	//set the shooter angle to 55 degrees.
				BallControl(0.0, false, false, false, true, false);
			else	//once shooter equals 55 degrees, shoot the ball.
				BallControl(0.0, false, false, false, false, true);
				if (robotimer.get() > 7.0) 	//after 7 seconds from start of auto. the ball will shoot.
					BallControl(0.0, true, false, false, false, true);
		}
    }

    /**
     * Runs the motors with arcade steering.
     */
    public void operatorControl() {
    	robotimer.reset();	//resets timer.
		gyro.reset();		//resets gyro.
		
		PublishDash();

		// Below is code for the drive system
		RampSpeed(driverControl.getRawAxis(1), driverControl.getRawAxis(3), currleft, currright);
		if (driverControl.getRawButton(5)) {	//if button pressed for drive straight.
			gyro.reset();
			driveStraight = true; //sets drive straight to true.
			while (driveStraight) {	//if drive straight is true. drive straight.
				double gyroangle = gyro.getAngle();
				double speed = 0.8;
				if (driverControl.getRawButton(6))
					speed = 1.0;
				else
					speed = 0.8;
				speed *= driverControl.getRawAxis(3);
				myDrive.drive(-speed, -gyroangle * TURN_SCALING);

				// non driving code
				ClimbControl(driverControl.getRawButton(7), driverControl.getRawButton(8));
				BallControl(shooterControl.getRawAxis(0), shooterControl.getRawButton(1), shooterControl.getRawButton(3),
						shooterControl.getRawButton(6), shooterControl.getRawButton(5),
						shooterControl.getRawButton(2));
				driveStraight = driverControl.getRawButton(5); // keep this at the end
			}
		} else {	//if button is not pressed.
			driveStraight = false; //set drivestraight to false.
			revlatch.Toggle(driverControl.getRawButton(1), revbutton, revstate);
			ramplatch.Toggle(driverControl.getRawButton(4), rampbutton, rampstate);
			if (rampstate) {
				if (revstate)
					myDrive.tankDrive(currright, currleft);
				else
					myDrive.tankDrive(-currleft, -currright);
			} else {
				double speed = 0.8;
				if (driverControl.getRawButton(6))
					speed = 1.0;
				else
					speed = 0.8;
				if (revstate)
					myDrive.tankDrive(driverControl.getRawAxis(3) * speed, driverControl.getRawAxis(1) * speed);
				else
					myDrive.tankDrive(-driverControl.getRawAxis(1) * speed, -driverControl.getRawAxis(3) * speed);
			}


		}
		// End of code for the drive system
		ClimbControl(driverControl.getRawButton(7), driverControl.getRawButton(8));
		BallControl(shooterControl.getRawAxis(0), shooterControl.getRawButton(1), shooterControl.getRawButton(3),
				shooterControl.getRawButton(6), shooterControl.getRawButton(5),
				shooterControl.getRawButton(2));
		// A little code for the assist aim system

    }

    /**
     * Runs during test mode
     */
    public void test() {
    }
}
