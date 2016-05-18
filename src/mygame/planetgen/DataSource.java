package mygame.planetgen;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import static com.jme3.math.FastMath.atan2;
import static com.jme3.math.FastMath.asin;
import mygame.util.SphereMath;

/**
 *
 * @author Sameer
 */
public interface DataSource
{
    // 0 <= theta <= 2pi ;; 0 <= phi <= pi
    // as defined at http://mathworld.wolfram.com/Sphere.html
    public float getRadius(float phi, float theta, float baseRadius);
    
    public default float getPrecision(boolean horizontal)
    {
        return FastMath.PI / 64;
    }
    
    public static final DataSource FLAT = (phi, theta, baseRadius) -> baseRadius;
    
    public static DataSource HEIGHTMAP(AbstractHeightMap map, float modulation)
    {
        return new DataSource()
        {
            @Override
            public float getRadius(float phi, float theta, float baseRadius)
            {
                Vector3f d = SphereMath.getCoords(phi, theta, baseRadius);
                d.normalizeLocal();
                
                float u = 0.5f + (atan2(d.z, d.x) / FastMath.TWO_PI);
                float v = 0.5f - (asin(d.y) / FastMath.PI);
                
                return baseRadius + (modulation * (map.getInterpolatedHeight(u * map.getSize(), v * map.getSize())));
            }
            
            @Override
            public float getPrecision(boolean horizontal)
            {
                float size = map.getSize();       
                return horizontal ? FastMath.TWO_PI / size : FastMath.PI / size;
            }
        };
    }
}
