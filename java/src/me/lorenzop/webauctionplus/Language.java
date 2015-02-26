package me.lorenzop.webauctionplus;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Language {

	protected HashMap<String, String> langMap = new HashMap<String, String>();
	protected FileConfiguration langConfig = null;

	protected final WebAuctionPlus plugin;
	private boolean isOk;

	public Language(final WebAuctionPlus plugin) {
		this.plugin = plugin;
		isOk = false;
		loadKeys();
	}

	// load language yml
	public synchronized void loadLanguage(final String lang) {
		isOk = false;
		final String effectiveLang = (lang == null || lang.isEmpty() || lang.length() != 2) ? "en" : lang;
		if(effectiveLang.length() != 2)
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Language should only be 2 letters! "+effectiveLang);
		// try loading language file
		loadLanguageFile(effectiveLang);
		if(isOk) return;
		if(!effectiveLang.equals("en")) {
			// failed to load, so load default en.yml
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Defaulting to en.yml");
			loadLanguageFile("en");
			if(isOk) return;
		}
		WebAuctionPlus.log.severe("Failed to load language! "+effectiveLang);
	}
	public boolean isOk() {return this.isOk;}

	private void loadLanguageFile(final String lang) {
		try {
			// load from plugins folder
			final File langFile = new File(plugin.getDataFolder().toString()+File.separator+"languages"+File.separator+lang+".yml");
			this.langConfig = YamlConfiguration.loadConfiguration(langFile);
			// load defaults from jar
			final InputStream defaultLangStream = plugin.getClass().getClassLoader().getResourceAsStream("languages/"+lang+".yml");
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
			for(final String key : langMap.keySet()) {
				langMap.put(key, langConfig.getString(key));
			}
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Loaded language file "+lang+".yml");

		} catch(Exception e) {
			WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix+"Failed to load language file "+lang+".yml");
			e.printStackTrace();
			return;
		}
		isOk = true;
	}

	public synchronized String getString(final String key) {
		if(!isOk) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"No language file has been loaded!");
		} else if(langMap.containsKey(key)) {
			String value = langMap.get(key);
			if(value!=null && !value.isEmpty())
				return value;
		}
		WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Language message not found: " + key);
		return "<<Message not found!>>";
	}

	private void loadKeys() {
		langMap.put("no_permission",				"");
		langMap.put("no_cheating",					"");
		langMap.put("no_item_in_hand",				"");
		langMap.put("item_stack_stored",			"");
		langMap.put("got_mail",						"");
		langMap.put("no_mail",						"");
		langMap.put("inventory_full",				"");
		langMap.put("not_enough_money_pocket",		"");
		langMap.put("not_enough_money_account",		"");
		langMap.put("reloading",					"");
		langMap.put("finished_reloading",			"");
		langMap.put("account_created",				"");
		langMap.put("password_changed",				"");
		langMap.put("account_not_found",			"");
		langMap.put("created_shout_sign",			"");
		langMap.put("created_recent_sign",			"");
		langMap.put("created_deposit_sign",			"");
		langMap.put("created_withdraw_sign",		"");
		langMap.put("created_deposit_mail_sign",	"");
		langMap.put("created_withdraw_mail_sign",	"");
		langMap.put("sign_removed",					"");
		langMap.put("invalid_sign",					"");
		langMap.put("mailbox_title",				"");
		langMap.put("mailbox_opened",				"");
		langMap.put("mailbox_closed",				"");
		langMap.put("please_wait",					"");
		langMap.put("removed_enchantments",			"");
	}

}
