package org.apache.pdfbox.rendering;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

public class DocumentStructureExtractor {
	private PDDocument document;
	private Map<Integer, List<PDMarkedContent>> markedContentMap;
	
	public DocumentStructureExtractor(PDDocument document) {
		this.document = document;
		this.markedContentMap = new HashMap<Integer, List<PDMarkedContent>>();
	}
	
	public List<PDMarkedContent> extract(int pageIndex) throws IOException {
		if (this.markedContentMap.containsKey(pageIndex)) {
			return this.markedContentMap.get(pageIndex);
		}
		PDPage page = this.document.getPage(pageIndex);
		if (page == null) {
			return null;
		}
		
		List<PDMarkedContent> contentList = this.extractStructure(page);
		this.markedContentMap.put(pageIndex, contentList);
		return contentList;
	}
	
	public List<PDMarkedContent> extract(PDPage page) throws IOException {
		int pageIndex = this.document.getPages().indexOf(page);
		return this.extract(pageIndex);
	}
	
	public List<PDMarkedContent> extractStructure(PDPage page) throws IOException {
		PageStructureExtractor pageExtractor = new PageStructureExtractor(page);
		pageExtractor.extract();
		return pageExtractor.getMarkedContentList(); 
	}
}
