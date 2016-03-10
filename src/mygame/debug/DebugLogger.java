package mygame.debug;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author Sameer Suri
 */
public class DebugLogger
{
    private static FileOutputStream out = null;

    private static void init() throws FileNotFoundException
    {
        out = new FileOutputStream("./out/jmeGameDebug.txt");
    }

    public static void print(Object o)
    {
        try
        {
            if(out == null) init();

            out.write(toString(o).getBytes());
        }
        catch(IOException e)
        {
            System.err.println("DebugLogger: Error logging object with exception " + e.getClass().getSimpleName());
        }
    }

    public static void println(Object o)
    {
        print(toString(o) + System.lineSeparator());
    }

    public static void printf(Object o, Object... args)
    {
        print(String.format(toString(o), args));
    }

    public static void printfln(Object o, Object... args)
    {
        printf(toString(o) + "%n", args);
    }

    private static String toString(Object o)
    {
        if(o.getClass().isArray())
        {
            if(o instanceof Object[])
            {
                return Arrays.deepToString((Object[]) o);
            }
            else
            {
                Object[] oArr = new Object[Array.getLength(o)];
                for(int i = 0; i < oArr.length; i++)
                {
                    oArr[i] = Array.get(o, i);
                }

                return Arrays.deepToString(oArr);
            }
        }
        else
        {
            return o.toString();
        }
    }

    public static void close()
    {
        try
        {
            out.close();
        }
        catch(IOException | NullPointerException e)
        {
            System.err.println("DebugLogger: Failure to close output stream due to " + e.getClass().getSimpleName());
        }
    }
}
