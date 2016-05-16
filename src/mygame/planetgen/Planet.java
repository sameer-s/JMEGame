package mygame.planetgen;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;
import mygame.util.SphereMath;

/**
 *
 * @author Sameer
 */
public class Planet extends Geometry
{
    private final String PLANET_DEBUG_ACTION_NAME;
    
    public Planet(String name, DataSource source, float radius, AssetManager assetManager, ColoringAgent color, boolean shaded)
    {
        super(name);
        
        PLANET_DEBUG_ACTION_NAME = "PlanetDebugAction" + name;
        
        mesh = new Mesh();
        
        float hPrecision = source.getPrecision(true);
        float vPrecision = source.getPrecision(false);
        
        List<Vector3f> vertices = new ArrayList<>();
        List<ColorRGBA> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        
        final int thetaIter = (int) (FastMath.TWO_PI / hPrecision);
        final int phiIter = (int) (FastMath.PI / vPrecision);           
        
        // Defines a function for converting a theta and phi index into one overall index
        IntBinaryOperator getIndex = (t, p) -> (t * phiIter) + p;
        
        for(float theta = 0; theta < FastMath.TWO_PI; theta += hPrecision)
        {
            for(float phi = 0; phi < FastMath.PI; phi += vPrecision)
            {
                final float adjustedRadius = source.getRadius(phi, theta, radius);
                vertices.add(SphereMath.getCoords(phi, theta, adjustedRadius));
                                
//                final float index = ((phi / vPrecision) * thetaIter) + (theta / hPrecision);
//                final float cVal = (index) / ((float)(thetaIter * phiIter));
//                colors.add(new ColorRGBA(cVal, 0f, 1 - cVal, 1f));  

                colors.add(color.getColor(adjustedRadius, radius));
            }
        }
       
        
        for(int t = 0; t < thetaIter; t++)
        {
            for(int p = 0; p < phiIter; p++)
            {
                int i1 = getIndex.applyAsInt(t, p);
                int i2 = getIndex.applyAsInt((t + 1) % thetaIter, p);
                int i3 = getIndex.applyAsInt((t + 1) % thetaIter, p + 1);
                int i4 = getIndex.applyAsInt(t, p + 1);
                
                if(p != phiIter - 1)
                {
                    indices.add(i1);
                    indices.add(i2);
                    indices.add(i3);
                    indices.add(i1);
                    indices.add(i3);
                    indices.add(i4);
                }
                else
                {
                    i3 = phiIter + p + 1;
                    i4 = phiIter;
                }
                
                Vector3f v1 = vertices.get(i1);
                Vector3f v2 = vertices.get(i2);
                Vector3f v3 = vertices.get(i3);
                Vector3f v4 = vertices.get(i4);

                Vector3f normal;
                Vector3f t1, t2, t3, t4;
                Vector3f n1, n2, n3, n4;

                t1 = v1.subtract(v1);
                t2 = v2.subtract(v3);
                t3 = v3.subtract(v4);
                t4 = v4.subtract(v1);

                n1 = t1.cross(t2).normalize();
                n2 = t2.cross(t3).normalize();
                n3 = t3.cross(t4).normalize();
                n4 = t4.cross(t1).normalize();

                normal = n1.add(n2).add(n3).add(n4).normalize();
                normals.add(normal);
            }
        }
                
//        mesh.setMode(Mesh.Mode.Lines);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices.toArray(new Vector3f[vertices.size()])));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors.toArray(new ColorRGBA[colors.size()])));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices.stream().mapToInt(i->i).toArray()));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals.toArray(new Vector3f[normals.size()])));
        mesh.updateBound();
        
        
        Material mat;
        if(shaded)
        {
            mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseVertexColor", true);
        }
        else
        {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setBoolean("VertexColor", true);
        }
        
        this.setMaterial(mat);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
    }
    
    public Planet(String name, DataSource source, float radius, AssetManager assetManager, ColoringAgent color)
    {
        this(name, source, radius, assetManager, color, true);
    }

    public void addDebugTriggers(InputManager man, Trigger... triggers)
    {
        man.addListener((ActionListener)(actionName, isPressed, tpf) -> { if(name.equals(PLANET_DEBUG_ACTION_NAME) && isPressed) toggleDebug(); }, PLANET_DEBUG_ACTION_NAME);
        man.addMapping(PLANET_DEBUG_ACTION_NAME, triggers);
    }
    
    public void toggleDebug()
    {
        switch(mesh.getMode())
        {
            case Points:
                mesh.setMode(Mesh.Mode.Lines);
                break;
            case Lines:
                mesh.setMode(Mesh.Mode.Triangles);
                break;
            case Triangles:
                mesh.setMode(Mesh.Mode.Points);
                break;
        }
    }
}
