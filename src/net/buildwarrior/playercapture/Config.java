package net.buildwarrior.playercapture;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

	private File file;
	private FileConfiguration fileConfiguration;

	public Config(String path) {
		file = new File(path);

		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		fileConfiguration = YamlConfiguration.loadConfiguration(file);
	}

	public Config(File file) {
		this.file = file;
		fileConfiguration = YamlConfiguration.loadConfiguration(file);
	}

	public void saveConfig() {
		try {
			fileConfiguration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getData() {
		return fileConfiguration;
	}
}