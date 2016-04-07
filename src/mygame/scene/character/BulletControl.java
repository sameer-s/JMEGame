package mygame.scene.character;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import mygame.game.ServerMain;

/**
 *
 * @author Sameer Suri
 */
public class BulletControl extends GhostControl implements PhysicsTickListener, Savable
{
    private float ttl = Float.MAX_VALUE;

    private Vector3f movementVector;

    @SuppressWarnings("LeakingThisInConstructor")
    public BulletControl(CollisionShape shape, Vector3f movementVector)
    {
        super(shape);
        this.movementVector = movementVector;
        ServerMain.instance.addPhysicsTickListener(this);
    }

    public BulletControl(CollisionShape shape, Vector3f movementVector, float ttl)
    {
        this(shape, movementVector);
        this.ttl = ttl;
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf)
    {
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf)
    {
        this.getOverlappingObjects().stream()
                .filter(obj -> obj instanceof mygame.scene.GameObject.Destructible)
                .map(obj -> (mygame.scene.GameObject.Destructible) obj)
                .forEach(mygame.scene.GameObject.Destructible::destroyGameObject);

        if(this.getOverlappingObjects().stream().anyMatch(obj -> obj instanceof mygame.scene.GameObject.Destructible))
        {
            ServerMain.instance.removeSpatial(spatial);
        }
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);

        ttl -= tpf;

        if(ttl <= 0)
        {
            ServerMain.instance.removeSpatial(spatial);
        }

        this.spatial.setLocalTranslation(this.spatial.getLocalTranslation().add(movementVector));
    }
}
