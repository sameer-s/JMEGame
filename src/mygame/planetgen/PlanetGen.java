package mygame.planetgen;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <code>PlanetMeshGen</code>
 * Generates a planet from a random heightmap.
 * Orginal source:
 * http://ahuynh.posterous.com/article-1-generating-a-planet-in-opengl
 * Adapted for jmonkeyengine by:
 * ajperkins@gmail.com
 */
public class PlanetGen
{
    // Radius of planet
    protected float planetRadius;
    // Width of heightmap
    protected int heightmapWidth;
    // Stores heightmap data
    protected float heightmapData[][];

    public PlanetGen()
    {

    }

    public Mesh generateMesh ()
    {
        return generateMesh(250);
    }

    public Mesh generateMesh (float radius)
    {
        planetRadius = radius;

        Mesh mesh = new Mesh();

        int gammaSamples = heightmapWidth;
        int thetaSamples =  heightmapWidth;

        List<Vector3f> vertexList = new ArrayList<>();
        List<Vector3f> normalList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        List<Float> colorList = new ArrayList<>();

        // Horizontal points
        float gammaStep = 2 * FastMath.PI / thetaSamples;

        // Vertical points
        float thetaStep = FastMath.PI / ( gammaSamples - 1 );

        // Generate vertices
        for( int i = 0; i < thetaSamples; i++ )
        {
            float gamma = i * gammaStep;
            for( int j = 0; j < gammaSamples; j++ )
            {
                float theta = j * thetaStep;

                Vector3f pt = new Vector3f();
                pt.x = planetRadius * FastMath.sin( theta ) * FastMath.cos( gamma );
                pt.y = planetRadius * FastMath.cos( theta );
                pt.z = planetRadius * FastMath.sin( theta ) * FastMath.sin( gamma );

                float height = heightmapData[i][j];

                vertexList.add( pt.normalize().mult(planetRadius + height) );

                // Set vertex colors
                if( height <= 1f )
                {
                    colorList.add(0.0f);
                    colorList.add(0.4f);
                    colorList.add(0.8f);
                    colorList.add(1.0f); // Ocean
                } else if( height <= 1.5f )
                {
                    colorList.add(0.83f);
                    colorList.add(0.72f);
                    colorList.add(0.34f);
                    colorList.add(1.0f); // Sand
                } else if( height <= 10f )
                {
                    colorList.add(0.2f);
                    colorList.add(0.6f);
                    colorList.add(0.1f);
                    colorList.add(1.0f); // Grass
                } else
                {
                    colorList.add(0.5f);
                    colorList.add(0.5f);
                    colorList.add(0.5f);
                    colorList.add(1.0f); // Mountains
                }
            }
        }

        // Generate normals
        for( int i = 0; i < thetaSamples; i++ )
        {
            for( int j = 0; j < gammaSamples; j++ )
            {
                int i1 = i * gammaSamples + j;
                int i2 = ( ( i + 1 ) % thetaSamples ) * gammaSamples + j;
                int i3 = ( ( i + 1 ) % thetaSamples ) * gammaSamples + j + 1;
                int i4 = i * gammaSamples + j + 1;

                if( j >= gammaSamples-1 ) {
                        i3 = gammaSamples + j + 1;
                        i4 = j + 1;
                }

                Vector3f v1 = vertexList.get(i1);
                Vector3f v2 = vertexList.get(i2);
                Vector3f v3 = vertexList.get(i3);
                Vector3f v4 = vertexList.get(i4);

                Vector3f normal;
                Vector3f t1, t2, t3, t4;
                Vector3f n1, n2, n3, n4;

                t1 = v1.subtract( v1 );
                t2 = v2.subtract( v3 );
                t3 = v3.subtract( v4 );
                t4 = v4.subtract( v1 );

                n1 = t1.cross( t2 ).normalize();
                n2 = t2.cross( t3 ).normalize();
                n3 = t3.cross( t4 ).normalize();
                n4 = t4.cross( t1 ).normalize();

                normal = n1.add( n2 ).add( n3 ).add( n4 ).normalize();
                normalList.add(normal);
            }
        }

        // Generate indices
        for( int i = 0; i < thetaSamples; i++ )
        {
            for( int j = 0; j < gammaSamples-1; j++ )
            {
                Integer i1 = i * gammaSamples + j;
                Integer i2 = ( ( i + 1 ) % thetaSamples ) * gammaSamples + j;
                Integer i3 = ( ( i + 1 ) % thetaSamples ) * gammaSamples + j + 1;
                Integer i4 = i * gammaSamples + j + 1;

                indexList.add( i1 );
                indexList.add( i2 );
                indexList.add( i3 );

                indexList.add( i1 );
                indexList.add( i3 );
                indexList.add( i4 );
            }
        }

        // Set buffers
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertexList.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normalList.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indexList)));
        mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(toFloatArray(colorList)));
        mesh.updateBound();

        return mesh;
    }


    public void generateHeightmap(int width, int seed, int numIslands, int islandRadius)
    {
        heightmapWidth = width;
        heightmapData = new float[heightmapWidth][heightmapWidth];

        Random generator;

        if(seed == -1)
        {
            generator = new Random();
        }
        else
        {
            generator = new Random(seed);
        }

        for(int i = 0; i < numIslands; i++)
        {
            int xIndex = generator.nextInt(heightmapData.length);
            int yIndex = generator.nextInt(heightmapData[0].length);
            float height = generator.nextFloat() * 10;

            for(int j = 0; j < islandRadius; j++)
            {
                for(float theta = 0; theta < 2 * FastMath.PI; theta++)
                {
                    int dx = Math.round(FastMath.cos(theta) * j);
                    int dy = Math.round(FastMath.sin(theta) * j);

                    int newXIndex = xIndex + dx;
                    int newYIndex = yIndex + dy;

                    while(newXIndex < 0)
                        newXIndex += heightmapData.length;

                    while(newXIndex >= heightmapData.length)
                        newXIndex -= heightmapData[0].length;

                    while(newYIndex < 0)
                        newYIndex += heightmapData.length;

                    while(newYIndex >= heightmapData.length)
                        newYIndex -= heightmapData[0].length;

                    heightmapData[newXIndex][newYIndex] = height;
                }
            }
        }
    }

    protected int[] toIntArray(List<Integer> list)
    {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list)
            ret[i++] = e;
        return ret;
    }

    protected float[] toFloatArray(List<Float> list)
    {
        float[] ret = new float[list.size()];
        int i = 0;
        for (Float e : list)
            ret[i++] = e;
        return ret;
    }

} // End PlanetMeshGen Class
