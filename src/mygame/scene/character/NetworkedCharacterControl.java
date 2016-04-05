package mygame.scene.character;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mygame.network.message.PlayerInformationMessage;

/**
 * A controller for a character whose location is being updated over network.
 * It is updated by way of PlayerInformationMessage.
 * @author Sameer Suri
 */
public class NetworkedCharacterControl extends RigidBodyControl
{
    public NetworkedCharacterControl()
    {
        // Uses the character's mass for the physics engine
        super();
        // Sets up the collider shape as the character does
        setCollisionShape(ThirdPersonCharacterControl.generateShape());

        setKinematic(true);
    }

    private PlayerInformationMessage m;

    public void recieveMessage(PlayerInformationMessage m)
    {
        this.m = m;
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);

        if(m == null) return;

        Vector3f i = this.spatial.getLocalTranslation();
        Vector3f j = this.getPhysicsLocation();
        Vector3f k = new Vector3f(m.location[0], m.location[1], m.location[2]);

        move(m, tpf);
        rotate(m);
        System.out.println("" + i + j + k + currentLocation + this.spatial.getLocalTranslation() + this.getPhysicsLocation());
    }

    private Vector3f targetLocation;
    private Vector3f currentLocation = new Vector3f();
    private boolean firstUpdate = true;
    private void move(PlayerInformationMessage m, float tpf)
    {
        targetLocation = new Vector3f(m.location[0], m.location[1], m.location[2]);

//        if(firstUpdate == true)
//        {
//            currentLocation = new Vector3f(targetLocation);
//            firstUpdate = false;
//        }
//        else
//        {
//            final float distance = currentLocation.distance(targetLocation);
//            if(distance != 0)
//            {
//                final Vector3f movement = targetLocation.subtract(currentLocation);
//                final Vector3f adjusted = movement.mult((tpf * m.currentSpeed) / distance);
//
//                currentLocation = adjusted.length() > movement.length() ? movement.add(currentLocation) : adjusted.add(currentLocation);
//            }
//        }

        currentLocation = targetLocation;
        
        // Try this one if the bottom one doesnt work
//        this.setPhysicsLocation(currentLocation);

        System.out.println(currentLocation);
        this.spatial.setLocalTranslation(currentLocation);
    }

    private Quaternion currentRotation;
    private void rotate(PlayerInformationMessage m)
    {
        currentRotation = new Quaternion(m.rotation[0], m.rotation[1], m.rotation[2], m.rotation[3]);

        this.spatial.setLocalRotation(currentRotation);
    }
}
