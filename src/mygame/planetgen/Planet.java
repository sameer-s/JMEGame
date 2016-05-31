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
        
        List<Vector3f> vertices = new ArrayList<>();
        List<ColorRGBA> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        
        DataSource rSource = new DataSource(source);
        rSource.regularize();
        
        final int centerRowLength = source.getRow(source.getRowCount() / 2).length;

        for(int i = 0; i < source.getRowCount(); i++)
        {
            final float[] row = source.getRow(i);
            final float[] rRow = rSource.getRow(i);
            
            final float n = (radius * row.length) / (centerRowLength);
            
            final int ySign = i > source.getRowCount()/2 ? -1 : 1;
            final float y = ySign * FastMath.sqrt(FastMath.sqr(radius) - FastMath.sqr(n));
            
            final float r = FastMath.sqrt(FastMath.sqr(radius) - FastMath.sqr(y));
            
            for(int j = 0; j < rRow.length; j++)
            {
                final float theta = (j + 1) * (FastMath.TWO_PI / rRow.length);
                Vector3f vertex = new Vector3f();
                vertex.x = r * FastMath.cos(theta);
                vertex.y = y;
                vertex.z = r * FastMath.sin(theta);
                vertex.multLocal(rRow[j]);
                vertices.add(vertex);
                
                colors.add(color.getColor(rRow[j]));
            }
        }
                
        for(int i = 0; i < vertices.size(); i++)
        {
            int i2 = i + 1, i3 = i + centerRowLength, i4 = i3 + 1;
            
            if(i2 >= vertices.size() || i3 >= vertices.size() || i4 >= vertices.size()) continue;

            indices.add(i);
            indices.add(i2);
            indices.add(i3);   

            indices.add(i2);
            indices.add(i3);
            indices.add(i4);
            
            // https://www.opengl.org/wiki/Calculating_a_Surface_Normal
            Vector3f u = vertices.get(i2).subtract(vertices.get(i));
            Vector3f v = vertices.get(i3).subtract(vertices.get(i));
            
            normals.add(u.cross(v));
        }
        
        vertices.add(new Vector3f(0, radius * source.nPole, 0));
        vertices.add(new Vector3f(0, -radius * source.sPole, 0));
        colors.add(color.getColor(source.nPole));
//        colors.add(ColorRGBA.Red);
        colors.add(color.getColor(source.sPole));
          
        for(int i = 0; i < vertices.size() - 2; i++)
        {
            int i2, i3;
            
            if(i / centerRowLength == 0)
            {
                i2 = (i + 1) % centerRowLength;
                i3 = vertices.size() - 2;
            }
            else if(i / centerRowLength == source.getRowCount() - 1)
            {
                continue;
            }
            else continue;
            
            System.out.printf("%d %d %d%n", i, i2, i3);
            indices.add(i);
            indices.add(i2);
            indices.add(i3);
            
            Vector3f u = vertices.get(i2).subtract(vertices.get(i));
            Vector3f v = vertices.get(i3).subtract(vertices.get(i));
            
            normals.add(u.cross(v));
        }
        
        indices.add(vertices.size() - 2);
        indices.add(centerRowLength - 1);
        indices.add(centerRowLength);
        
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices.toArray(new Vector3f[vertices.size()])));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors.toArray(new ColorRGBA[colors.size()])));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices.stream().mapToInt(i->i).toArray()));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals.toArray(new Vector3f[normals.size()])));
        
        mesh.setStatic();
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
        man.addListener((ActionListener)(actionName, isPressed, tpf) -> {if(actionName.equals(PLANET_DEBUG_ACTION_NAME) && isPressed) toggleDebug(); }, PLANET_DEBUG_ACTION_NAME);
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
        
        System.out.printf("Toggled Planet \"%s\" mesh to \"%s\" mode.%n", name, mesh.getMode().name());
    }
}
