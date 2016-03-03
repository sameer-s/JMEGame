package mygame.character;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.HashMap;

/**
 *
 * @author Sameer Suri
 */
public class ShipCharacterControl extends ThirdPersonCharacterControl
{
    // Overriden class fields
    static
    {
        _radius = 2f;
        _height = 3f;
        xOffset = 0;
        yOffset = -1.5f;
        zOffset = -1.35f;
        _mass = 1;
        moveSpeed = 10;
    }
    
    private static final int throttleInc = 1, minThrottle = -100, maxThrottle = 100;
    private int throttle = 0;
    
    public ShipCharacterControl(Spatial spatial, Camera cam)
    {
        super(new HashMap<>(), spatial, cam);
    }
    
    @Override
    public void initKeys(InputManager inputManager, int joystickId)
    {
        inputManager.addMapping("Throttle+", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Throttle-", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addMapping("Debug", new KeyTrigger(KeyInput.KEY_B));
        
        inputManager.addListener(this, "Throttle+", "Throttle-");
    }
    
    @Override
    public void onAction(String action, boolean isPressed, float tpf)
    {
        super.onAction(action, isPressed, tpf);
        
        switch(action)
        {
            case "Throttle+":
                throttle = Math.min(throttle + throttleInc, maxThrottle);
                break;
            case "Throttle-":
                throttle = Math.max(throttle - throttleInc, minThrottle);
                break;
        }
    }
}
