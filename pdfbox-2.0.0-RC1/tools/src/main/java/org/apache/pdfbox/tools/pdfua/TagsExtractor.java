package org.apache.pdfbox.tools.pdfua;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.rendering.DocumentStructureExtractor;

public class TagsExtractor {

	public static void main(String[] args) {
		File pdf = new File("c:/uatest/pdf_reference_1-7.pdf");
		try {
			extract(pdf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void extract(File pdf) throws IOException {
		PDDocument document = PDDocument.load(pdf);
		int n = document.getNumberOfPages();
		
		DocumentStructureExtractor extractor = new DocumentStructureExtractor(document);	
		for (int i = 0; i < n; i++) {
	        PDPage page = document.getPage(i);
	        
			List<PDMarkedContent> contents = extractor.extract(page);
			
			System.out.println(contents.size());
			for (PDMarkedContent content : contents) {
				System.out.println(content.toString());
			}
		}
	}
}
