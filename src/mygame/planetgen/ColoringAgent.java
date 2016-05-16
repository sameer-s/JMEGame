package mygame.planetgen;

import com.jme3.math.ColorRGBA;

/**
 *
 * @author Sameer
 */
public interface ColoringAgent
{
    public static final ColorRGBA OCEAN_COLOR = new ColorRGBA(.05f, .2f, 1f, 1f);
    
    public default ColorRGBA getColor(float adjustedRadius, float originalRadius)
    {
        return getColor(adjustedRadius / originalRadius);
    }
    
    public ColorRGBA getColor(float factor);
    
    public static ColoringAgent OCEAN = factor -> OCEAN_COLOR;
    public static ColoringAgent DICOLOR = factor -> {
//        System.out.println(factor);
        if(factor > 1.1f) return new ColorRGBA(.2f, .3f, .05f, 1f);
        else return OCEAN_COLOR;
    };
}
