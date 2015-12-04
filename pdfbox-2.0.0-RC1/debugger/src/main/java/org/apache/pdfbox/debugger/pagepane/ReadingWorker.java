package org.apache.pdfbox.debugger.pagepane;

import javax.swing.SwingWorker;

public class ReadingWorker extends SwingWorker<Void, Integer> {

	private ReadingEngine engine;
	private String text;
	
	public ReadingWorker(ReadingEngine engine, String text) {
		this.engine = engine;
		this.text = text;
	}

	@Override
	protected Void doInBackground() throws Exception {
		this.engine.speak(this.text);
		return null;
	}
	
	public void stop() {
		try {
			this.engine.stop();
			this.cancel(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
