/*
Copyright (c) 2012 Aaron Perkins

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package mygame.game;

import jmeplanet.test.*;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.system.AppSettings;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import javax.swing.JOptionPane;

import jmeplanet.Planet;
import jmeplanet.FractalDataSource;
import jmeplanet.PlanetAppState;
import jmeplanet.PlanetCollisionShape;
import mygame.util.Configuration;

/**
 * PlanetPhysicsTest
 * 
 */
public class PlanetMain extends SimpleApplication {
    
    private BulletAppState bulletAppState;
    private PlanetAppState planetAppState;
    private CameraNode cameraNode;
    private RigidBodyControl cameraNodePhysicsControl;
    private boolean physicsDebug = false;
    private float linearSpeed = 10000f;
    private float angularSpeed = 50f;
    
    private Configuration config;
    
    public static void main(String[] args)
    {
        PlanetMain app = new PlanetMain();
        
        File file = new File("out/game.cfg");
        if(file.canRead())
        {
            FileReader in = null;
            try
            {
                in = new FileReader(file);
                
                StringBuilder sb = new StringBuilder();
                for(int character = in.read(); character != -1; character = in.read())
                {
                    sb.append((char) character);
                }
                
                app.config = Configuration.deserialize(sb.toString());
            }
            catch(IOException e)
            {
                e.printStackTrace(System.err);
            }
            finally
            {
                try
                {
                    if(in != null) in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
        else
        {
            popup("Configuration file does not exist or is not readable. Assuming default config...");
            app.config = new Configuration();
        }
        
        AppSettings settings = new AppSettings(true);
        settings.setResolution(app.config.width, app.config.height);
        settings.setSamples(app.config.samples);
        settings.setFullscreen(app.config.fullscreen);
        app.setSettings(settings);
        app.showSettings = false;
        app.start();
    }
    
    private static void popup(String message)
    {
        JOptionPane.showMessageDialog(null, message);
    }
    
    public PlanetMain() {
        super( new StatsAppState(), new DebugKeysAppState() );
    }
 
    @Override
    public void simpleInitApp() {
        // Only show severe errors in log
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // setup physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
        bulletAppState.getPhysicsSpace().setAccuracy(1f/((float) config.physicsAccuracy));
         
        // setup input
        setupInput();
        
        // setup camera and camera node
        CameraControl cameraControl = new CameraControl(this.getCamera(),ControlDirection.SpatialToCamera);
        cameraNode = new CameraNode("Camera", cameraControl);
        cameraNode.setLocalTranslation(new Vector3f(-50000f, 0f, 150000f));
        cameraNode.rotate(0, FastMath.PI, 0);
        cameraNodePhysicsControl = new RigidBodyControl(new SphereCollisionShape(2.5f), 1f);
        cameraNode.addControl(cameraNodePhysicsControl);
        rootNode.attachChild(cameraNode);
        bulletAppState.getPhysicsSpace().add(cameraNode);
        cameraNodePhysicsControl.setAngularFactor(0);
        cameraNodePhysicsControl.setLinearDamping(0.8f);
        cameraNodePhysicsControl.setAngularDamping(0.99f);
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(new ColorRGBA(0.45f,0.45f,0.35f,1.0f));
        sun.setDirection(new Vector3f(1f, -1f, 0f));
        
        rootNode.addLight(sun);
        
        // Add planet app state
        planetAppState = new PlanetAppState(rootNode, sun);
        stateManager.attach(planetAppState);
        
        Random r = new Random();
        
        // Add planet
        FractalDataSource planetDataSource = new FractalDataSource(r.nextInt(Integer.MAX_VALUE));
        planetDataSource.setHeightScale(800f);
        Planet planet = Utility.createEarthLikePlanet(getAssetManager(), 63710.0f, null, planetDataSource);
        planet.addControl(new RigidBodyControl(new PlanetCollisionShape(planet.getLocalTranslation(), planet.getRadius(), planetDataSource), 0f));
        planetAppState.addPlanet(planet);
        rootNode.attachChild(planet);
        bulletAppState.getPhysicsSpace().add(planet);
        
        // Add moon
        FractalDataSource moonDataSource = new FractalDataSource(r.nextInt(Integer.MAX_VALUE));
        moonDataSource.setHeightScale(300f);
        Planet moon = Utility.createMoonLikePlanet(getAssetManager(), 10000, moonDataSource);
        moon.setLocalTranslation(-100000f, 0f, 0f);
        RigidBodyControl moonPhysicsControl = new RigidBodyControl(new PlanetCollisionShape(moon.getLocalTranslation(), moon.getRadius(), moonDataSource), 0f);
        moon.addControl(moonPhysicsControl);   
        planetAppState.addPlanet(moon);
        rootNode.attachChild(moon);
        bulletAppState.getPhysicsSpace().add(moon);  
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        Planet planet = planetAppState.getNearestPlanet();
        if (planet != null && planet.getPlanetToCamera() != null) {
//            cameraNodePhysicsControl.setGravity(planet.getPlanetToCamera().normalize().mult(-100f));
            
            if(linearSpeed == 100) return;
            // uses a quadratic equation (y = -x^2 + 5) to convert the distance from the planet into a coefficient
            // the coefficient is clamped between 1 and 3
            // this is used to get a decreased speed closer to the planet but higher in deep space
            float distanceMod = FastMath.clamp(-(planet.getDistanceToCamera() * planet.getDistanceToCamera()) + 5, 1, 3);
            linearSpeed = FastMath.clamp(distanceMod * planet.getDistanceToCamera(), 6000, 100000);
            System.out.println(linearSpeed);
        }   
    }
    
    @Override
    public void stop()
    {
        super.stop();
       
        File file = new File("out/game.cfg");
        try
        {
            file.createNewFile();
        }
        catch(IOException e)
        {
            e.printStackTrace(System.err);
        }
          
        if(file.canWrite())
        {
            FileWriter out = null;
            try
            {
                out = new FileWriter(file);
                
                out.write(config.serialize());
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            finally
            {
                try
                {
                    if(out != null) out.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
    
    private void setupInput() {
        // Toggle mouse cursor
        inputManager.addMapping("TOGGLE_CURSOR", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        // Toggle wireframe
        inputManager.addMapping("TOGGLE_WIREFRAME", new KeyTrigger(KeyInput.KEY_T));
        // Toggle physics view
        inputManager.addMapping("TOGGLE_PHYSICS_DEBUG", new KeyTrigger(KeyInput.KEY_P));
        // Movement keys
        inputManager.addMapping("RotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true),
                                               new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("RotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, false),
                                                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("RotateDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                                             new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("RotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                                               new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("SpinLeft", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("SpinRight", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("RotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                                               new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(actionListener, "TOGGLE_WIREFRAME", "TOGGLE_CURSOR", "TOGGLE_PHYSICS_DEBUG");
        inputManager.addListener(analogListener, "MoveLeft","MoveRight","MoveForward","MoveBackward","RotateLeft","RotateRight","RotateUp","RotateDown","SpinLeft","SpinRight" );          
   
        inputManager.setCursorVisible(false);
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){       
            if (name.equals("TOGGLE_CURSOR") && !pressed) {
                if (inputManager.isCursorVisible()) {
                    inputManager.setCursorVisible(false);
                } else {
                    inputManager.setCursorVisible(true);
                }
            }
            if (name.equals("TOGGLE_WIREFRAME") && !pressed) {
                for (Planet planet: planetAppState.getPlanets()) {
                    planet.toogleWireframe();
                }
            }  
            if (name.equals("TOGGLE_PHYSICS_DEBUG") && !pressed) {
                if (!physicsDebug) {
                    bulletAppState.setDebugEnabled(true);
                    for (Planet planet: planetAppState.getPlanets()) {
                        planet.setVisiblity(false);
                    }
                    physicsDebug = true;
                }
                else {
                    bulletAppState.setDebugEnabled(false);
                    for (Planet planet: planetAppState.getPlanets()) {
                       planet.setVisiblity(true);
                    }
                    physicsDebug = false;
                }
            }
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            
            if (name.equals("MoveLeft"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getLeft().mult(linearSpeed));
            if (name.equals("MoveRight"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getLeft().mult(-linearSpeed));
            if (name.equals("MoveForward"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getDirection().mult(linearSpeed));
            if (name.equals("MoveBackward"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getDirection().mult(-linearSpeed));
            
            Vector3f xRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(0).normalize();
            Vector3f yRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(1).normalize();
            Vector3f zRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(2).normalize();

            if (name.equals("RotateLeft"))
                cameraNodePhysicsControl.applyTorque(yRotation.mult(angularSpeed));
            if (name.equals("RotateRight"))
                cameraNodePhysicsControl.applyTorque(yRotation.mult(-angularSpeed));
            if (name.equals("RotateUp"))
                cameraNodePhysicsControl.applyTorque(xRotation.mult(angularSpeed));
            if (name.equals("RotateDown"))
                cameraNodePhysicsControl.applyTorque(xRotation.mult(-angularSpeed));
            if (name.equals("SpinLeft"))
                cameraNodePhysicsControl.applyTorque(zRotation.mult(-angularSpeed));
            if (name.equals("SpinRight"))
                cameraNodePhysicsControl.applyTorque(zRotation.mult(angularSpeed));

        }   
    };
    
}