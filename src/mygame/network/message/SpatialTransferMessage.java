package mygame.network.message;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Sameer Suri
 */
@Serializable()
public class SpatialTransferMessage
{
    String data = "";
    
    public static SpatialTransferMessage create(Spatial spatial)
    {
        BinaryExporter exporter = BinaryExporter.getInstance();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try
        {
            exporter.save(spatial, baos);
        }
        catch(IOException e)
        {
            e.printStackTrace(System.err);
        }
       
        SpatialTransferMessage stm = new SpatialTransferMessage();
        try
        {
            stm.data = new String(baos.toByteArray(), "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace(System.err);
        }
        
        return stm;
    }
    
    public static Spatial from(SpatialTransferMessage stm)
    {
        
        
        return null;
    }
    
    public SpatialTransferMessage(){};
}
