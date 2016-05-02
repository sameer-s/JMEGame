package mygame.planetgen;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import planetmeshgen.PlanetMeshGen;

public class PlanetGen
{
    private Mesh mesh;

    public PlanetGen(float radius, long seed)
    {
        radius = 250;

        final float boxWidth = FastMath.sqrt((radius * radius) / 2);
        mesh = new Box(boxWidth / 2, boxWidth / 2, boxWidth / 2);

        Vector3f[] vertices = BufferUtils.getVector3Array((FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData());

        for(Vector3f vertex : vertices)
        {
            vertex.multLocal(radius / vertex.distance(Vector3f.ZERO));
        }

        AbstractHeightMap heightmap;
        try
        {
            int size = (int) Math.ceil((1 + FastMath.sqrt(1 + (8 * vertices.length))) / 4f);

            System.out.println(vertices.length + " "  + size);
            heightmap = new HillHeightMap(size, 10, 10, 100, seed);

            PlanetMeshGen pmg = new PlanetMeshGen();
            pmg.generateHeightmap(size, (int) seed, 30, 90, 25000, .8f, .3f);

            final float[] hd = pmg.heightmapData;

            for(int i = 0; i < hd.length; i++)
            {
                int x = i / heightmap.getSize();
                int z = i % heightmap.getSize();

                heightmap.setHeightAtPoint(hd[i], x, z);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        ColorRGBA[] colors = new ColorRGBA[vertices.length];
        int[] indices = new int[vertices.length * 6];
        Vector3f[] normals = new Vector3f[vertices.length];

        int phis = 2 * (heightmap.getSize() - 1);
        int thetas = heightmap.getSize();

        float phiStep = FastMath.PI / (thetas - 1);
        float thetaStep = FastMath.TWO_PI / phis;

        for(int p = 0; p < phis; p++)
        {
            float theta = p * thetaStep;

            for(int t = 0; t < thetas; t++)
            {
                float phi = t * phiStep;

                Vector3f vertex = getCoords(radius, phi, theta);

                int p2 = p;

                if(p2 >= heightmap.getSize())
                {
                    p2 = ((2 * heightmap.getSize()) - 1) - p2;
                }

                float height = heightmap.getHeightMap()[(p2 * heightmap.getSize()) + t];

                vertex.normalizeLocal().multLocal(radius + height);

                vertices[p * thetas + t] = vertex;

                ColorRGBA color;

                if(height < 1f)
                {
                    color = new ColorRGBA(0, .2f, .75f, 1);
                }
                else if(height < 1.5f)
                {
                    color = new ColorRGBA(.75f, .75f, .25f, 1);
                }
                else if(height < 10f)
                {
                    color = new ColorRGBA(.1f, .75f, .1f, 1);
                }
                else
                {
                    color = new ColorRGBA(.5f, .5f, .5f, 1);
                }

                colors[p * thetas + t] = color;
            }
        }

        int indexListCounter = 0;

        for(int p = 0; p < phis; p++)
        {
            for(int t = 0; t < thetas; t++)
            {
                int point1 = p * thetas + t,
                    point2 = ((p + 1) % phis) * thetas + t,
                    point3 = ((p + 1) % phis) * thetas + t + 1,
                    point4 = p * thetas + t + 1;

                indices[indexListCounter++] = point1;
                indices[indexListCounter++] = point2;
                indices[indexListCounter++] = point3;
                indices[indexListCounter++] = point1;
                indices[indexListCounter++] = point3;
                indices[indexListCounter++] = point4;

                if(t > thetas - 2)
                {
                    point3 = thetas + t + 1;
                    point4 = t + 1;
                }

                Vector3f vertex1 = vertices[point1],
                         vertex2 = vertices[point2],
                         vertex3 = vertices[point3],
                         vertex4 = vertices[point4],

                         diff1 = vertex1.subtract(vertex1),
                         diff2 = vertex2.subtract(vertex3),
                         diff3 = vertex3.subtract(vertex4),
                         diff4 = vertex4.subtract(vertex1),

                         normal1 = diff1.cross(diff2).normalize(),
                         normal2 = diff2.cross(diff3).normalize(),
                         normal3 = diff3.cross(diff4).normalize(),
                         normal4 = diff4.cross(diff1).normalize();

                normals[p * thetas + t] = normal1.add(normal2).add(normal3).add(normal4).normalize();
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.updateBound();
    }


    private Vector3f getCoords(float radius, float phi, float theta)
    {
        Vector3f coords = new Vector3f();

        coords.x = radius * FastMath.sin(phi) * FastMath.cos(theta);
        coords.y = radius * FastMath.cos(phi);
        coords.z = radius * FastMath.sin(phi) * FastMath.sin(theta);

        return coords;
    }

    public Mesh getMesh()
    {
        return mesh;
    }
}
