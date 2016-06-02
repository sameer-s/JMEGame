package mygame.util;

/**
 *
 * @author Sameer
 */
public abstract class ArrayUtils
{
    public static float[] stretchArray(float[] in)
    {
        float[] arr = new float[in.length + 2];
        
        if(arr.length % 2 == 1)
        {
            if(arr.length == 3)
            {
                arr[0] = in[0];
                arr[1] = in[0];
                arr[2] = in[0];
                return arr;
            }
            
            arr[0] = in[0];
            arr[arr.length - 1] = in[in.length - 1];
            arr[arr.length / 2] = in[in.length / 2];
            
            final int openSlots = arr.length - 3;
            for(int i = 0; i < in.length - 1; i++)
            {
                final float average = (in[i] + in[i + 1]) / 2f;
               
                if(i < openSlots / 2)
                {
                    arr[i + 1] = average;
                }
                else
                {
                    arr[i + 2] = average;
                }
            }
        }
        else
        {
            if(arr.length == 2)
            {
                return new float[] {1f, 1f};
            }
            
            if(arr.length == 4)
            {
                arr[0] = in[0];
                arr[1] = (in[0] + in[1]) / 2;
                arr[2] = arr[1];
                arr[3] = in[1];
                return arr;
            }
            
            arr[0] = in[0];
            arr[arr.length - 1] = in[in.length - 1];
            arr[(arr.length / 2) - 1] = in[(in.length / 2) - 1];
            arr[arr.length / 2] = in[in.length / 2];
                     
            final int openSlots = arr.length - 4;
            for (int i = 0; i < in.length - 1; i++)
            {
                final float average = (in[i] + in[i + 1]) / 2f;
                
                if(i <= openSlots / 2)
                {
                    arr[i + 1] = average;
                }
                else
                {
                    arr[i + 2] = average;
                }
            }
        }
        
        return arr;
    }
    
    public static float[] makeOneLonger(float[] in)
    {
        float[] out = new float[in.length + 1];
        
        System.arraycopy(in, 0, out, 0, in.length);

        out[out.length - 1] = out[out.length - 2];
        
        return out;
    }
}
