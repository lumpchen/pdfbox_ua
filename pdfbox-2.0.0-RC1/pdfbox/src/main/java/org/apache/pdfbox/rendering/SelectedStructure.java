package org.apache.pdfbox.rendering;

import java.util.HashSet;
import java.util.Set;

public class SelectedStructure {

	private int pageIndex;
	private Set<Integer> mcids;
	
	public SelectedStructure(int pageIndex, Set<Integer> mcids) {
		this.pageIndex = pageIndex;
		this.mcids = mcids;
	}
	
	public SelectedStructure(int pageIndex, int mcid) {
		this.pageIndex = pageIndex;
		this.mcids = new HashSet<Integer>();
		this.mcids.add(mcid);
	}
	
	public boolean isSelected(int mcid) {
		if (this.mcids != null && this.mcids.contains(mcid)) {
			return true;
		}
		return false;
	}
	
	public int getPageIndex() {
		return this.pageIndex;
	}
	
}
