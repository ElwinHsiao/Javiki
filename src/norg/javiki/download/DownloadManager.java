package norg.javiki.download;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import norg.javiki.ClassName;
import norg.javiki.download.DownloadSession.TaskInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadManager {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();

	private static final File DESTINATION_DIRECTORY;
	private static ForegroundHandler sForegroundHandler;
//	private HashMap<String, OnDownloadListener> mOnDownloadListeners = new HashMap<String, DownloadManager.OnDownloadListener>();
//	private HashMap<String, DownloadSession> mDownloadSessions = new HashMap<String, DownloadSession>();
	private HashMap<String, DownloadTask> mDownloadTasks = new HashMap<String, DownloadTask>();

//	protected static ForegroundHandler getForegroundHandler() {
//		return sForegroundHandler;
//	}
	 public static final ExecutorService THREAD_POOL_EXECUTOR  = new ThreadPoolExecutor(5, 128, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
         private final AtomicInteger mCount = new AtomicInteger(1);

         public Thread newThread(Runnable r) {
             return new Thread(r, "DownloadThread #" + mCount.getAndIncrement());
         }
     });

	 
	static {
		DESTINATION_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		DESTINATION_DIRECTORY.mkdir();
	}

//	private OnDownloadListener mOnDownloadListener;

	public DownloadManager() {
		init();
	}
	
	public DownloadManager(String url) {
		init();
	}
	
	protected void init() {
		sForegroundHandler = new ForegroundHandler();
	}
	
	
	public void enqueueTask(final String url, OnDownloadListener listener) {
		DownloadTask downloadTask = new DownloadTask();
		mDownloadTasks.put(url, downloadTask);
		
		downloadTask.listener = listener;
		Future<?> future = THREAD_POOL_EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				downloadFile(url);
			}
		});
		downloadTask.future = future;
		
//		boolean isCanceled = future.cancel(true);
	}
	
	public boolean cancelTask(String url) {
		boolean result = mDownloadTasks.get(url).future.cancel(true);
		removeTask(url);
		return result;
	}
	
//	public void start() {
//		
//	}

//	public void setOnDownloadListener(OnDownloadListener listener) {
//		mOnDownloadListener = listener;
//		sForegroundHandler.setHandler(listener);
//	}
	
	private void downloadFile(String url) {
		DownloadSession session = getClient(url);
		mDownloadTasks.get(url).session = session;
		session.start();
	}
	
	private DownloadSession getClient(String url) {
		return DownloadSessionFactory.createNetworkSession(url, sForegroundHandler);
	}

	public interface OnDownloadListener {
		/**
		 * 
		 * @param totalSize : total size in Byte
		 */
		void onDownloadStart(String url, long totalSize);
		
		/**
		 * 
		 * @param currentSize : current fetched size in Byte
		 */
		void onDownloadProgress(String url, long currentSize);
		
		/**
		 * 
		 * @param filePath : full file path
		 */
		void onDownloadFinished(String url, File filePath);
		
		public static final int ERROR_NETWORK = ForegroundHandler.MSG_ERROR_CODE_NETWORK;
		public static final int ERROR_TIMEOUT = ForegroundHandler.MSG_ERROR_CODE_TIMEOUT;
		public static final int ERROR_STORAGE = ForegroundHandler.MSG_ERROR_CODE_STORAGE;
		
		void onDownloadFailed(String url, int error);
	}
	
	private void removeTask(String url) {
		mDownloadTasks.remove(url);
	}
	
	class ForegroundHandler extends Handler {
		public static final int MSG_START = 1;
		public static final int MSG_PROGRESS = 2;
		public static final int MSG_FINISH = 3;
		public static final int MSG_ERROR = 4;
		
		public static final int MSG_ERROR_CODE_NETWORK = 1;
		public static final int MSG_ERROR_CODE_TIMEOUT = 2;
		public static final int MSG_ERROR_CODE_STORAGE = 3;
//		private OnDownloadListener listener;
		
		public ForegroundHandler() {			
		}
		
//		public ForegroundHandler(OnDownloadListener listener) {
//			this.listener = listener;
//		}
//		
//		public void setHandler(OnDownloadListener listener) {
//			this.listener = listener;
//		}
		
		@Override
	    public void handleMessage(Message msg) {
			Log.i(TAG, "msg.what=" + msg.what);
			TaskInfo taskInfo = (TaskInfo) msg.obj;
			String url = taskInfo.getUrl();
			OnDownloadListener listener = mDownloadTasks.get(url).listener;
			
			switch (msg.what) {
			case MSG_START:
				if (listener != null) {
					listener.onDownloadStart(url, taskInfo.getTotalSize());
				}
				break;

			case MSG_PROGRESS:
				if (listener != null) {
					listener.onDownloadProgress(url, taskInfo.getCurrentSize());
				}
				break;

			case MSG_FINISH:
				if (listener != null) {
					listener.onDownloadFinished(url, taskInfo.getFilePath());
					removeTask(url);
				}
				break;

			case MSG_ERROR:
				if (listener != null) {
					listener.onDownloadFailed(url, msg.arg1);
				}
				break;
			}
	    }

	}
	
	static class DownloadTask {
		OnDownloadListener listener;
		DownloadSession session;
		Future<?> future;
	}
}
