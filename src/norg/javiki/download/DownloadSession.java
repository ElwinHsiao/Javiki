package norg.javiki.download;

import java.io.File;
import java.io.WriteAbortedException;
import java.util.concurrent.TimeoutException;

import norg.javiki.ClassName;
import norg.javiki.download.DownloadManager.ForegroundHandler;
import android.accounts.NetworkErrorException;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

public abstract class DownloadSession {
	
	private static final String TAG = ClassName.CALLED_CLASS_NAME();
	public State mState;

	private ForegroundHandler mHandler;
	private TaskInfo mTaskInfo;

//	private OnDownloadListener mOnDownloadListener;
	
	public DownloadSession(String url, ForegroundHandler handler) {
		mTaskInfo = new TaskInfo();
		mTaskInfo.url = url;
		mHandler = handler;
	}
	
	public String getUrl() {
		return mTaskInfo.url;
	}
	
	public void setFilePath(File filePath) {
		mTaskInfo.filePath = filePath;
	}
	
	public File getFilePath() {
		if (mTaskInfo.filePath == null) {
			File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			String fileName = URLUtil.guessFileName(mTaskInfo.url, null, null);
			mTaskInfo.filePath = new File(directory, fileName);
		}
		return mTaskInfo.filePath;
	}
	
	public void setHandler(ForegroundHandler handler) {
		mHandler = handler;
	}
	
//	protected OnDownloadListener getOnDownloadListener() {
//		return mOnDownloadListener;
//	}
	
	public TaskInfo getTaskInfo() {
		return mTaskInfo;
	}
	
	public State getState() {
		return mState;
	}
	
	public void start() {
		try {
			doDownload();
		} catch (WriteAbortedException e) {
			e.printStackTrace();
			sendError(ForegroundHandler.MSG_ERROR_CODE_STORAGE);
			return;
		} catch (NetworkErrorException e) {
			e.printStackTrace();
			sendError(ForegroundHandler.MSG_ERROR_CODE_NETWORK);
			return;
		} catch (TimeoutException e) {
			e.printStackTrace();
			sendError(ForegroundHandler.MSG_ERROR_CODE_TIMEOUT);
			return;
		}
		
		updateState(State.FINISHED);
	}

	private void sendError(int errorCode) {
		updateState(State.FAILED);
		mHandler.obtainMessage(ForegroundHandler.MSG_ERROR, errorCode, 0, mTaskInfo).sendToTarget();
		mTaskInfo.filePath = null;
		mHandler.obtainMessage(ForegroundHandler.MSG_FINISH, mTaskInfo).sendToTarget();
	}
	
	public void pause() throws UnsupportedOperationException {
		doPause();
		updateState(State.PAUSED);
	}
	
	public void resume() throws UnsupportedOperationException {
		doPause();
		updateState(State.STARTED);
	}
	
	public void cancel() {
		doCancel();
		updateState(State.CANCELED);
	}
	
	public long getTotalSize() {
		return mTaskInfo.totalSize;
	}
	
//	public long getCurrentSize() {
//		return mCurrentSize;
//	}

	private void updateState(State newState) {
		Log.i(TAG, "state changed: " + mState + " -> " + newState);
		mState = newState;
		
		switch (newState) {
		case FINISHED:
			mHandler.obtainMessage(ForegroundHandler.MSG_FINISH, mTaskInfo).sendToTarget();;
			break;
		}
	}
	
	/**
	 * Called by derived class to transfer total size
	 * @param totalSize
	 */
	protected void onStart(long totalSize) {
		mTaskInfo.totalSize = totalSize;
		updateState(State.STARTED);
		mHandler.obtainMessage(ForegroundHandler.MSG_START, mTaskInfo).sendToTarget();;
	}
	
	/**
	 * Called by derived class to update progress 
	 * @param currentSize
	 */
	protected void onProgress(long currentSize) {
		mTaskInfo.currentSize = currentSize;
		mHandler.obtainMessage(ForegroundHandler.MSG_PROGRESS, mTaskInfo).sendToTarget();;
	}

//	protected abstract int getErrorCode();

	protected abstract void doDownload() throws NetworkErrorException,TimeoutException,WriteAbortedException;
	protected abstract void doPause() throws UnsupportedOperationException;
	protected abstract void doResume() throws UnsupportedOperationException;
	protected abstract void doCancel();
	
	public static class TaskInfo {
		private String url;
		private File filePath;
		private long currentSize;
		private long totalSize;
		
		public String getUrl() {
			return url;
		}

		public File getFilePath() {
			return filePath;
		}

		public long getCurrentSize() {
			return currentSize;
		}

		public long getTotalSize() {
			return totalSize;
		}

	}
	
	static enum State {
		STARTED,
		FINISHED,
		PAUSED,
		CANCELED,
		FAILED
	}
}
