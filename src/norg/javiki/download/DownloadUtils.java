package norg.javiki.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUtils {
	
	/**
	 * Download from HTTP, mustn't run in main thread, Caller need to apply permission themselves.
	 * @param url
	 * @return : null if there is some errors.
	 */
	public static InputStream download(String url) {
		try {
			URL mUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(5);
			conn.setReadTimeout(5);
			InputStream is = conn.getInputStream();
			return is;
//        conn.setInstanceFollowRedirects(false);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
