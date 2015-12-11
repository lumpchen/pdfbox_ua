package org.apache.pdfbox.debugger.pagepane;

import java.util.Locale;

public interface ReadingEngine {

	public String[] getAvailableVoices();
	
	public Locale[] getAvailableLocales();
	
	public void setVoice(String voice);
	
	public void setLocale(Locale locale) throws Exception;
	
	public void readOut(String text) throws Exception;
	
	public void stop() throws Exception;
}
