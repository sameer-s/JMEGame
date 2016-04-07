package mygame.scene.character;

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
    public RotationLockedChaseCamera(Camera cam, Spatial target, InputManager man)
    {
        super(cam, target, man);

        setDragToRotate(false);
        setDefaultDistance(7);
        setRotationSpeed(2);
        setMinVerticalRotation(-(7 * FastMath.PI) / 16);

    }

    @Override
    protected void zoomCamera(float value)
    {
    }

    @Override
    protected void updateCamera(float tpf)
    {
        super.updateCamera(tpf);

        if(target != null)
        {
            target.setLocalRotation(this.cam.getRotation());
        }

//        cam.setLocation(cam.getLocation().add(0, 3, 0));
    }
}
