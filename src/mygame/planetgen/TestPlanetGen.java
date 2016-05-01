package mygame.planetgen;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPlanetGen extends SimpleApplication
{
    Geometry planet;

    public static void main(String[] args)
    {
        TestPlanetGen app = new TestPlanetGen();
        app.start();
    }

    @Override
    public void simpleInitApp()
    {
        Logger.getLogger("com.jme3").setLevel(Level.SEVERE);

        this.getCamera().setLocation(new Vector3f(0,0,1000));
        this.getFlyByCamera().setMoveSpeed(1000.0f);
//        this.getFlyByCamera().setZoomSpeed(20.0f);

        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.2f));

        rootNode.addLight(sun);
        rootNode.addLight(al);

//      public PlanetGen(float radius, Random generator)
        PlanetGen planetGen = new PlanetGen(250, new Random().nextLong());

        // Add planet
        planet = new Geometry("Planet");
        planet.setMesh(planetGen.getMesh());
        planet.setCullHint(CullHint.Never);

        Material mat = new Material(this.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
//        mat.getAdditionalRenderState().setWireframe(true);


        planet.setMaterial(mat);

        rootNode.attachChild(planet);
    }

    @Override
    public void simpleUpdate(float tpf)
    {
        planet.rotate(0, 0.1f*tpf, 0);
    }
}