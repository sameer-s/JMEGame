package mygame.planetgen;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.Random;

public class TestPlanetGen extends SimpleApplication
{
    Geometry planet;

    public static void main(String[] args)
    {
        TestPlanetGen app = new TestPlanetGen();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Setup camera
        this.getCamera().setLocation(new Vector3f(0,0,1000));
        this.getFlyByCamera().setMoveSpeed(200.0f);

        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

//      public PlanetGen(int zSamples, int radialSamples, float radius, float roughness, Random generator)
        PlanetGen planetGen = new PlanetGen(30, 30, 250, 0.5f, new Random());
//        planetGen = new PlanetGen(30, 30, 250, 0f, new Random());
                
        // Add planet
        planet = new Geometry("Planet", planetGen.getMesh());
        
        Material mat = new Material(this.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);   
        
        planet.setMaterial(mat);

        rootNode.attachChild(planet);

    }
}