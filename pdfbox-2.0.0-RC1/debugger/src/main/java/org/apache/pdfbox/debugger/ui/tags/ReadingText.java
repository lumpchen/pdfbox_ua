package org.apache.pdfbox.debugger.ui.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReadingText {

	private String text;
	private String lang;
	private Locale locale;
	
	public ReadingText(String text, String lang) {
		this.text = text;
		this.lang = lang;
	}
	
	public ReadingText(String text, Locale locale) {
		this.text = text;
		this.locale = locale;
		this.lang = this.locale.getLanguage();
	}
	
	public String getText() {
		return this.text;
	}
	
	public Locale getLocale() {
		if (this.locale == null) {
			if (this.lang.equalsIgnoreCase("en-us") || this.lang.equalsIgnoreCase("en")) {
				this.locale = Locale.US;
			} else if (this.lang.equalsIgnoreCase("en-gb")) {
				this.locale = Locale.UK;
			} else if (this.lang.toLowerCase().startsWith("fr")) {
				this.locale = Locale.FRENCH;
			} else if (this.lang.toLowerCase().startsWith("de") || this.lang.toLowerCase().startsWith("da")) {
				this.locale = Locale.GERMAN;
			} else {
				this.locale = new Locale(this.lang);
			}
		}
		return this.locale;
	}

	public String getLang() {
		return this.lang;
	}
	
	@Override
	public String toString() {
		return this.lang + " - (" + this.text + ")";
	}
	
	public static List<ReadingText> mergeTextByLang(List<ReadingText> textList) {
		if (textList == null || textList.isEmpty()) {
			return textList;
		}
		List<ReadingText> mergeList = new ArrayList<ReadingText>();
		for (ReadingText text : textList) {
			if (mergeList.isEmpty()) {
				mergeList.add(text);
				continue;
			}
			ReadingText last = mergeList.get(mergeList.size() - 1);
			if (text.getLocale().getLanguage().equalsIgnoreCase(last.getLocale().getLanguage())) {
				String textMerge = last.getText() + " " + text.getText();
				ReadingText merge = new ReadingText(textMerge, last.locale);
				mergeList.remove(last);
				mergeList.add(merge);
			} else {
				mergeList.add(text);
			}
		}
		return mergeList;
	}
}
