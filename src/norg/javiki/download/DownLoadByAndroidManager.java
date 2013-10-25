package norg.javiki.download;

import java.util.HashMap;

import norg.javiki.ClassName;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

public class DownLoadByAndroidManager {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();
	private Context mContext;
	private DownloadManager mDlm;
	private  HashMap<Long, String> mTask;
	private IntentFilter mBroadCastFilter;

	public DownLoadByAndroidManager(Context context) {
		mContext = context;
        mDlm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        mTask = new HashMap<Long, String>();
        mBroadCastFilter = new IntentFilter();
        mBroadCastFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);	//当下载结束时进行触发。
        mBroadCastFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);	//当点击一个正在下载的文件，如图所示 
	}
    
	public long startDownload(String url, String title){
		if (mTask.containsValue(url)) {	//如果正在下载
			return -1;
		}
		if (mTask.size() == 0) {	//第一个任务
			mContext.registerReceiver(mStatusReceiver, mBroadCastFilter);			
		}
		
//        Uri uri = Uri.parse("http://commonsware.com/misc/test.mp4");
		Uri uri = Uri.parse(url);
        //文件将存放在外部存储的确实download文件内，如果无此文件夹，创建之，如果有，下面将返回false。不同的手机不同Android版本的SD卡的挂载点可能会不一样，因此通过系统方式获取。
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir(); 

        //设置文件类型和文件名
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();  
//        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        String mimeString = mimeTypeMap.getMimeTypeFromExtension("apk");	//mimeString=application/vnd.android.package-archive
        String fileName = URLUtil.guessFileName(url, null, mimeString);
        Log.d(TAG, "mimeString=" + mimeString);
        
        //步骤2： 通过向下载服务发出enqueue()的请求，将放在下载队列中，通常会触发立即下载，并返回下载的ID号，根据这个号，可以查询相关的下载情况。分别设置请求的Uri
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);	//设置完成后在通知栏提示
        request.setTitle(title)       				//用于信息查看 
    	/*.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)*/
       	/*.setAllowedOverRoaming(false)*/           		//缺省是true，所以天价漫游数据费的产生 
       	.setDescription(fileName)  	//用于信息查看
       	.setMimeType(mimeString)
       	/*.setVisibleInDownloadsUi(false)*/
       	.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)	//若不设置， 则在/data/data/com.android.providers.download/cache/下, 文件名是自动用url最后一个斜杠后面的字符串
       	.allowScanningByMediaScanner();
        
        long downloadId = mDlm.enqueue(request);      
        mTask.put(downloadId, url);
       
        return downloadId;
    }

	public void cancelDownload(long... ids) {
		mDlm.remove(ids);
	}

