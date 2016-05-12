package mygame.game;
 
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.terrain.geomipmap.TerrainQuad;
import mygame.planetgen.DataSource;
import mygame.planetgen.Planet;
 
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

        System.out.println("Start");
        
        Geometry planet = new Planet("Planet", DataSource.FLAT, 250f, assetManager);
        
        System.out.println("End");
        rootNode.attachChild(planet);
    }
}