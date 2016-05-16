package mygame.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        String s = new GsonBuilder().setPrettyPrinting().create().toJson(this);
        s = "  " + s.substring(1, s.length() - 1).trim();
        String[] split = s.split("\r\n|\r|\n"); // one regex to rule them all ("them" being newlines)
        for(int i = 0; i < split.length; i++)
        {
            split[i] = split[i].substring(2);
        }
        return String.join(System.lineSeparator(), split);
    }
    
    public static Configuration deserialize(String in)
    {
        Configuration c = new Gson().fromJson("{" + in + "}", Configuration.class);
        return c;
    }
}
