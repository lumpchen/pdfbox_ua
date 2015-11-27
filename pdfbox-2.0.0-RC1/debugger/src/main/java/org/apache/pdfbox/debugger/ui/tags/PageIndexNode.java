package org.apache.pdfbox.debugger.ui.tags;

public class PageIndexNode {
	
	private boolean isRoot = false;
	private int pageIndex; // start from 0
	
	public PageIndexNode() {
		this.isRoot = true;
		this.pageIndex = -1;
	}
	
	public PageIndexNode(int pageNo) {
		this.isRoot = false;
		this.pageIndex = pageNo;
	}
	
	public boolean isRoot() {
		return this.isRoot;
	}
	
	public int getIndex() {
		return this.pageIndex;
	}
	
	@Override
	public String toString() {
		if (this.isRoot) {
			return "Pages";
		}
		return "Page " + (this.pageIndex + 1);
	}
}
