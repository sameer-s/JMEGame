package mygame.util;

import com.google.gson.Gson;

/**
 *
 * @author Sameer
 */
public class Configuration
{
    public int width = 1024;
    public int height = 768;
    public int physicsAccuracy = 600;
    public int samples = 0;
    public boolean fullscreen = false;
    
    public Configuration() {}
    
    public String serialize()
    {
        String s = new Gson().toJson(this);
        return s;
    }
    
    public static Configuration deserialize(String in)
    {
        Configuration c = new Gson().fromJson(in.trim(), Configuration.class);
        return c;
    }
}
