package org.apache.pdfbox.debugger.pagepane;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;

public class ReadingEngineMaryTTS implements ReadingEngine {
	
	private MaryInterface marytts;
	private Set<String> voices;
	private Set<Locale> locales;
	private AudioPlayer player;
	
	private String voice;
	private Locale locale;
	
	private static Map<String, String> VOICE_MAP = new HashMap<String, String>();
	static 
	{
		VOICE_MAP.put(Locale.ENGLISH.getLanguage(), "dfki-obadiah");
		VOICE_MAP.put(Locale.FRENCH.getLanguage(), "upmc-jessica");
		VOICE_MAP.put(Locale.GERMANY.getLanguage(), "dfki-pavoque-styles");
	}
	
	public ReadingEngineMaryTTS() throws Exception {
		this.marytts = new LocalMaryInterface();
		this.voices = marytts.getAvailableVoices();
		this.locales = marytts.getAvailableLocales();
		
		this.marytts.setVoice(this.voices.iterator().next());
		this.marytts.setLocale(Locale.US);
	}
	
	@Override
	public void setVoice(String voice) {
		if (!this.voices.contains(voice)) {
			return;
		}
		if (voice.equals(this.voice)) {
			return;
		}
		this.voice = voice;
		this.marytts.setVoice(voice);
	}
	
	@Override
	public void setLocale(Locale locale) throws Exception {
//		if (!this.locales.contains(locale)) {
//			return;
//		}
		if (this.locale != null && locale.getLanguage().equals(this.locale.getLanguage())) {
			return;
		}
		this.locale = locale;
		this.marytts.setLocale(locale);
		
		String voice = VOICE_MAP.get(locale.getLanguage());
		if (voice == null) {
			throw new IllegalArgumentException("No voice for language: " + locale.toString());
		}
		this.setVoice(voice);
	}
	
	@Override
	public String[] getAvailableVoices() {
		if (this.voices != null) {
			return this.voices.toArray(new String[this.voices.size()]);
		}
		return new String[0];
	}

	@Override
	public Locale[] getAvailableLocales() {
		if (this.locales != null) {
			return this.locales.toArray(new Locale[this.locales.size()]);
		}
		return new Locale[0];
	}

	@Override
	public void readOut(String text) throws Exception {
		AudioInputStream audio = this.marytts.generateAudio(text);
		
		this.player = new AudioPlayer(audio);
		this.player.start();
		this.player.join();
	}

	@Override
	public void stop() throws Exception {
		if (this.player != null) {
			this.player.cancel();			
		}
	}
}
