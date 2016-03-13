package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parserializer {
	/**
	 * Serializes list of file names to string
	 * @param list	list of file names
	 * @return	serialized string
	 */
	public static String serialize(List<String> list){
		String serializedString = "";
		
		// Join into single string with newline as separator
		for(String item : list){
			if(serializedString.length() > 0){
				serializedString += "\n";
			}
			
			serializedString += item;
		}
		
		return serializedString;
	}
	
	/**
	 * Parse string into list of file names
	 * @param serializedString	serialized string
	 * @return	list of file names
	 */
	public static List<String> parse(String serializedString){
		String[] parsedArray = null;
		List<String> parsedList = new ArrayList<>();
		
		// Split into list of strings (filenames) with newline as separator
		parsedArray = serializedString.split("\n");
		parsedList = Arrays.asList(parsedArray);
		
		return parsedList;
	}
}
