package mygame.game;
 
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
 
public class PlanetMain2 extends SimpleApplication
{
    private TerrainQuad[] terrain = new TerrainQuad[6];
    Material mat;
 
    public static void main(String[] args)
    {
        PlanetMain2 app = new PlanetMain2();
        app.start();
    }
 
    @Override
    public void simpleInitApp()
    { 
        flyCam.setMoveSpeed(500);
        flyCam.setRotationSpeed(5);

        mat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        mat.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        Texture grass = assetManager.loadTexture(
                "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat.setTexture("Tex1", grass);
        mat.setFloat("Tex1Scale", 64f);

        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        mat.setTexture("Tex2", dirt);
        mat.setFloat("Tex2Scale", 32f);

        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        mat.setTexture("Tex3", rock);
        mat.setFloat("Tex3Scale", 128f);

        for(int i = 0; i < terrain.length; i++)
        {
            terrain[i] = new TerrainQuad("TerrainSquare" + i, 9, 65, null);

            terrain[i].setMaterial(mat);
            terrain[i].setLocalScale(2f, 1f, 2f);
            terrain[i].setLocalTranslation(0, i * 10, 0);
            rootNode.attachChild(terrain[i]);

            TerrainLodControl control = new TerrainLodControl(terrain[i], getCamera());
            terrain[i].addControl(control);
        }
        // 0 -> top
        // 1 -> left
        // 2 -> right
        // 3 -> front
        // 4 -> back
        // 5 -> bottom
        
//        terrain[1].setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI/2, new Vector3f(0,0,1)));
        terrain[3].setLocalRotation(new Quaternion().fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0)));
        
        
        for(int i = 2; i < terrain.length; i++)
            if(i!=3)terrain[i].setCullHint(CullHint.Always);
    }
  }