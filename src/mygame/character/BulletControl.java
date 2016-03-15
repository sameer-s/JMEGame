package mygame.character;

import com.jme3.bullet.control.RigidBodyControl;

/**
 *
 * @author Sameer Suri
 */
public class BulletControl extends RigidBodyControl
{
    float ttl = Float.MAX_VALUE;

    /**
     * Creates a new bullet [controller].
     * @param mass The mass of the object being controlled
     * @param ttl The time to live, in seconds
     */
    public BulletControl(float mass, float ttl)
    {
        super(mass);

        this.ttl = ttl;
    }

    /**
     * Creates a new bullet [controller] with a default time to live of 20 seconds.
     * @param mass The mass of the object being controlled
     */
    public BulletControl(float mass)
    {
        this(mass, 20f);
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);

        System.out.println("nitipuhalam " + ttl);
        ttl -= tpf;

        if(ttl <= 0)
        {
            spatial.removeFromParent();
        }
    }
}
