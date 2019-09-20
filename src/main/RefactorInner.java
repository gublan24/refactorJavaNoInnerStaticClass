package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RefactorInner {

	public static void main(String[] args) {
		String dir = "/home/abdulaziz/Desktop/BerkeleyDb/javaFiles/base_copy/com/sleepycat/bind/serial";
		String outDir = "" + new File(".").getAbsolutePath() + "/RefactorInner_output/";
		if (args.length > 0)
			dir = args[0];
		if (args.length > 1)
			outDir = args[1];

		ArrayList<String> fileNames = getAllFilesInDirectory(dir);
		for (String f : fileNames) {
			System.out.println(f);
		}

		if (fileNames.size() > 0) {
			for (String file : fileNames) {
				writeFilesWithCommentedInnerStaticClasses(file, outDir);

			}
		}

	}

	private static void writeFilesWithCommentedInnerStaticClasses(String file, String outDir) {
		
		try {
			
			BufferedReader buffReader = new BufferedReader(new FileReader(file));
			
			String line = "";
			boolean ecounterStatic = false;
			int open_bracket_count = 0;
			int close_bracket_count = 0;

			try 
			{
				while((line=buffReader.readLine()) != null)
				{
					if(line.contains("static class"))
					{
						ecounterStatic = true;
						int numberofOpenBracket= countOccurrence(line, "{");
						int numberofCloseBracket= countOccurrence(line, "}");
						open_bracket_count += numberofOpenBracket;
						close_bracket_count += numberofCloseBracket;
						System.out.println("//"+line);

						
					} else if (ecounterStatic)
					{
						int numberofOpenBracket= countOccurrence(line, "{");
						int numberofCloseBracket= countOccurrence(line, "}");
						open_bracket_count += numberofOpenBracket;
						close_bracket_count += numberofCloseBracket;
						System.out.println("//"+line);

						if(close_bracket_count == open_bracket_count)
						{ //reset 
							ecounterStatic = false;
							open_bracket_count = 0;
							close_bracket_count = 0;
							System.out.println("//"+line);
						}
					}
					else 
					{
						System.out.println(line); // do nothing 

					}
				}
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("-----------------------------------------");
			e.printStackTrace();
		}

	}

	private static ArrayList<String> getAllFilesInDirectory(String dir) {

		ArrayList<String> files = new ArrayList<>();
		File f = new File(dir);
		File[] allSubFiles = f.listFiles();

		for (File file : allSubFiles) {
			if (file.isDirectory()) {
				files.addAll(getAllFilesInDirectory(file.getAbsolutePath()));

			} else {
				files.add(file.getAbsolutePath());

			}
		}

		return files;
	}
	
	public static int countOccurrence(String source, String sentence) {
		int occurrences = 0;

    if (source.contains(sentence)) {
        int withSentenceLength    = source.length();
        int withoutSentenceLength = source.replace(sentence, "").length();
        occurrences = (withSentenceLength - withoutSentenceLength) / sentence.length();
    }

    return occurrences;
}

}
