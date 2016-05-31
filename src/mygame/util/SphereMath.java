package mygame.util;

import static com.jme3.math.FastMath.cos;
import static com.jme3.math.FastMath.sin;
import com.jme3.math.Vector3f;

/**
 *
 * @author Sameer
 */
public abstract class SphereMath
{
    public static final Vector3f getCoords(float phi, float theta, float radius)
    {
        Vector3f d = new Vector3f();
        d.x = radius * cos(theta) * sin(phi);
        d.y = radius * cos(phi);
        d.z = radius * sin(theta) * sin(phi);
        
        System.out.printf("[SphereMath] Input (phi,theta)=(%f,%f) with radius %f. Output vertex: %s.%n", phi, theta, radius, d);
        
        return d;
    }
}
