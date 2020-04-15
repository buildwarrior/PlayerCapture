package net.buildwarrior.playercapture.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class URLContents {

	public static String getUrlContents(String spec) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(spec).openConnection().getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
			bufferedReader.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return stringBuilder.toString();
	}
}