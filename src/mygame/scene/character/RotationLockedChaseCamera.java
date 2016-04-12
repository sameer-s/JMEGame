package mygame.scene.character;

import com.jme3.input.CameraInput;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

/**
 *
 * @author Sameer Suri
 */
public class RotationLockedChaseCamera extends ChaseCamera
{
    public String[] movementActions = new String[] {
        CameraInput.CHASECAM_MOVELEFT,
        CameraInput.CHASECAM_MOVERIGHT,
        CameraInput.CHASECAM_UP,
        CameraInput.CHASECAM_DOWN,
        CameraInput.CHASECAM_ZOOMIN,
        CameraInput.CHASECAM_ZOOMOUT
    };

    public RotationLockedChaseCamera(Camera cam, Spatial target, InputManager man)
    {
        super(cam, target, man);

        this.setDragToRotate(false);
        this.setDefaultDistance(7);
        this.setRotationSpeed(2);
        this.setMinVerticalRotation(-(7 * FastMath.PI) / 16);
        this.setChasingSensitivity(0.1f);
        this.setMinDistance(10f);
    }

    @Override
    public void onAnalog(String name, float value, float tpf)
    {
        if (name.equals(movementActions[0]))
        {
            rotateCamera(-value);
        }
        else if (name.equals(movementActions[1]))
        {
            rotateCamera(value);
        }
        else if (name.equals(movementActions[2]))
        {
            vRotateCamera(value);
        }
        else if (name.equals(movementActions[3]))
        {
            vRotateCamera(-value);
        }
        else if (name.equals(movementActions[4]))
        {
            zoomCamera(-value);
            if (zoomin == false)
            {
                distanceLerpFactor = 0;
            }
            zoomin = true;
        }
        else if (name.equals(movementActions[5]))
        {
            zoomCamera(+value);
            if (zoomin == true)
            {
                distanceLerpFactor = 0;
            }
            zoomin = false;
        }
    }

    @Override
    protected void updateCamera(float tpf)
    {
        super.updateCamera(tpf);

        if(target != null)
        {
//            target.setLocalRotation(new Quaternion().slerp(target.getLocalRotation(), this.cam.getRotation(), .01f));
            target.setLocalRotation(this.cam.getRotation());
        }
    }
}