//	public void close() {
//		mContext.unregisterReceiver(mStatusReceiver);
//	}
	
	public void openDldMgr(){ 
	    mContext.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
	}
	
    private BroadcastReceiver mStatusReceiver =  new BroadcastReceiver() {  
        public void onReceive(Context context, Intent intent) { 
        	if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {        		
            	long completedId = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
            	mTask.remove(completedId);
            	if (mTask.size() == 0) {	//没有任务了
            		mContext.unregisterReceiver(mStatusReceiver);
            	}
            	
            	Cursor cursor = mDlm.query(new DownloadManager.Query().setFilterById(completedId));
            	if (cursor == null || cursor.getCount() == 0) {
					Log.i(TAG, "in onReceive, canceled id=" + completedId);
					return;            		
            	}
            	cursor.moveToFirst();
            	Log.i(TAG, "in onReceive, complete, id=" + completedId);
            	
//            	queryStatus(completedId);            	
//            	try {
//                	queryStatus(completedId);					
//				} catch (CursorIndexOutOfBoundsException e) {
//					e.printStackTrace();
//					Log.i(TAG, "in onReceive, canceled id=" + completedId);
//					return;
//				}
            	
//            	Cursor c = mDlm.query(new DownloadManager.Query().setFilterById(completedId));
            	
//            	try {
//					ParcelFileDescriptor file = mDlm.openDownloadedFile(completedId);
//					FileDescriptor fileDscpt = file.getFileDescriptor();
//					new FileInputStream(fileDscpt);
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
            	
            	// TODO call callback
            	int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            	int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
            	String uri = null;
            	if (status == DownloadManager.STATUS_SUCCESSFUL) {
                	uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            	}
            	
            	if (mOnStatusChanged != null) {
            		mOnStatusChanged.onDownLoadCompleted(completedId, uri);
            	}
            	
        	} else {
            	long[] downLoaingIds = intent.getExtras().getLongArray(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            	
            	String ids = "";
            	for (int i = 0; i < downLoaingIds.length; ++i) {
            		ids += downLoaingIds[i] + ",";
            	}
            	Log.d(TAG, "in onReceive, notification clicked, ids=" + ids);
      		
            	// TODO call callback
            	if (mOnStatusChanged != null) {
            		mOnStatusChanged.onNotificationClicked(downLoaingIds);
            	}
        	}
        } 
    }; 
   
//    private DownloadManager.Query mQuery = new DownloadManager.Query();
    public Object queryById(long id, String columnName) {
    	Cursor c = mDlm.query(new DownloadManager.Query().setFilterById(id));
	    if(c == null || c.getCount() == 0){ 
	        Log.e(TAG, "Download not found!");
	        return null;
	    }
	    c.moveToFirst();
	    
	    int columnIndex = c.getColumnIndex(columnName);
	    if (c.getType(columnIndex) == Cursor.FIELD_TYPE_INTEGER) {
	    	return c.getLong(columnIndex);
	    } else if (c.getType(columnIndex) == Cursor.FIELD_TYPE_STRING) {
	    	return c.getString(columnIndex);
	    }	    
    	
    	return null;
    }
	
	public String queryStatus(long id){
	    //通过ID向下载管理查询下载情况，返回一个cursor 
	    Cursor c = mDlm.query(new DownloadManager.Query().setFilterById(id));
	    if(c == null || c.getCount() == 0){ 
	        Log.e(TAG, "Download not found!");
	        return null;
	    }
        c.moveToFirst(); 
	    
        //以下是从游标中进行信息提取
        String info = "getUriForDownloadedFile=" + mDlm.getUriForDownloadedFile(id) + "\n" +
        		"Column_id : " + 
                c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)) + "\n" +
                "Column_bytes_downloaded so far : " + 
                c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) + "\n" +
                "Column last modified timestamp : " + 
                c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)) + "\n" +
                "COLUMN_LOCAL_URI: " + 
                c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)) + "\n" +
                "COLUMN_LOCAL_FILENAME: " + 
                c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)) + "\n" +
                "Column statue : " + 
                c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) + "\n" +
                "Column reason : " + 
                c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));	  
	        
        Log.d(TAG, info);
        return info;
	} 
	
	private OnStatusChangedListener mOnStatusChanged;
	public void setOnStatusChangedListener(OnStatusChangedListener l) {
		this.mOnStatusChanged = l;
	}

	public interface OnStatusChangedListener {
		void onDownLoadCompleted(long id, String uri);
		void onNotificationClicked(long... ids);
	}
	
	private String statusMessage(Cursor c){  
	    switch(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))){ 
	    case DownloadManager.STATUS_FAILED:  
	        return "STATUS_FAILED";  
	    case DownloadManager.STATUS_PAUSED:  
	       return "STATUS_PAUSED";  
	    case DownloadManager.STATUS_PENDING:  
	        return "STATUS_PENDING";  
	    case DownloadManager.STATUS_RUNNING: 
	        return "STATUS_RUNNING";  
	    case DownloadManager.STATUS_SUCCESSFUL:  
	        return "STATUS_SUCCESSFUL";  
	    default: 
	        return "Unknown Information";  
	    } 
	}
}
