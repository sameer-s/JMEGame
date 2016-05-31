package mygame.planetgen;

import java.util.Arrays;
import java.util.Iterator;
import mygame.util.ArrayUtils;

/**
 *
 * @author Sameer
 */
public class DataSource implements Iterable<float[]>
{
    private float[][] data;
    public float nPole = 1f, sPole = 1f;
    
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
        
        for(int i = 0; i < data.length / 2; i++)
        {
            if(data[i].length > data[i + 1].length)
            {
                throw new IllegalArgumentException("The arrays above the center in DataSource data must be increasing in length.");
            }
        }
                
        for(int i = (data.length / 2) + 1; i < data.length; i++)
        {
            if(data[i].length > data[i - 1].length)
            {
                throw new IllegalArgumentException("The arrays below the center in DataSource data must be decreasing in length.");  
            }
        }
        
        for(int i = 0; i < data.length / 2; i++)
        {            
            if(data[i].length != data[data.length - i - 1].length)
            {
                throw new IllegalArgumentException("The length of the arrays in DataSource data must be symmetrical across the center.");
            }
        }
        
        for(int i = 0; i < data.length / 2; i++)
        {            
            if((data[i].length % 2) != (data[data.length / 2].length % 2))
            {
                String eo = data[data.length / 2].length % 2 == 0 ? "even" : "odd";
                throw new IllegalArgumentException(String.format("Since the center row is of %s length, all other rows must be of %s length as well (data[%d].length=%d)", eo, eo, i, data[i].length));
            }
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
        int centerRowLength = data[data.length / 2].length;
        
        for(int i = 0; i < data.length; i++)
        {
            int length = data[i].length;
            while(length != centerRowLength)
            {
                data[i] = ArrayUtils.stretchArray(data[i]);
                length = data[i].length;
            }
        }
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
