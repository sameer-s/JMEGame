package mygame.game;

import com.jme3.input.CameraInput;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;

/**
 *
 * @author Sameer Suri
 */
public abstract class JoystickInit
{
    public static void init(InputManager inputManager)
    {
        // Gets a list of the current joysticks.
        Joystick[] joysticks = inputManager.getJoysticks();

        inputManager.setAxisDeadZone(0.15f);
        // If there are any joysticks...
        if(joysticks != null)
        {
            // ... for each joystick ...
            for(Joystick joystick : joysticks)
            {
                System.out.printf("Joystick found! name = %s, id=%d%n", joystick.getName(), joystick.getJoyId());

                String suffix = joystick.getJoyId() == 1 ? "" : "_Player2";

                // Since we use the same action name as the keyboard inputs registered by the engine, it handles the looking for us.
                joystick.getAxis(JoystickAxis.X_AXIS).assignAxis(CameraInput.CHASECAM_MOVERIGHT + suffix, CameraInput.CHASECAM_MOVELEFT + suffix);
                joystick.getAxis(JoystickAxis.Y_AXIS).assignAxis(CameraInput.CHASECAM_DOWN + suffix, CameraInput.CHASECAM_UP + suffix);
                joystick.getAxis(JoystickAxis.Z_AXIS).assignAxis(CameraInput.CHASECAM_MOVERIGHT + suffix, CameraInput.CHASECAM_MOVELEFT + suffix);
                joystick.getAxis(JoystickAxis.Z_ROTATION).assignAxis(CameraInput.CHASECAM_DOWN + suffix, CameraInput.CHASECAM_UP + suffix);

                joystick.getButton(JoystickButton.BUTTON_4).assignButton("Throttle-" + suffix);
                joystick.getButton(JoystickButton.BUTTON_5).assignButton("Throttle+" + suffix);
                joystick.getButton(JoystickButton.BUTTON_2).assignButton("Shoot" + suffix);
            }
        }
        else
        {
            System.out.println("No joysticks found");
        }
    }
}
