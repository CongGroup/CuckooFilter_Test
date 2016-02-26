package cuckoo_filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class TestDriver {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		CuckooFilter cf = new CuckooFilter();
		System.out.println("Toy cuckoo filter");
		System.out.println("Usage: [i]nsert([if] for insertion by file), [c]ontain, [d]elete, [s]tats, [q]uit");
		
		Random generator = new Random();
		PrintWriter pw = new PrintWriter("test.txt", "UTF-8");
		for(int i=0; i<3800000; ++i)
			pw.println(generator.nextInt(Integer.MAX_VALUE));
		pw.close();
		
		try{
		    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		    String cmd, item;
		    while(true)
		    {
		    	cmd = reader.readLine();
		    	if( cmd.equals("i") )
		    	{
		    		System.out.print("Insert item: ");
		    		item = reader.readLine();
		    		cf.insert(item);
		    	}
		    	else if( cmd.equals("if") )
		    	{
		    		System.out.print("Insert by file: ");
		    		String filepath = "test.txt";//reader.readLine();
		    		BufferedReader fileReader = new BufferedReader(new FileReader(filepath));
		    		while( (item=fileReader.readLine()) != null )
		    			cf.insert(item);
		    		fileReader.close();
		    	}
		    	else if( cmd.equals("c") )
		    	{
		    		System.out.print("Check item: ");
		    		item = reader.readLine();
		    		System.out.println("Item " + item + " is" + (cf.contain(item)?" ":" NOT ") + "in the filter");
		    	}
		    	else if( cmd.equals("d") )
		    	{
		    		System.out.print("Delete item: ");
		    		item = reader.readLine();
		    		cf.delete(item);
		    	}
		    	else if( cmd.equals("s") )
		    	{
		    		System.out.println("Cuckoo filter has " + cf.numOfItems() + " items with " + cf.loadFactor() + " load factor");
		    	}
		    	else if( cmd.equals("q") )
		    		System.exit(0);
		    	else
		    		System.out.println("Usage: [i]nsert([if] for insertion by file), [c]ontain, [d]elete, [s]tats, [q]uit");
		    }
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
}
