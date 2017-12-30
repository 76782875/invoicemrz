package base;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MRZDataFinder {
	
	private static FileProcessor fileProcessor;
	private static Properties prop;
	
	private static final MRZDataFinder instance = 
			new MRZDataFinder();

	public static MRZDataFinder getInstance(){
        return instance;
    }
		
	private MRZDataFinder(){
		Properties prop = new Properties();
		prop.setProperty("temp_dir", "/tmp");
		prop.setProperty("tess_data", "/tmp");
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println("Found following settings:");
			System.out.println("Temporary Directory: " + prop.getProperty("temp_dir"));
			System.out.println("Tessdata: " + prop.getProperty("tess_data"));

		} catch (IOException ex) {
			System.out.println("Can not locate propertyfile. Run with default settings.");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.out.println("Can not locate propertyfile. Run with default settings.");
				}
			}
		}

		fileProcessor = new FileProcessor(prop.getProperty("temp_dir"), prop.getProperty("tess_data"));
	}
		
	public List<DocPage> processFile(InputStream stream, String mimetype){
		
    	if (!(mimetype.contains("image") || mimetype.equals("application/pdf"))){
    		return null;
    	}
		
		List<DocPage> result = fileProcessor.process(stream, mimetype);
			    
	    return result;
	}
	
	public List<String[]> getMRZLines(File file, String mimetype) throws Exception{
		InputStream stream = new FileInputStream(file);
		return getMRZLines(stream, mimetype);
	}
	
	public List<String[]> getMRZLines(InputStream stream, String mimetype) throws Exception{
		
		List<String[]> codes = new LinkedList<String[]>();
		
		if (stream != null)
	    {
			List<DocPage> result = processFile(stream, mimetype);
			
			if (result != null){
				for (DocPage page_result : result){
					List<DocLine> lines = new LinkedList<DocLine>();
					for (DocParagraph record : page_result.getLines()){
						for (DocLine line : record.getLines()){
							lines.add(line);
						}
					}
					Collections.sort(lines, new YLineComparator());
					
					if (lines.size() >= 2){
						String[] output = new String[]{"",""};
						output[0] = lines.get(lines.size() - 2).getText();
						output[1] = lines.get(lines.size() - 1).getText();
						codes.add(output);
					}
				}
			}
	    }
		
		return codes;
	} 
	
	
public static void main (String[] args) throws Exception{
	
	File dir = new File(args[0]);
	
	int fNum = 3;
	int counter = 1;
	for (File fl : dir.listFiles())
    {
    	if (!(fl.isFile() && (fl.getName().toLowerCase().endsWith("pdf")
    			|| fl.getName().toLowerCase().endsWith("png") 
    			|| fl.getName().toLowerCase().endsWith("jpg") 
    			|| fl.getName().toLowerCase().endsWith("jpeg") 
    			|| fl.getName().toLowerCase().endsWith("tif") 
    			|| fl.getName().toLowerCase().endsWith("tiff") 
    			
    			))){
    		continue;
    	} 
    	
    	if (counter > fNum){
    		break;
    	}
    	counter++;
    	
    	String mimetype = "image/tiff";
    	if (fl.getName().toLowerCase().endsWith("pdf")){
    		mimetype = "application/pdf";
    	}
		
    	System.out.println("File Name: " + fl.getName());
    	
    	List<String[]> codes = MRZDataFinder.getInstance().getMRZLines(fl, mimetype);
    	
    	for (String[] code : codes){
	    	System.out.println(code[0]);
	    	System.out.println(code[1]);
	    	
	    	if (MRZTextChecker.check(code[1])){
	    		MRZ mrz = MRZTextRetriever.retrieve(code[0], code[1]);
	    		System.out.println("Success");
	    		MRZPrinter.print(mrz);
	    	}
    	}
		//printWriter.println(lines[0]);
		//printWriter.println(lines[1]);
    }
	//printWriter.close();
	}
}
