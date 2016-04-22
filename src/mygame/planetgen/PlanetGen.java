package mygame.planetgen;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.Random;
import planetmeshgen.PlanetMeshGen;

public class PlanetGen
{
    private Mesh mesh;
//    private Vector2f[] textures;

    public PlanetGen(int zSamples, int radialSamples, float radius, float roughness, Random generator)
    {
        mesh = new Sphere(zSamples, radialSamples, radius);

        PlanetMeshGen pmg = new PlanetMeshGen();
        pmg.generateHeightmap();

        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        Vector3f[] vertices = BufferUtils.getVector3Array(vertexBuffer);
        ColorRGBA[] colors = new ColorRGBA[vertices.length];

        for (int i = 0; i < vertices.length; i++)
        {
            float diff = 1;
            diff = pmg.heightmapData[i];


            diff *= 100;

            if(roughness != 0)
            {
//                diff = (float) ((roughness * generator.nextGaussian()) + 1);
//                vertices[i].multLocal(diff);
                vertices[i].normalize();
                vertices[i].multLocal(radius + diff);
            }

//            diff = (vertices[i].distance(Vector3f.ZERO)) / radius;


            if(diff <= 1.005f)
            {
                colors[i] = new ColorRGBA(0f, .4f, .8f, 1f);
            }
            else if(diff <= 1.02f)
            {
                colors[i] = new ColorRGBA(.83f, .72f, .34f, 1f);
            }
            else if(diff <= 1.03f)
            {
                colors[i] = new ColorRGBA(.2f, .6f, .1f, 1f);
            }
            else
            {
                colors[i] = new ColorRGBA(.5f, .5f, .5f, 1f);
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors));
    }


    private Vector3f getCoords(float radius, float phi, float theta)
    {
        Vector3f coords = new Vector3f();

        coords.x = radius * FastMath.sin(phi) * FastMath.cos(theta);
        coords.y = radius * FastMath.sin(phi) * FastMath.sin(theta);
        coords.z = radius * FastMath.cos(phi);

        return coords;
    }

    public Mesh getMesh()
    {
        return mesh;
    }
}
