package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RefactorInner {

	public static void main(String[] args) {

		String dir = new File("").getAbsolutePath();
		String outDir = dir + "/refactor_innerStatic_backup";
		if (args.length > 0)
		{
			dir = args[0];
			outDir = dir + "/refactor_innerStatic_backup/";
		}
		if (args.length > 1)
			outDir = args[1];

		BufferedWriter logFile = null;
		createDirectory(outDir);
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

		} catch (IOException e) {
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
				
				String staticClassSegment = "// Original file:"+ originalFileFullName + "\n";
				staticClassSegment+= "// NAME_SPACE" + "\n";
				staticClassSegment += "class "+ stripExtension(javaFileBeingRead_File.getName())+" {" + "\n";  //OuterClass name
				
				boolean toWriteNewJavaFile = false;
				String classNamespace = "";
				while ((line = buffReader.readLine()) != null) 
				{
					if(line.matches("package.*.;.*"))
					{
						classNamespace = line.replace("package", "namespace");
						//System.out.println(classNamespace);
					}
		
					
					if (line.contains("static class")) 
					{
						line = line.replace("public"," ");
						ecounterStatic = true;
						open_bracket_count += countOccurrence(line, "{");
						close_bracket_count += countOccurrence(line, "}");
						// System.out.println("//"+line);
						newJavaFilecontent += "// START_OF_STATIC_CLASS \n";
						newJavaFilecontent += "// "+line + "\n";						
						toWriteNewJavaFile = true;
						staticClassSegment = staticClassSegment.replace("// NAME_SPACE", classNamespace);
						staticClassSegment += "  "+ line+"\n";
					} 
					else if (ecounterStatic) 
					{
						open_bracket_count += countOccurrence(line, "{");
						close_bracket_count += countOccurrence(line, "}");
						newJavaFilecontent += "// "+line + "\n";
						staticClassSegment += "  "+ line + "\n";

						if (close_bracket_count == open_bracket_count) { // reset
							ecounterStatic = false;
							open_bracket_count = 0;
							close_bracket_count = 0;
							newJavaFilecontent += "// END_OF_STATIC_CLASS \n";
							staticClassSegment += "}" + "\n"; // eclose the static class 

						}
					} 
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
					
					String umpleOutputDir = outputDirectory_String+"/ump/";
					createDirectory(umpleOutputDir);
					String umpFile = stripExtension(javaFileBeingRead_File.getName())+"_static.ump";
					writeToFile(staticClassSegment, null, umpleOutputDir+umpFile, originalFileFullName); // static classes only 
					
					
					System.out.println("use "+umpFile+"; ");
					
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
