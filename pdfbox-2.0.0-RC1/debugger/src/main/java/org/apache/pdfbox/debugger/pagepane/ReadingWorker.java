package org.apache.pdfbox.debugger.pagepane;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.debugger.ui.tags.ReadingText;

public class ReadingWorker extends Thread {

	private ReadingEngine engine;
	
	private List<ReadingText> textQueue;
	
	public ReadingWorker() {
		try {
			this.engine = new ReadingEngineMaryTTS();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.textQueue = new ArrayList<ReadingText>();
	}
	
	synchronized public void add(ReadingText readingText) {
		this.textQueue.add(readingText);
		this.notifyAll();
	}
	
	public void stopReading() {
		try {
			this.engine.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.textQueue.clear();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				synchronized (this) {
					while (this.textQueue.isEmpty()) {
						this.wait();
					}
				}
			
				if (this.textQueue.size() > 0) {
					ReadingText readText = this.textQueue.remove(0);
					this.engine.setLocale(readText.getLocale());
					this.engine.readOut(readText.getText());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
