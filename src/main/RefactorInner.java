package main;

import java.io.File;
import java.util.ArrayList;

public class RefactorInner {
	
	
	public static void main(String[]  args)
	{
		String dir ="/home/abdulaziz/Desktop/BerkeleyDb/javaFiles/base_copy/com/sleepycat";
		String outDir =""+new File(".").getAbsolutePath()+"output/";
		if(args.length > 0)
			dir = args[0];
		if (args.length > 1)
			outDir = args[1];
		
		ArrayList<String>fileNames= getAllFilesInDirectory(dir);
		for(String f : fileNames)
		{
			System.out.println(f);
		}
		
		
		
		
	}

	private static ArrayList<String> getAllFilesInDirectory(String dir) {
		
		ArrayList<String> files = new ArrayList<>();
		File f=new File(dir);
		File[] allSubFiles=f.listFiles();
		
		for (File file : allSubFiles) {
		    if(file.isDirectory())
		    {
	        files.addAll(getAllFilesInDirectory(file.getAbsolutePath()));

		    }
		    else
		    {
	        files.add(file.getAbsolutePath());

		    }
		}
		
		
		return files;
	}

}
