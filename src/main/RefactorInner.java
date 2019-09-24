package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RefactorInner {

	public static void main(String[] args) {

		String dir = new File("").getAbsolutePath();
		String outDir = dir + "/../refactor_innerStatic/backup";
		boolean doUmplification = false;
		if (args.length > 0)
		{
			dir = args[0];
			outDir = dir + "/../refactor_innerStatic/backup";
		}
		if (args.length > 1)
			outDir = args[1];
		if (args.length >2)
			doUmplification = args[2].equals("true");
		
		BufferedWriter logFile = null;
		createDirectory(outDir);
		
		System.out.println("STEP 1: prepare java files ... ");
		try {
			logFile = new BufferedWriter(new FileWriter(outDir+"log.txt", true));
			logFile.append("Start: ---------------------------------------------------------" + "\n");

			ArrayList<String> fileNames = getAllFilesInDirectory(dir);
			if (fileNames.size() > 0) {
				for (String file : fileNames) {
					writeFilesWithCommentedInnerStaticClasses(file, outDir, logFile);
				}
			}
			logFile.append("End: ---------------------------------------------------------" + "\n");
			logFile.close();
			
			// STEP 2: run umplificator
			System.out.println("STEP 1 FINISHED.");			
		// Run a java app in a separate system process
			//ProcessBuilder builder = new ProcessBuilder("java -jar /home/abdulaziz/Desktop/BerkeleyDb/umplificator.jar");		    
			//Process process = builder.start();

			if (doUmplification) {
				
				System.out.println("STEP 2: run umplificator for directory ("+dir+") ...");
				File umpiliDir = createDirectory(dir+"/../refactor_innerStatic/out_umplification");
				Process proc = Runtime.getRuntime().exec("java -jar /home/abdulaziz/Desktop/BerkeleyDb/umplificator.jar " + dir + " --path="+umpiliDir.getAbsolutePath());
				try 
				{
					int terminationNum = proc.waitFor();
					String line;
				  BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				  while ((line = input.readLine()) != null) {
				    System.out.println(line);
				  }
				  input.close();
				  if(terminationNum==0)
					System.out.println("STEP 2 Finished");
				  else
						System.out.println("STEP 2 has some errors.");


				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				// System.out.println(proc.getOutputStream().toString());
				// InputStream err = proc.getErrorStream();
			} 
				
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void writeFilesWithCommentedInnerStaticClasses(String originalFileFullName, String outputDirectory_String, BufferedWriter logFile) {

		try 
		{
			BufferedReader buffReader = new BufferedReader(new FileReader(originalFileFullName));
			
			File javaFileBeingRead_File = new File(originalFileFullName);
			//File outputDirectory_File = 
			createDirectory(outputDirectory_String);
			String line = "";
			boolean ecounterStatic = false;
			int open_bracket_count = 0;
			int close_bracket_count = 0;
			try 
			{
				String originalText = new String(Files.readAllBytes(Paths.get(originalFileFullName)), StandardCharsets.UTF_8);
				createDirectory(outputDirectory_String+"/originalFiles");
				String outputFullFileName = outputDirectory_String + "/originalFiles/"+javaFileBeingRead_File.getName();
				String newJavaFilecontent = "";
				String innerClassSegment = "// Original file:"+ originalFileFullName + "\n";
				innerClassSegment+= "// NAME_SPACE" + "\n";
				// parent class is 
				innerClassSegment += "class "+ stripExtension(javaFileBeingRead_File.getName())+" {" + "\n";  //OuterClass name
				
				boolean toWriteNewJavaFile = false;
				String classNamespace = "";
				while ((line = buffReader.readLine()) != null) 
				{
					if(line.matches("package.*.;.*"))
					{
						classNamespace = line.replace("package", "namespace");
						//System.out.println(classNamespace);
					}
		
					boolean innerClass =line.matches("(class|.*.class) [A-Z].*") && !line.contains(stripExtension(javaFileBeingRead_File.getName()));
					
					if (line.contains("static class") || innerClass) 
					{
						
						if (!(line.contains("{") && (line.replace("{", " ").matches(".*\\W"))))
							{
							System.out.println("This line contains class keyword: >>"+line);
							newJavaFilecontent += line + "\n"; 
							continue;
							
							}
						
						//REPLACE
						line = line.replace("public","").trim();
						line = line.replace("private","").trim();
						line = line.replace("final","").trim();
						line = line.replace("protected","").trim();

						

						//
						if(!line.contains("static"))
						{
							line = "inner "+line;
						}
						//
						ecounterStatic = true;
						open_bracket_count += countOccurrence(line, "{");
						close_bracket_count += countOccurrence(line, "}");
						// System.out.println("//"+line);
						newJavaFilecontent += "// START_OF_STATIC_CLASS \n";
						newJavaFilecontent += "// "+line + "\n";						
						toWriteNewJavaFile = true;
						innerClassSegment = innerClassSegment.replace("// NAME_SPACE", classNamespace);
						innerClassSegment += "  "+ line+"\n";
					} 
					else if (ecounterStatic) 
					{
						open_bracket_count += countOccurrence(line, "{");
						close_bracket_count += countOccurrence(line, "}");
						newJavaFilecontent += "// "+line + "\n";
						innerClassSegment += "  "+ line + "\n";

						if (close_bracket_count == open_bracket_count) { // reset
							ecounterStatic = false;
							open_bracket_count = 0;
							close_bracket_count = 0;
							newJavaFilecontent += "// END_OF_STATIC_CLASS \n";
							innerClassSegment += "}" + "\n"; // eclose the static class 

						}
					}
			//		else if ()
				//	{
					//	if (line.contains("{") && (line.replace("{", " ").matches(".*\\W")))
					//	{
						//	System.out.println(line);
						//	newJavaFilecontent += line + "\n"; 

						//}
						//else 
							///newJavaFilecontent += line + "\n"; 
						
					//}
					
					else
					{
						newJavaFilecontent += line + "\n"; 
						//skip lines do not belong to static class  
					}
				} // END while(...)
				
				buffReader.close(); // closing the file after reading 
				
				if(toWriteNewJavaFile)
				{
					writeToFile(newJavaFilecontent,logFile,originalFileFullName,originalFileFullName);// overwrite existing file
					
					writeToFile(originalText,null,outputFullFileName,originalFileFullName); // write a backup
					
					String umpleOutputDir = outputDirectory_String+"/ump_static/";
					createDirectory(umpleOutputDir);
					String umpFile = stripExtension(javaFileBeingRead_File.getName())+"_static.ump";
					writeToFile(innerClassSegment, null, umpleOutputDir+umpFile, originalFileFullName); // static classes only 
					
					
					//System.out.println("use "+umpFile+"; ");
					
					// write all use
					OutputStream outStream = new FileOutputStream(umpleOutputDir+"master.ump",true);
					outStream.write(("use "+umpFile+"; ").getBytes());
					outStream.write("\n".getBytes());
					outStream.close();
				}
				logFile.flush();
			}

			catch (IOException e) 
			{
				e.printStackTrace();
			}

		} 
		catch (FileNotFoundException e) {
			System.out.println("-----------------------------------------");
			e.printStackTrace();
		}

	}

	private static File createDirectory(String dir) {
		File directory = new File(dir);
		if (!directory.exists()) 
		{
			directory.mkdir();
		}
		return directory;
	}

	private static void writeToFile(String fileContent,BufferedWriter logFile ,String outputFullFileName, String file) throws IOException {
		
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFullFileName));
		if(bufferedWriter != null)
		{
			if(logFile != null)
			bufferedWriter.write("// Original file location:  "+file+"\n");
			bufferedWriter.write(fileContent );
			bufferedWriter.flush();
			bufferedWriter.close();
			if(logFile != null)
			logFile.append("" + file + "\n ");
		}
	}
	
	

	private static ArrayList<String> getAllFilesInDirectory(String dir) {

		ArrayList<String> files = new ArrayList<>();
		File f = new File(dir);
		File[] allSubFiles = f.listFiles();

		for (File file : allSubFiles) {
			if (file.isDirectory()) {
				files.addAll(getAllFilesInDirectory(file.getAbsolutePath()));
			} 
			else {
				files.add(file.getAbsolutePath());

			}
		}

		return files;
	}
	
	public static String stripExtension (String str) {
    if (str == null) 
    	return null;
    int pos = str.lastIndexOf(".");
    if (pos == -1)
    	return str;
    return str.substring(0, pos);
}

	public static int countOccurrence(String source, String sentence) {
		int occurrences = 0;
		if (source.contains(sentence)) {
			int withSentenceLength = source.length();
			int withoutSentenceLength = source.replace(sentence, "").length();
			occurrences = (withSentenceLength - withoutSentenceLength) / sentence.length();
		}
		return occurrences;
	}

}
