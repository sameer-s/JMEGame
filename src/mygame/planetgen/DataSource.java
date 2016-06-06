package mygame.planetgen;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import mygame.util.ArrayUtils;

/**
 *
 * @author Sameer
 */
public class DataSource implements Iterable<float[]>
{
    private float[][] data;
    public float nPole = 1f, sPole = 1f;
    
    private boolean isRegular = false;
    
    public DataSource()
    {
        
    }
    
    public DataSource(DataSource other)
    {
        data = new float[other.data.length][];

        for(int i = 0; i < other.data.length; i++)
        {
            float[] row = other.data[i];
            int rowLength = row.length;
            data[i] = new float[rowLength];
            
            System.arraycopy(row, 0, data[i], 0, rowLength);
        }
        
        nPole = other.nPole;
        sPole = other.sPole;
    }
    
    public void setData(float[][] data)
    {
        if(data.length % 2 == 0) 
        {
            throw new IllegalArgumentException("DataSource data must have an odd number of rows.");
        }
        
        this.data = data;
    }
   
    public void setData(float[][] data, boolean[][] mask)
    {   
        float[][] jaggedData = new float[data.length][0];
        
        for(int r = 0; r < data.length; r++)
        {
            int sum = 0;
            for(boolean b : mask[r])
            {
                sum += b ? 1 : 0;
            }
            
            float[] row = new float[sum];
            int i = 0;
            for(int c = 0; c < data[0].length; c++)
            {
                if(mask[r][c])
                {
                    row[i] = data[r][c];
                    i++;
                }
            }
            
            jaggedData[r] = row;
        }
        
        setData(jaggedData);
    }
    
    public float[] getRow(int row)
    {
        return data[row];
    }
    
    public int getElementCount()
    {
        int sum = 0;
        for(float[] row : data)
        {
            sum += row.length;
        }
        
        return sum;
    }
    
    public int getRowCount()
    {
        return data.length;
    }
    
    public float getData(int row, int col)
    {
        return data[row][col];
    }
    
    public void regularize()
    {
        if(isRegular) return;
        
        isRegular = true;
        
        int centerRowLength = data[data.length / 2].length;
        
        for(int i = 0; i < data.length; i++)
        {
            int length = data[i].length;

            if(length % 2 != centerRowLength % 2)
            {
                data[i] = ArrayUtils.makeOneLonger(data[i]);
            }
            
            while(length != centerRowLength)
            {
                if(length > centerRowLength)
                {
                    data[i] = Arrays.copyOf(data[i], centerRowLength);
                    break;
                }
                
                data[i] = ArrayUtils.stretchArray(data[i]);
                length = data[i].length;
            }
        }
    }
    
    public void setData(int r, int c, int val)
    {
        
    }
    
    public static final DataSource FLAT = new DataSource();

    static
    {
        float[][] data = new float[127][0];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = new float[(-2 * Math.abs(i - 63)) + 128];
            Arrays.fill(data[i], 0, data[i].length, 1f);
        }
        
        FLAT.setData(data);
    }

    public static final DataSource SAMPLE = new DataSource();
    
    static
    {
        System.out.println("Loading sample image");
        
        final float sensitivity = 0.05f;
        
        try
        {            
            BufferedImage image = ImageIO.read(new File("sample4.png"));
            Raster raster = image.getData();
         
            final int w = raster.getWidth(), h = raster.getHeight(), nb = raster.getNumBands();
            float[][] data = new float[w][];
            
            for(int x = 0; x < w; x++)
            {
                List<Integer> pixels = new ArrayList<>();
                for(int y = 0; y < h; y++)
                {
                    int[] pixel = raster.getPixel(x, y, (int[]) null);
                
                    if(!(pixel[0] == 0 && pixel[1] == 255 && pixel[2] == 0))
                    {
                        pixels.add(pixel[0]);
                    }
                }
                data[x] = new float[pixels.size()];
                
                for(int i = 0; i < pixels.size(); i++)
                {
                    data[x][i] = 1 + (sensitivity * ((pixels.get(i) - 127) / 127f));
                    
//                    System.out.println(data[x][i]); // FIXME print statements cause major lag
                }
            }

            System.out.println(data[255].length);
            SAMPLE.setData(data);
            
            System.out.println("Sample image loaded");
        }
        catch(IOException e)
        {
            System.err.println("Unable to load sample.png into the SAMPLE DataSource.");
        }
    }

    @Override
    public Iterator<float[]> iterator()
    {
        return new Iterator<float[]>()
        {
            int i = 0;
            
            @Override
            public boolean hasNext()
            {
                return i < data.length - 1;
            }

            @Override
            public float[] next()
            {
                return data[i++];
            }
        };
    }
}
