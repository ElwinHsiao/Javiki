package norg.javiki.packageutils;

import java.io.File;

import android.content.Intent;
import android.net.Uri;

public class ApkUtils {

	/**
	 * Deprecated, use {@link norg.javiki.IntentBuilder.buildAppInstallIntent} instead
	 * @param filePath
	 * @return
	 */
	@Deprecated
	public static Intent buildAppInstallIntent(File filePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(filePath), "application/vnd.android.package-archive");
		return intent;
	}
	

}
