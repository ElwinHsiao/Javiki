package norg.javiki.parser;

import java.io.InputStream;

public interface ParseInterface<Result> {
	public static final String DEFAULT_ENCODE = "UTF-8";
	Result parse(InputStream is, String encode);
	void done(Result result);
}