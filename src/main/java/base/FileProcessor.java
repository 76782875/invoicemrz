package base;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixReadTiff;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept.PIX;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import base.PDFPageContent;
import base.PDFTextCoordinates;
import base.Utils;
import base.PageRectangle;
import base.TessAPIWrapper;

public class FileProcessor {
	String TEMP = "/tmp";
	String TESSDATA = "/tmp"; 
	TessAPIWrapper api;
	
	public void close() {
		if (api != null){
			api.close();
		}
	}
	
	public FileProcessor(String temp, String tess){
		this.TEMP = temp;
		this.TESSDATA = tess;
		api = TessAPIWrapper.getInstance(TESSDATA);
		java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);
	}
	
		
	public List<DocPage> process(File file, String mimetype){
		
		if (!(mimetype.equals("application/pdf")
				|| mimetype.equals("image/tiff")
				))
			return null;
		
		String textContent = "";
		boolean isImage = false;
		BufferedImage[] inputImage = null;
		String suffix = "";
		
		PDFTextCoordinates coords = null;
		try {
			coords = new PDFTextCoordinates();
		} catch (IOException e4) {
			e4.printStackTrace();
			return null;
		}
		List<DocPage> content = new ArrayList<DocPage>();
		if (!mimetype.equals("application/pdf")){
			isImage = true;
			
			ImageInputStream is = null;
			try {
				is = ImageIO.createImageInputStream(file);
			} catch (IOException e3) {
	    		return null;
			}
	    	
	    	if (is == null){
	    		return null;
	    	}
	    	
	    	try {
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
				if (iterator == null || !iterator.hasNext()) {
		    		return null;
				}
				// We are just looking for the first reader compatible:
				ImageReader reader = (ImageReader) iterator.next();
				iterator = null;
				
				reader.setInput(is);
				int nbPages = 0;
				try {
					nbPages = reader.getNumImages(true);
				} catch (IOException e2) {
		    		return null;
				}

				inputImage = new BufferedImage[nbPages];
				for (int page_num = 0; page_num < nbPages; page_num++){
					try {
						inputImage[page_num] = reader.read(page_num);
					} catch (IOException e1) {
						return null;
					}
				}
			}catch (Exception ex){
				return null;
			}
		}
		
		if (mimetype.equals("application/pdf")){
			PDDocument document;
			try {
				document = PDDocument.load(file);
				
				if (document == null){
					return null;
				}
				
				List<PDPage> allPages = document.getDocumentCatalog().getAllPages();

				if (allPages == null || allPages.size() == 0){
					document.close();
					return null;
				}
				
				PDFTextStripper pdfStripper = new PDFTextStripper();
				pdfStripper.setEndPage(1);
				textContent = pdfStripper.getText(document);
				
				if (textContent == null){
					document.close();
					return null;
				}
				
				if (textContent != null && !textContent.isEmpty()){
					textContent = textContent.replaceAll("\n", " ").replaceAll("\r", " ").trim();
				}
				
				int numImages = 0;
				for (int i = 0; i < allPages.size() && i == 0; i++) {
					PDPage page = (PDPage) allPages.get(i);
			        PDResources resources = page.getResources();
			        
			        if (resources != null){
				        Map pageImages = resources.getImages();
				        
				        if (pageImages != null) {
				        	numImages += pageImages.size();
				        }
			        }
			        i++;
				}
				
				if (textContent.isEmpty() && numImages == 0){
					return null;
				}else if (textContent.isEmpty() && numImages == 1
						||
						textContent.isEmpty() && numImages > 1
						|| 
						((textContent.length() < 240) && numImages > 0)
						){
					isImage = true;
					if (textContent.isEmpty() && numImages == 1){
						inputImage = new BufferedImage[allPages.size()];
						for (int page_num = 0; page_num < allPages.size(); page_num++){
							PDPage page = (PDPage) allPages.get(page_num);
					        PDResources resources = page.getResources();
					        Map pageImages = resources.getImages();
					        String key = (String) pageImages.keySet().iterator().next();
				            PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
				            inputImage[page_num] = image.getRGBImage();
				            suffix = image.getSuffix();
						}
					}else{
						try{
							for (int page_num = 0; page_num < allPages.size(); page_num++){
								inputImage[page_num] = allPages.get(page_num).convertToImage(BufferedImage.TYPE_INT_RGB, 72);
							} 
						}catch (Exception ex){
							return null;
						}
		            }
				} 
				
				if (isImage == false){
					try {
						content.addAll(coords.getContents(document));
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
				
				document.close();
			}catch (IOException e) {
				return null;
			}
		}
		
		if (isImage == true && inputImage == null){
			return null;
		}
		
		if (isImage){
			for (int im = 0; im < inputImage.length; im++){
				BufferedImage resizedImage = inputImage[im];
				
				
				String tempImageFileName = TEMP + File.separator + file.getName() + ".tif";
				File tmpFile = new File(tempImageFileName);
				
				if (mimetype.equals("application/pdf")){
					try {
						ImageIO.write(resizedImage, (suffix.isEmpty() ? "png" : suffix), tmpFile);
						resizedImage = ImageIO.read(tmpFile);
					} catch (Exception e) {
						return null;
					}
				}else{
					int rw = inputImage[im].getWidth()*1;
					ResampleOp resampleOp = new ResampleOp(rw,(rw * inputImage[im].getHeight()) / inputImage[im].getWidth() );
					resampleOp.setFilter(ResampleFilters.getBSplineFilter()); 
					resizedImage = resampleOp.filter(inputImage[im], null);
				}
				resizedImage = Deskew.doIt(resizedImage);
				
				tempImageFileName = TEMP + File.separator + file.getName() + ".tif";
				tmpFile = new File(tempImageFileName);
				try {
					//DPI.saveGridImage(resizedImage, tmpFile);
					
					ImageIO.write(resizedImage, "tif", tmpFile);
				} catch (Exception e) {
					return null;
				}
				
				PIX pixImage = pixReadTiff(tempImageFileName, 0);
				try {
					FileUtils.forceDelete(new File(tempImageFileName));
				} catch (Exception e) {
					System.out.println("Can not delete " + tempImageFileName);
				}
				
				if (pixImage == null){
					return null;
	    		}
				
				if (api.getAPI() == null){
					return null;
				}
				
				//api.getAPI().Clear();
				api.getAPI().SetImage(pixImage);
				
				BytePointer outText = api.getAPI().GetHOCRText(0);
				String tessOutput = outText.getString();
				
				textContent = api.getAPI().GetUTF8Text().getString();
				
				if (textContent != null){
					textContent = textContent.replaceAll("\n", " ");
				}
		        
				pixDestroy(pixImage);
				if (outText != null){
				    outText.deallocate();
				}
				
				try {
					content.add(coords.getTIFFContents(im, tessOutput));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
			
		return content;
	}
	
	public List<DocPage> process(InputStream is, String mimetype){
		
		if (!(mimetype.equals("application/pdf")
				|| mimetype.equals("image/tiff")
				))
			return null;

		String textContent = "";
		boolean isImage = false;
		BufferedImage[] inputImage = null;
		String suffix = "";
		
		PDFTextCoordinates coords = null;
		try {
			coords = new PDFTextCoordinates();
		} catch (IOException e4) {
			e4.printStackTrace();
			return null;
		}
		List<DocPage> content = new ArrayList<DocPage>();
		if (!mimetype.equals("application/pdf")){
			isImage = true;
			
	    	if (is == null){
	    		return null;
	    	}
	    	
	    	try {
	    		ImageInputStream in = ImageIO.createImageInputStream(is);
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(in);
				if (iterator == null || !iterator.hasNext()) {
		    		return null;
				}
				// We are just looking for the first reader compatible:
				ImageReader reader = (ImageReader) iterator.next();
				iterator = null;
				
				reader.setInput(in);
				int nbPages = 0;
				try {
					nbPages = reader.getNumImages(true);
				} catch (IOException e2) {
		    		return null;
				}

				inputImage = new BufferedImage[nbPages];
				for (int page_num = 0; page_num < nbPages; page_num++){
					try {
						inputImage[page_num] = reader.read(page_num);
					} catch (IOException e1) {
						return null;
					}
				}
			}catch (Exception ex){
				return null;
			}
		}
		
		if (mimetype.equals("application/pdf")){
			PDDocument document;
			try {
				document = PDDocument.load(is);
				
				if (document == null){
					return null;
				}
				
				List<PDPage> allPages = document.getDocumentCatalog().getAllPages();

				if (allPages == null || allPages.size() == 0){
					document.close();
					return null;
				}
				
				PDFTextStripper pdfStripper = new PDFTextStripper();
				pdfStripper.setEndPage(1);
				textContent = pdfStripper.getText(document);
				
				if (textContent == null){
					document.close();
					return null;
				}
				
				if (textContent != null && !textContent.isEmpty()){
					textContent = textContent.replaceAll("\n", " ").replaceAll("\r", " ").trim();
				}
				
				int numImages = 0;
				for (int i = 0; i < allPages.size() && i == 0; i++) {
					PDPage page = (PDPage) allPages.get(i);
			        PDResources resources = page.getResources();
			        
			        if (resources != null){
				        Map pageImages = resources.getImages();
				        
				        if (pageImages != null) {
				        	numImages += pageImages.size();
				        }
			        }
			        i++;
				}
				
				if (textContent.isEmpty() && numImages == 0){
					return null;
				}else if (textContent.isEmpty() && numImages == 1
						||
						textContent.isEmpty() && numImages > 1
						|| 
						((textContent.length() < 240) && numImages > 0)
						){
					isImage = true;
					if (textContent.isEmpty() && numImages == 1){
						inputImage = new BufferedImage[allPages.size()];
						for (int page_num = 0; page_num < allPages.size(); page_num++){
							PDPage page = (PDPage) allPages.get(page_num);
					        PDResources resources = page.getResources();
					        Map pageImages = resources.getImages();
					        String key = (String) pageImages.keySet().iterator().next();
				            PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
				            inputImage[page_num] = image.getRGBImage();
				            suffix = image.getSuffix();
						}
					}else{
						try{
							for (int page_num = 0; page_num < allPages.size(); page_num++){
								inputImage[page_num] = allPages.get(page_num).convertToImage(BufferedImage.TYPE_INT_RGB, 72);
							} 
						}catch (Exception ex){
							return null;
						}
		            }
				} 
				
				if (isImage == false){
					try {
						content.addAll(coords.getContents(document));
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
				
				document.close();
			}catch (IOException e) {
				return null;
			}
		}
		
		if (isImage == true && inputImage == null){
			return null;
		}
		
		if (isImage){
			for (int im = 0; im < inputImage.length; im++){
				BufferedImage resizedImage = inputImage[im];
				
				
				String tempImageFileName = TEMP + File.separator + "diagtestitem" + System.currentTimeMillis() + ".tif";
				File tmpFile = new File(tempImageFileName);
				
				if (mimetype.equals("application/pdf")){
					try {
						ImageIO.write(resizedImage, (suffix.isEmpty() ? "png" : suffix), tmpFile);
						resizedImage = ImageIO.read(tmpFile);
					} catch (Exception e) {
						return null;
					}
				}else{
					int rw = inputImage[im].getWidth()*1;
					ResampleOp resampleOp = new ResampleOp(rw,(rw * inputImage[im].getHeight()) / inputImage[im].getWidth() );
					resampleOp.setFilter(ResampleFilters.getBSplineFilter()); 
					resizedImage = resampleOp.filter(inputImage[im], null);
				}
				resizedImage = Deskew.doIt(resizedImage);
				
				tempImageFileName = TEMP + File.separator + "diagtestitem" + System.currentTimeMillis() + ".tif";
				tmpFile = new File(tempImageFileName);
				try {
					//DPI.saveGridImage(resizedImage, tmpFile);
					
					ImageIO.write(resizedImage, "tif", tmpFile);
				} catch (Exception e) {
					return null;
				}
				
				PIX pixImage = pixReadTiff(tempImageFileName, 0);
				try {
					FileUtils.forceDelete(new File(tempImageFileName));
				} catch (Exception e) {
					System.out.println("Can not delete " + tempImageFileName);
				}
				
				if (pixImage == null){
					return null;
	    		}
				
				if (api.getAPI() == null){
					return null;
				}
				
				//api.getAPI().Clear();
				api.getAPI().SetImage(pixImage);
				
				BytePointer outText = api.getAPI().GetHOCRText(0);
				String tessOutput = outText.getString();
				
				textContent = api.getAPI().GetUTF8Text().getString();
				
				if (textContent != null){
					textContent = textContent.replaceAll("\n", " ");
				}
		        
				pixDestroy(pixImage);
				if (outText != null){
				    outText.deallocate();
				}
				
				try {
					content.add(coords.getTIFFContents(im, tessOutput));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
			
		return content;
	}
}
