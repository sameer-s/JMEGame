package mygame.character;

import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
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
            target.getControl(ThirdPersonCharacterControl.class).setPhysicsRotation(this.cam.getRotation());
        }
    }
}
