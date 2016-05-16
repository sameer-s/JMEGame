package mygame.scene.character;

import com.jme3.input.CameraInput;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author Sameer
 */
public class RollRotationFlyCamera extends FlyByCamera
{
    public static final String ROTATE_LEFT  = "RollRotationFlyCameraRotateLeft",
                               ROTATE_RIGHT = "RollRotationFlyCameraRotateRight";
    
    public RollRotationFlyCamera(Camera cam)
    {
        super(cam);
    }
    
    @Override
    public void registerWithInput(InputManager man)
    {
        super.registerWithInput(man);

        inputManager.deleteMapping(CameraInput.FLYCAM_RISE);
        inputManager.deleteMapping(CameraInput.FLYCAM_LOWER);
        
        inputManager.addMapping(ROTATE_LEFT, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(ROTATE_RIGHT, new KeyTrigger(KeyInput.KEY_E));
        
        inputManager.addListener(this, ROTATE_LEFT, ROTATE_RIGHT);
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf)
    {
        switch(name)
        {
            case ROTATE_LEFT:
                break;
            case ROTATE_RIGHT:
                break;
            default:
                super.onAnalog(name, value, tpf);
                break;
        }
    }
}
