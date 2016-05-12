package mygame.planetgen;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import planetmeshgen.SphereMath;

/**
 *
 * @author Sameer
 */
public class Planet extends Geometry
{
    public Planet(String name, DataSource source, float radius, AssetManager assetManager)
    {
        super(name);
        
        mesh = new Mesh();
        
        float hPrecision = source.getPrecision(true);
        float vPrecision = source.getPrecision(false);
        
        List<Vector3f> vertices = new ArrayList<>();
        List<ColorRGBA> colors = new ArrayList<>();
                        
        for(float theta = 0; theta < FastMath.TWO_PI; theta += hPrecision)
        {
            for(float phi = 0; phi < FastMath.PI; phi += vPrecision)
            {
                vertices.add(SphereMath.getCoords(phi, theta, source.getRadius(phi, theta, radius)));
                colors.add(new ColorRGBA(theta / FastMath.TWO_PI, 0, phi / FastMath.PI, 1));   
            }
        }
                
        mesh.setMode(Mesh.Mode.Points);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices.toArray(new Vector3f[vertices.size()])));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors.toArray(new ColorRGBA[colors.size()])));
        mesh.updateBound();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        this.setMaterial(mat);
    }
}
