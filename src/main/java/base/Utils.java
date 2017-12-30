package base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.pdfbox.io.IOUtils;

//http://esird1:8110/login-finder/login/auth
public class Utils {	
	public static InputStream getPdfAttachment(InputStream source) {
		
		try{
			Properties props = System.getProperties();
	
	        Session mailSession = Session.getDefaultInstance(props, null);
	        MimeMessage message = new MimeMessage(mailSession, source);
	
	        MimeMessageParser parser = new MimeMessageParser(message);
	        parser.parse();
	        List<DataSource> attachements = new LinkedList<DataSource>();
	        DataSource invoiceDataSource = null;
	        for(DataSource dataSource : parser.getAttachmentList()) {
			    if (StringUtils.isNotBlank(dataSource.getName())
			    		&& dataSource.getName().toLowerCase().endsWith("pdf")) {
			    	attachements.add(dataSource);
			    	if (dataSource.getName().toLowerCase().contains("invoice")){
			    		invoiceDataSource = dataSource;
			    	}
			    }
			}
	        
	        if (attachements.size() == 1){
	        	return attachements.get(0).getInputStream();
	        }else{
				if (invoiceDataSource != null){
					return invoiceDataSource.getInputStream();
				}
			}
        }catch (Exception ex){
        	return null;
        }
        
        return null;
	}
	
	public static String printList(List<String> lst){
		String result = "";
		
		if (lst == null || lst.isEmpty()){
			return result;
		}
		
		for (String l : lst){
			l = l.trim();
			
			if (l.isEmpty()){
				continue;
			}
			
			if (!result.isEmpty()){
				result = result + "||";
			}
			
			result = result + l;
		}
		
		return result;
	}
}
