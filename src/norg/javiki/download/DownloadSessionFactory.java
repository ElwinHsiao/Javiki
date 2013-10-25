package norg.javiki.download;

import android.util.Log;
import norg.javiki.ClassName;
import norg.javiki.download.DownloadManager.ForegroundHandler;

public class DownloadSessionFactory {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();

	private static final String HTTP_PREFIX = "http://";
	private static final String FTP_PREFIX = "ftp://";

	public static DownloadSession createNetworkSession(String url, ForegroundHandler handler) {
		if (url.startsWith(HTTP_PREFIX)) {
			Log.i(TAG, "in createNetworkSession: create http download session");
			return new HttpDownloadSession(url, handler);
		} else if (url.startsWith(FTP_PREFIX)) {
			// TODO:
		}
		
		Log.w(TAG, "in createNetworkSession: unknow scheme");
		return null;
	}
}
