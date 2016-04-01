package mygame.character;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;

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
            }
        }
    }
    
    public static class Ghost extends GhostControl implements PhysicsTickListener
    {
        @Override
        public void prePhysicsTick(PhysicsSpace space, float tpf)
        {
        }

        @Override
        public void physicsTick(PhysicsSpace space, float tpf)
        {
            for(PhysicsCollisionObject obj : this.getOverlappingObjects())
            {
                System.out.println(obj.toString());
            }
        }
    }
}
