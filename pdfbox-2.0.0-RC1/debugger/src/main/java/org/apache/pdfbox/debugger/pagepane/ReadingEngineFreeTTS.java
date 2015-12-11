package org.apache.pdfbox.debugger.pagepane;

import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.speech.Central;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import org.apache.pdfbox.debugger.ui.tags.MarkedContentNode;
import org.apache.pdfbox.pdmodel.PDPage;

public class ReadingEngineFreeTTS implements ReadingEngine {

	private String voiceName = "kevin16";

	private Synthesizer synthesizer;
	private PDPage page;
	private List<MarkedContentNode> pageMarkedContentList;

	static private String noSynthesizerMessage() {
		String message = "No synthesizer created.  This may be the result of any\n"
				+ "number of problems.  It's typically due to a missing\n"
				+ "\"speech.properties\" file that should be at either of\n"
				+ "these locations: \n\n";
		message += "user.home    : " + System.getProperty("user.home") + "\n";
		message += "java.home/lib: "
				+ System.getProperty("java.home")
				+ File.separator
				+ "lib\n\n"
				+ "Another cause of this problem might be corrupt or missing\n"
				+ "voice jar files in the freetts lib directory.  This problem\n"
				+ "also sometimes arises when the freetts.jar file is corrupt\n"
				+ "or missing.  Sorry about that.  Please check for these\n"
				+ "various conditions and then try again.\n";
		return message;
	}
	
	public ReadingEngineFreeTTS() {
		this.initSynthesizer();
	}

	public ReadingEngineFreeTTS(PDPage pdPage,
			List<MarkedContentNode> pageMarkedContentList) {
		this.page = pdPage;
		this.pageMarkedContentList = pageMarkedContentList;
		listAllVoices("general");
		this.initSynthesizer();
	}
	
	public static void main(String[] args) {
		listAllVoices("general");
	}

	public static void listAllVoices(String modeName) {

		System.out.println();
		System.out.println("All " + modeName
				+ " Mode JSAPI Synthesizers and Voices:");

		/*
		 * Create a template that tells JSAPI what kind of speech synthesizer we
		 * are interested in. In this case, we're just looking for a general
		 * domain synthesizer for US English.
		 */
		SynthesizerModeDesc required = new SynthesizerModeDesc(null, // engine
																		// name
				modeName, // mode name
				Locale.US, // locale
				null, // running
				null); // voices

		/*
		 * Contact the primary entry point for JSAPI, which is the Central
		 * class, to discover what synthesizers are available that match the
		 * template we defined above.
		 */
		EngineList engineList = Central.availableSynthesizers(required);
		for (int i = 0; i < engineList.size(); i++) {

			SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
			System.out.println("    " + desc.getEngineName() + " (mode="
					+ desc.getModeName() + ", locale=" + desc.getLocale()
					+ "):");
			Voice[] voices = desc.getVoices();
			for (int j = 0; j < voices.length; j++) {
				System.out.println("        " + voices[j].getName());
			}
		}
	}

	private void initSynthesizer() {
		/*
		 * Find a synthesizer that has the general domain voice we are looking
		 * for. NOTE: this uses the Central class of JSAPI to find a
		 * Synthesizer. The Central class expects to find a speech.properties
		 * file in user.home or java.home/lib.
		 * 
		 * If your situation doesn't allow you to set up a speech.properties
		 * file, you can circumvent the Central class and do a very non-JSAPI
		 * thing by talking to FreeTTSEngineCentral directly. See the
		 * WebStartClock demo for an example of how to do this.
		 */
		try {
			SynthesizerModeDesc desc = new SynthesizerModeDesc(null, // engine
																		// name
					"general", // mode name
					Locale.US, // locale
					null, // running
					null); // voice
			this.synthesizer = Central.createSynthesizer(desc);

			/*
			 * Just an informational message to guide users that didn't set up
			 * their speech.properties file.
			 */
			if (this.synthesizer == null) {
				System.err.println(noSynthesizerMessage());
				throw new RuntimeException(noSynthesizerMessage());
			}
			
			synthesizer.allocate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getText() {
		if (this.pageMarkedContentList == null
				|| pageMarkedContentList.isEmpty()) {
			return "";
		}
		StringBuilder text = new StringBuilder();
		for (MarkedContentNode mc : this.pageMarkedContentList) {
			if (mc.getContentString() != null) {
				text.append(mc.getContentString());

				text.append(" ");
			}
		}
		return text.toString();
	}

	public void speak() throws Exception {
		String text = this.getText();
		if (text.isEmpty()) {
			return;
		}
		this.speak(text);
	}

	public void speak(String text) throws Exception {
		/*
		 * Get the synthesizer ready to speak
		 */
		synthesizer.resume();

		/*
		 * Choose the voice.
		 */
		SynthesizerModeDesc desc = (SynthesizerModeDesc) synthesizer
				.getEngineModeDesc();
		Voice[] voices = desc.getVoices();
		Voice voice = null;
		for (int i = 0; i < voices.length; i++) {
			if (voices[i].getName().equals(voiceName)) {
				voice = voices[i];
				break;
			}
		}
		if (voice == null) {
			System.err.println("Synthesizer does not have a voice named "
					+ voiceName + ".");
		}
		synthesizer.getSynthesizerProperties().setVoice(voice);

		/*
		 * The the synthesizer to speak and wait for it to complete.
		 */
		synthesizer.speakPlainText(text, null);
		synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);

		/*
		 * Clean up and leave.
		 */
//		this.synthesizer.deallocate();
	}

	public void stop() throws Exception {
		this.synthesizer.cancel();
	}

	@Override
	public String[] getAvailableVoices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale[] getAvailableLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVoice(String voice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale locale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readOut(String text) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
