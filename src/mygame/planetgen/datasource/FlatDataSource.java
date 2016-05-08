package mygame.planetgen.datasource;

/**
 *
 * @author Sameer
 */
public class FlatDataSource implements DataSource
{
    private final float radius;
    
    public FlatDataSource(float radius)
    {
        this.radius = radius;
    }
    
    @Override
    public float getRadius(float phi, float theta)
    {
        return radius;
    }
}
