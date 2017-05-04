package io.github.mthli.Ninja.Utils;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonHelper {
	public static Object select(JSONObject node, String path, Object def){
		if (node == null) return def;
		if (path == null) return def;
		int pos = path.indexOf("/");
		String current = pos == -1 ? path : path.substring(0, pos);
		String next = pos == -1 ? null : path.substring(pos + 1);
		Iterator<?> keys = node.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            if (current.equals(key)){
	            Object val;
				try {
					val = node.get(key);
					if (next == null) return val;
		            if(val instanceof JSONObject){
		            	return select((JSONObject)val, next, def);
		            } else {
		            	return def;
		            }
				} catch (JSONException e) {
					//Logger.error(e);
				}
            }
        }
        return def;
	}
	public static String selectString(JSONObject node, String path, String def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toString(val);
	}
	public static Integer selectInteger(JSONObject node, String path, Integer def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toInteger(val);
	}
	public static Double selectDouble(JSONObject node, String path, Double def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toDouble(val);
	}

	public static boolean selectBoolean(JSONObject node, String path, boolean def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toBoolean(val);
	}

	public static JSONObject parse(String json) {
		if (json == null) return null;
		JSONTokener tokener = new JSONTokener(json);
		Object root;
		try {
			root = tokener.nextValue();
			return (JSONObject)root;
		} catch (Exception e) {
			//Logger.error(e);
			return null;
		}
	}

}
