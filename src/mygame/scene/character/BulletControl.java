package mygame.scene.character;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.Map;
import mygame.game.PlanetMain_Buggy;
import mygame.scene.DestructibleGameObject;

/**
 *
 * @author Sameer Suri
 */
public class BulletControl extends GhostControl implements DestructibleGameObject
{
    private float ttl = Float.MAX_VALUE;

    private Vector3f movementVector;

    private String playerCode;

    public BulletControl(CollisionShape shape, Vector3f movementVector, String playerCode)
    {
        super(shape);
        this.movementVector = movementVector;
        this.playerCode = playerCode;
    }

    public BulletControl(CollisionShape shape, Vector3f movementVector, float ttl, String playerCode)
    {
        this(shape, movementVector, playerCode);
        this.ttl = ttl;
    }

    @Override
    public void destroyGameObject(Map<String, Object> data)
    {

    }

    @Override
    public Map<String, Object> getInfo()
    {
        HashMap<String, Object> info = new HashMap<>();
        info.put("ObjectType", "Bullet");
        info.put("PlayerCode", playerCode);
        return info;
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);

        ttl -= tpf;

        if(ttl <= 0)
        {
            PlanetMain_Buggy.instance.removeSpatial(spatial);
        }

        this.spatial.setLocalTranslation(this.spatial.getLocalTranslation().add(movementVector));
    }
}
