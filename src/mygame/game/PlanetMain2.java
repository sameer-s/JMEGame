package mygame.game;
 
import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import mygame.planetgen.ColoringAgent;
import mygame.planetgen.DataSource;
import mygame.planetgen.Planet;
import org.lwjgl.input.Keyboard;
 
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
//        flyCam.setMoveSpeed(4000);
//        flyCam.setRotationSpeed(7);
        
        cam.setFrustumFar(3000f);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(sun); 
        
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(.1f));
        rootNode.addLight(ambient);  
        
        AbstractHeightMap map;
        try
        {
//            map = new HillHeightMap(513, 1000, 50, 1000, 3);
//            map = new MidpointDisplacementHeightMap(2049, 1, .5f);
          
            /*
            final int size = 8;
            
            float[] blankMap = new float[(int) Math.pow(Math.pow(2,  size) + 1, 2)];
            for(int i = 0; i < blankMap.length; i++)
            {
                blankMap[i] = .5f;
            }
            
//            for(int i = 0 ; i < blankMap.length; i++) blankMap[i] = 1f;
            
            map = new RawHeightMap(blankMap);
        
            System.out.println(Arrays.toString(blankMap));
            */
            
            Texture heightMapImage = assetManager.loadTexture("Textures/sphericalheightmap.jpg");
            map = new ImageBasedHeightMap(heightMapImage.getImage());
            map.load();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        DataSource source = DataSource.HEIGHTMAP(map, .25f);
        source = DataSource.FLAT;
        
        Planet planet = new Planet("Planet", source, 250f, assetManager, ColoringAgent.DICOLOR, true);
        planet.toggleDebug();
        planet.addDebugTriggers(inputManager, new KeyTrigger(Keyboard.KEY_P), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        
        rootNode.attachChild(planet);
        
        
        ChaseCamera chaseCam = new ChaseCamera(cam, planet, inputManager);
        chaseCam.setDefaultDistance(800);
        chaseCam.setMaxDistance(1500);
        chaseCam.setMinDistance(300);
        chaseCam.setDragToRotate(false);
        chaseCam.setToggleRotationTrigger();
        chaseCam.setMinVerticalRotation(-FastMath.PI);
        chaseCam.setZoomSensitivity(35);
        
        cam.setLocation(new Vector3f(0, 0, 700));
        
        inputManager.addMapping("CaptureMouse", new KeyTrigger(Keyboard.KEY_X));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> chaseCam.setDragToRotate(chaseCam.isDragToRotate() ^ isPressed), "CaptureMouse");
    }
}