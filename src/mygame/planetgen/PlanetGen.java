package mygame.planetgen;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.util.BufferUtils;
import planetmeshgen.PlanetMeshGen;

public class PlanetGen
{
    private Mesh mesh;

    public PlanetGen(float radius, long seed)
    {
        mesh = new Mesh();

        AbstractHeightMap heightmap = null;

        radius = 250;

        try
        {
            heightmap = new HillHeightMap(750, 10, 10, 100, seed);

            PlanetMeshGen pmg = new PlanetMeshGen();
            pmg.generateHeightmap();

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

        int phis = 2 * (heightmap.getSize() - 1);
        int thetas = heightmap.getSize();

        float phiStep = FastMath.PI / (thetas - 1);
        float thetaStep = FastMath.TWO_PI / phis;

        Vector3f[] vertices = new Vector3f[phis * thetas];
        ColorRGBA[] colors = new ColorRGBA[phis * thetas];
        int[] indices = new int[phis * thetas * 6];
        Vector3f[] normals = new Vector3f[phis * thetas];

        for(int p = 0; p < phis; p++)
        {
            float theta = p * thetaStep;

            for(int t = 0; t < thetas; t++)
            {
                float phi = t * phiStep;

                Vector3f vertex = getCoords(radius, phi, theta);

                int p2 = p % heightmap.getSize();
                int t2 = t % heightmap.getSize();

                float height = heightmap.getHeightMap()[(p2 * heightmap.getSize()) + t2];

                vertex.normalizeLocal().multLocal(radius + height);

                vertices[p * heightmap.getSize() + t] = vertex;

                ColorRGBA color;

                if(height < 1f)
                {
                    color = ColorRGBA.Blue;
                    color = new ColorRGBA(0, .4f, .8f, 1);
                }
                else if(height < 1.5f)
                {
                    color = ColorRGBA.Brown;
                    color = new ColorRGBA(.83f, .72f, .34f, 1);
                }
                else if(height < 10f)
                {
                    color = ColorRGBA.Green;
                    color = new ColorRGBA(.2f, .6f, .1f, 1);
                }
                else
                {
                    color = ColorRGBA.Gray;
                    color = new ColorRGBA(.5f, .5f, .5f, 1);
                }

                colors[p * heightmap.getSize() + t] = color;
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

                         diff1 = new Vector3f(), // vertex1 - vertex1
                         diff2 = vertex2.subtract(vertex3),
                         diff3 = vertex3.subtract(vertex4),
                         diff4 = vertex4.subtract(vertex1),

                         normal1 = diff1.cross(diff2).normalize(),
                         normal2 = diff2.cross(diff3).normalize(),
                         normal3 = diff3.cross(diff4).normalize(),
                         normal4 = diff4.cross(diff1).normalize();

                normals[p * heightmap.getSize() + t] = normal1.add(normal2).add(normal3).add(normal4).normalize();
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
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
