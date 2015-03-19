package me.lorenzop.webauctionplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Language {

	protected final HashMap<String, String> langMap = new HashMap<String, String>();

	protected final List<String> words = new ArrayList<String>();

	protected final WebAuctionPlus plugin;
	private volatile boolean isOk = false;

	public Language(final WebAuctionPlus plugin) {
		this.plugin = plugin;
		loadKeys();
	}

	// load language yml
	public synchronized void loadLanguage(final String lang) {
		this.isOk = false;
		final String effectiveLang = (lang == null || lang.isEmpty() || lang.length() != 2) ? "en" : lang;
		if(effectiveLang.length() != 2)
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Language should only be 2 letters! "+effectiveLang);
		// load words file
		{
			final List<String> words = this.loadWordsFile(effectiveLang);
			if(words != null && !words.isEmpty()) {
				this.words.clear();
				this.words.addAll(words);
			} else {
				final List<String> words_en = this.loadWordsFile("en");
				if(words_en != null && !words_en.isEmpty()) {
					this.words.clear();
					this.words.addAll(words_en);
				} else {
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix+"Failed to load words file! "+effectiveLang);
					this.isOk = false;
					return;
				}
			}
		}
		// try loading language file
		this.loadLanguageFile(effectiveLang);
		if(this.isOk) return;
		if(!effectiveLang.equals("en")) {
			// failed to load, so load default en.yml
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Defaulting to en.yml");
			this.loadLanguageFile("en");
			if(this.isOk) return;
		}
		WebAuctionPlus.log.severe("Failed to load language! "+effectiveLang);
	}
	public boolean isOk() {return this.isOk;}

	private void loadLanguageFile(final String lang) {
		try {
			// load from plugins folder
			final File langFile = new File(this.plugin.getDataFolder().toString()+File.separator+"languages"+File.separator+lang+".yml");
			final FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
			// load defaults from jar
			final InputStream defaultLangStream = this.plugin.getClass().getClassLoader().getResourceAsStream("languages/"+lang+".yml");
			if(defaultLangStream == null && !langFile.exists()) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Language file not found: "+lang+".yml");
				return;
			}
			final InputStreamReader reader = new InputStreamReader(defaultLangStream);
			final YamlConfiguration defaultLangConfig = YamlConfiguration.loadConfiguration(reader);
			// copy defaults
			langConfig.setDefaults(defaultLangConfig);
			if(langFile.exists()) {
				if(langFile.canWrite()) {
					// save defaults
					langConfig.options().copyDefaults(true);
					langConfig.save(langFile);
				} else {
					WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"File is not writable! "+langFile.toString());
				}
			}
			// load language messages
			for(final String key : this.langMap.keySet()) {
				this.langMap.put(key, langConfig.getString(key));
			}
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Loaded language file "+lang+".yml");

		} catch(Exception e) {
			WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix+"Failed to load language file "+lang+".yml");
			e.printStackTrace();
			return;
		}
		this.isOk = true;
	}
	private List<String> loadWordsFile(final String lang) {
		// load from plugins folder
		try {
			final File wordsFile = new File(
					this.plugin.getDataFolder()+
					File.separator+"words.txt"
			);
			final List<String> words = Files.readAllLines(wordsFile.toPath());
			if(words != null && !words.isEmpty())
				return words;
		} catch (Exception ignore) {}
		// load from plugins words/ folder
		try {
			final File wordsFile = new File(
					this.plugin.getDataFolder()+
					File.separator+"words"+
					File.separator+lang+".txt"
			);
			final List<String> words = Files.readAllLines(wordsFile.toPath());
			if(words != null && !words.isEmpty())
				return words;
		} catch (Exception ignore) {}
		// load from jar
		try {
			final InputStream stream = this.plugin.getClass().getClassLoader().getResourceAsStream(
					"words/"+lang+".txt"
			);
			if(stream != null) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				final List<String> lines = new ArrayList<String>();
				while(true) {
					final String line = reader.readLine();
					if(line == null)
						break;
					lines.add(line);
				}
				if(!lines.isEmpty())
					return lines;
			}
		} catch (Exception ignore) {}
		WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix+"Failed to load words.txt file from jar!");
		return null;
	}

	public synchronized String getString(final String key) {
		if(!this.isOk) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"No language file has been loaded!");
		} else if(this.langMap.containsKey(key)) {
			String value = this.langMap.get(key);
			if(value!=null && !value.isEmpty())
				return value;
		}
		WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Language message not found: " + key);
		return "<<Message not found!>>";
	}

	private void loadKeys() {
		this.langMap.put("no_permission",               "");
		this.langMap.put("no_cheating",                 "");
		this.langMap.put("no_item_in_hand",             "");
		this.langMap.put("item_stack_stored",           "");
		this.langMap.put("got_mail",                    "");
		this.langMap.put("no_mail",                     "");
		this.langMap.put("inventory_full",              "");
		this.langMap.put("not_enough_money_pocket",     "");
		this.langMap.put("not_enough_money_account",    "");
		this.langMap.put("reloading",                   "");
		this.langMap.put("finished_reloading",          "");
		this.langMap.put("account_created",             "");
		this.langMap.put("password_changed",            "");
		this.langMap.put("password_cmd_changed",        "");
		this.langMap.put("password_generated",          "");
		this.langMap.put("temp_password_expired",      "");
		this.langMap.put("account_not_found",          "");
		this.langMap.put("created_shout_sign",         "");
		this.langMap.put("created_recent_sign",        "");
		this.langMap.put("created_deposit_sign",       "");
		this.langMap.put("created_withdraw_sign",      "");
		this.langMap.put("created_deposit_mail_sign",  "");
		this.langMap.put("created_withdraw_mail_sign", "");
		this.langMap.put("sign_removed",               "");
		this.langMap.put("invalid_sign",               "");
		this.langMap.put("mailbox_title",              "");
		this.langMap.put("mailbox_opened",             "");
		this.langMap.put("mailbox_closed",             "");
		this.langMap.put("please_wait",                "");
		this.langMap.put("removed_enchantments",       "");
	}


	public String generatePassword() {
		final StringBuilder pass = new StringBuilder();
		pass.append(getRandomWord(this.words));
		final Random rand = new Random(System.nanoTime());
		pass.append(rand.nextInt(90)+10);
		pass.append(getRandomWord(this.words));
		if(pass.length() >= 32) {
			for(int i=0; i<10; i++) {
				final String pass2 = generatePassword();
				if(pass2.length() < 32)
					return pass2;
			}
			return null;
		}
		return pass.toString();
	}


	private static String getRandomWord(final List<String> words) {
		if(words == null || words.isEmpty()) throw new NullPointerException();
		final Random rand = new Random(System.nanoTime());
		return words.get(rand.nextInt(words.size()));
	}


}
