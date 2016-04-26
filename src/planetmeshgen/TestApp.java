package planetmeshgen;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class TestApp extends SimpleApplication {

    Geometry planet;

    public static void main(String[] args){
        TestApp app = new TestApp();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Setup camera
        this.getCamera().setLocation(new Vector3f(0,0,1000));
        this.getFlyByCamera().setMoveSpeed(1000.0f);

        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.2f));
        rootNode.addLight(sun);
        rootNode.addLight(al);

        // Add planet
        planet = new Geometry("Planet");

        PlanetMeshGen planetMeshGen = new PlanetMeshGen();
        planetMeshGen.generateHeightmap();
        planet.setMesh(planetMeshGen.generateMesh());

        Material mat = new Material(this.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        // Uncommet for wireframe
//        mat.getAdditionalRenderState().setWireframe(true);

        planet.setMaterial(mat);

        rootNode.attachChild(planet);

    }

    @Override
    public void simpleUpdate(float tpf) {
        planet.rotate(0, 0.1f*tpf, 0);
    }

}