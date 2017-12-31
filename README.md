# invoicemrz

Financial Documents usually are in one of the following formats
- Text PDF
- PDF scan
- Tiff

This code shows how to read text pdfs, pdf scans and tiff documents in uniform manner. It can easily be seen that regardless file format we return list of pages that contains paragraphs for each page, lines for each paragraph and words for each line. 

```java
List<DocPage> result = processFile(stream, mimetype);
```

That allows to consider two last lines as potential MRZ lines. Lines are returned per page to allow for the case when one document contains many different invoices.

Then we check potential lines of code for correctness. It is possible because of the check digits inside the code.

```java
	for (String[] code : codes){	    	
	    	if (MRZTextChecker.check(code[1])){
	    		MRZ mrz = MRZTextRetriever.retrieve(code[0], code[1]);
	    		System.out.println("Success");
	    		MRZPrinter.print(mrz);
	    	}
    	}
```

It is agurable whether it is the best approach. Best approach will be known when it is finally defined where and how mrz is placed inside an invoice page. 

The code relies on open source library [JavaCPP from ByteDeco](http://bytedeco.org/) and open source bundles for image analysis available from GeoTk(http://www.geotk.com/). 

JavaCPP allows to minimise deployment time and is simply convenient as JavaCPP takes care of using Tesseract OCR in different environments. GeoTk helps with image IO.

Pdf processing is done using [PDFBox](https://pdfbox.apache.org/).

