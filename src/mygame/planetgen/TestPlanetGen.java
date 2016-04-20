package mygame.planetgen;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

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

        // Add planet
        planet = new Geometry("Planet");

        PlanetGen planetMeshGen = new PlanetGen();
        //    public void generateHeightmap(int width, int seed, int numIslands, float islandRadius, int iterations, float displacement, float smoothing ) {
        planetMeshGen.generateHeightmap(750, -1, 30, 90);
        planet.setMesh(planetMeshGen.generateMesh());

        Material mat = new Material(this.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);

        planet.setMaterial(mat);

        rootNode.attachChild(planet);

    }

    @Override
    public void simpleUpdate(float tpf) {
        planet.rotate(0, 0.005f*tpf, 0);
    }

}