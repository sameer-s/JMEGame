package mygame.planetgen;

/**
 *
 * @author Sameer
 */
public interface DataSource
{
    // 0 <= phi <= 2pi ;; 0 <= theta <= pi
    public float getRadius(float phi, float theta, float baseRadius);
    
    public static final DataSource
            FLAT = (phi, theta, baseRadius) -> baseRadius;
}
