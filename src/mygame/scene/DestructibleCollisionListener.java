package mygame.scene;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;

/**
 *
 * @author Sameer Suri
 */
public class DestructibleCollisionListener implements PhysicsCollisionListener
{
    @Override
    public void collision(PhysicsCollisionEvent event)
    {
        PhysicsCollisionObject nodeA = event.getObjectA(),
                               nodeB = event.getObjectB();

        System.out.println(nodeA + " " + nodeB);

        if(nodeA instanceof DestructibleGameObject && nodeB instanceof DestructibleGameObject)
        {
            DestructibleGameObject dgoA = (DestructibleGameObject) nodeA;
            DestructibleGameObject dgoB = (DestructibleGameObject) nodeB;

            boolean destroyDgoA = dgoA.shouldBeDestroyed(dgoB.getInfo());
            boolean destroyDgoB = dgoB.shouldBeDestroyed(dgoA.getInfo());

            if(destroyDgoA) dgoA.destroyGameObject(dgoB.getInfo());
            if(destroyDgoB) dgoB.destroyGameObject(dgoA.getInfo());
        }
    }
}
