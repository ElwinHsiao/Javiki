package norg.javiki;

import java.io.File;

import android.content.Intent;
import android.net.Uri;

public class IntentBuilder {
	public static Intent buildAppInstallIntent(File filePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(filePath), "application/vnd.android.package-archive");
		return intent;
	}
	
	public static Intent buildMarketLinkIntent(String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setData(Uri.parse("market://details?id=" + packageName));
		return intent;
	}
}
