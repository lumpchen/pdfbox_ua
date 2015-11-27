package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

public class PDParentTreeItem implements COSObjectable {

	private COSBase inside;
	private List<COSBase> itemList;
	
	public PDParentTreeItem(COSArray numTreeValue) {
		this.inside = numTreeValue;
		
		int size = ((COSArray) numTreeValue).size();
		this.itemList = new ArrayList<COSBase>(size);
		for (int i = 0; i < size; i++) {
			COSBase val = ((COSArray) numTreeValue).get(i);
			this.itemList.add(val);
		}
	}
	
	public PDParentTreeItem(COSBase base) {
		this.inside = base;
		this.itemList = new ArrayList<COSBase>(1);
		this.itemList.add(base);
	}
	
	public PDParentTreeItem(COSDictionary dict) {
		this((COSBase) dict);
	}
	
	@Override
	public COSBase getCOSObject() {
		return this.inside;
	}

	public COSBase getValue(int index) {
		if (index < 0 || index >= this.itemList.size()) {
			// throw exception
			return null;
		}
		return this.itemList.get(index);
	}
	
	public int size() {
		return this.itemList.size();
	}
}
 