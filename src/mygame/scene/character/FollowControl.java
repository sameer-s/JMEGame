package mygame.scene.character;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author ssuri4121
 */
public class FollowControl extends RigidBodyControl
{
    private Spatial follow;
    private Vector3f offset;
    
    public FollowControl(Spatial follow, Vector3f offset)
    {
        this.follow = follow;
        this.offset = offset;
    }
    
    public FollowControl(Spatial follow, float x, float y, float z)
    {
        this(follow, new Vector3f(x, y, z));
    }
    
    public FollowControl(Spatial follow)
    {
        this(follow, Vector3f.ZERO);
    }
    
    @Override
    public void update(float tpf)
    {
        Vector3f _offset = this.offset.clone().mult(spatial.getLocalRotation().getRotationColumn(2));
                
        Vector3f lerp = FastMath.interpolateLinear(.1f, spatial.getLocalTranslation(), follow.getLocalTranslation().add(_offset));
        spatial.setLocalTranslation(lerp);
        
        System.out.printf("current: %s%ntarget: %s%nlerp: %s%n%n", spatial.getLocalTranslation(), follow.getLocalTranslation(), lerp);
    }
}
