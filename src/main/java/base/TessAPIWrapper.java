package base;

import org.bytedeco.javacpp.tesseract.TessBaseAPI;

public class TessAPIWrapper {
	private static TessAPIWrapper instance = null;
	
	private TessBaseAPI api;
	private static String TESSDATA_PREFIX = "/tmp";
	
	private TessAPIWrapper() {
		api = new TessBaseAPI();
		if (api.Init(TESSDATA_PREFIX, "eng") != 0) {
			api = null;
			return;
		}
		api.SetPageSegMode(1);
	}

	public static TessAPIWrapper getInstance(String tess){
		TESSDATA_PREFIX = tess;
		if (instance == null){
			instance = new TessAPIWrapper();
        }
		return instance;
	}
	
	public TessBaseAPI getAPI(){
		return api;	
	}
	
	public void close(){
		api.close();
	}
}
