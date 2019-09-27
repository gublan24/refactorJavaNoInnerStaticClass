package main;

import java.io.*;
import java.util.ArrayList;

public class RunUmplificationForDir {
	
	public static void main(String[] args) {
		String dir = "/home/abdulaziz/Desktop/BerkeleyDb/all_feature_try/";
		appendMasterUmpFile(dir);
		//doUmplificationForMultipleSubDirectories(dir);
		System.out.println("Done, main method finished ...");
	}
	public static void appendMasterUmpFile(String dir) {
		ArrayList<String> featureDirList = RefactorInner.getSubDirectory(dir);
		System.out.println("Start umplification ...");
		for(String featureDir : featureDirList)
			//doUmplification(featureDir);
		{
			File f = new File(featureDir+"/ump_inner/Master.ump"); 
			if(f.exists())
			{
				try {
					System.out.println(f.toString());
					BufferedWriter logFile = new BufferedWriter(new FileWriter(f,true));
					logFile.write("\n // adding inner elements ; \n");
					logFile.write("use ../ump_inner/Master.ump ; \n");
					logFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Done, doUmplificationForMultipleSubDirectories method finished ...");
	}
	
	public static void doUmplificationForMultipleSubDirectories(String dir) {
		ArrayList<String> featureDirList = RefactorInner.getSubDirectory(dir);
		System.out.println("Start umplification ...");
		for(String featureDir : featureDirList)
			doUmplification(featureDir);
		System.out.println("Done, doUmplificationForMultipleSubDirectories method finished ...");
	}
	
	private static void doUmplification(String dir) {
		System.out.println("we are do umplification for :"+dir);
		String result = "";
		int counter = 0;
		int fileCount = 1;
		try {
			// for(String featureDir :featureDirList)

			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("java -jar /home/abdulaziz/Desktop/BerkeleyDb/umplificator.jar " + dir+"/com" + " --path="+dir+"/umpilif");

			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("run umple.jar ...");
			
			
			while ((line = br.readLine()) != null) {
				result += line + "\n";
				counter++;

				if (counter == 70 || br.readLine() == null) // last line.
				{

					try {
						BufferedWriter logFile = new BufferedWriter(new FileWriter("umpleJar" + fileCount + ".txt"));
						logFile.write(result + "\n");
						logFile.close();
						fileCount++;
						result = "";
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}
			System.out.println("***************************************************");
			int exitVal = proc.waitFor();
			if(exitVal!=0)
			System.out.println("Process exitValue: " + exitVal  +" for directory: "+dir);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
