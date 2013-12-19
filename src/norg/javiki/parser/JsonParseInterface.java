package norg.javiki.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.util.JsonReader;
import android.util.Log;

public abstract class JsonParseInterface<Result> implements ParseInterface<Result> {
	private static final String TAG = "JsonParseInterface";
	final public Result parse(InputStream is, String encode) {
		if (encode == null) encode = DEFAULT_ENCODE;
		JsonReader reader = null;
		try {
			reader = new JsonReader(new InputStreamReader(is, encode));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "input stream error: " + e);
			return null;
		}
		return parse(reader);
	}
	protected abstract Result parse(JsonReader reader);
	//inherited: void done(Result result);
}