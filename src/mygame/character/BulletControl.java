package mygame.character;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import mygame.character.enemy.DestructibleGameObject;
import mygame.game.Main;

/**
 *
 * @author Sameer Suri
 */
public class BulletControl
{
    public static class RigidBody extends RigidBodyControl
    {
        float ttl = Float.MAX_VALUE;

        /**
         * Creates a new bullet [controller].
         * @param mass The mass of the object being controlled
         * @param ttl The time to live, in seconds
         */
        public RigidBody(float mass, float ttl)
        {
            super(mass);

            this.ttl = ttl;
        }

        /**
         * Creates a new bullet [controller] with a default time to live of 20 seconds.
         * @param mass The mass of the object being controlled
         */
        public RigidBody(float mass)
        {
            this(mass, 20f);
        }

        @Override
        public void update(float tpf)
        {
            super.update(tpf);

            ttl -= tpf;

            if(ttl <= 0)
            {
                spatial.removeFromParent();
                spatial.removeControl(this);
            }
        }
    }

    public static class Ghost extends GhostControl implements PhysicsTickListener
    {
        private Main app;

        public Ghost(CollisionShape shape, Main app)
        {
            super(shape);
            this.app = app;
            app.addPhysicsTickListener(this); // FIXME leaking this in constructor
        }

        @Override
        public void prePhysicsTick(PhysicsSpace space, float tpf)
        {
        }

        @Override
        public void physicsTick(PhysicsSpace space, float tpf)
        {
            this.getOverlappingObjects().stream()
                    .filter(obj -> obj instanceof DestructibleGameObject)
                    .map(obj -> (DestructibleGameObject) obj)
                    .forEach(DestructibleGameObject::destroyGameObject);

            if(this.getOverlappingObjects().stream().anyMatch(obj -> obj instanceof DestructibleGameObject))
            {
                app.enqueue(() -> {
                    try
                    {
                        spatial.removeFromParent();
                        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(spatial);

                        spatial.removeControl(BulletControl.RigidBody.class);
                        spatial.removeControl(BulletControl.Ghost.class);

                        spatial = null;
                    }
                    catch(NullPointerException npe) {}

                    return 0;
                });
            }
        }
    }
}
