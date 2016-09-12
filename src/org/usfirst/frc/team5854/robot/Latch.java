/**
package org.usfirst.frc.team5854.robot;

public class Latch {
	static Latch(){
		Latch_Class.Toggle(b, bp, tb);Toggle(bool b, bool &bp, bool &tb);
	}
}
*/

package org.usfirst.frc.team5854.robot;

public class Latch {

	public void Toggle(boolean b, boolean bp, boolean tb)
	{
		if (b && !bp) {
			bp = true;
			if(tb) {
				tb = false;
			}
			else {
				tb = true;
			}
		} else if (!b)
			bp = false;
	}
}