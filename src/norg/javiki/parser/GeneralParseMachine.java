package norg.javiki.parser;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import norg.javiki.IdentityComputer;
import norg.javiki.download.DownloadUtils;


import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

public class GeneralParseMachine<Result> {
	private static final String TAG = "GeneralParser";

//	private boolean mNeedDownload;
	private ParseInterface<Result> mListener;
	private String mEncode = "UTF-8";
	private Parallelmode mParallelMode = Parallelmode.ONCE;
	private ParseTask mLastTask;
//	private Random mRandom = new Random();
	
	public static enum Parallelmode {
		ONCE,
		SINGLE,
		SERIAL,
		PARLLEL
	}
	
	public GeneralParseMachine () {
		mParallelMode = Parallelmode.ONCE;
	}
	
	/**
	 * 
	 * @param parallelMode : 
	 */
	public GeneralParseMachine (Parallelmode parallelMode) {
		mParallelMode = parallelMode;
	}
	public GeneralParseMachine<Result> setParserListener(ParseInterface<Result> listener) {
		mListener = listener;
		return this;
	}
	public GeneralParseMachine<Result> setEncode(String encode) {
		mEncode = encode;
		return this;
	}
	
//	public GeneralParser setParallelMode(int parallelMode) {
//		mParallelMode = parallelMode;
//		return this;
//	} 
	
	/**
	 * 
	 * @return The id of the current task, it doesn't make sense in ONCE mode.
	 */
	public long parse(InputStream inputStrem) {
		return parseInner(inputStrem);
	}
	
	public long parse(String url) {
		return parseInner(url);
	}
	
	private long parseInner(Object input) {
		long id =  IdentityComputer.computeId(input);
		verifyParameter();

		switch (mParallelMode) {
		case ONCE:
			if (mLastTask != null) {
				throw new IllegalStateException("The current parallel mode is ONCE, but you called parser() more than once.");
			}
			mLastTask = createTask(id, input);
			mLastTask.execute();
			break;
		case SINGLE:
			Executor singleExecutor = getExecutor();
			if (mLastTask != null && mLastTask.getStatus() != Status.FINISHED) {
				boolean isCanceled = mLastTask.cancel(true);
				Log.i(TAG, "cancel previous task, result=" + isCanceled);
			}
			
			mLastTask = createTask(id, input);
			mLastTask.executeOnExecutor(singleExecutor);
			break;
		case SERIAL:
			Executor serailExecutor = getExecutor();
			mLastTask = createTask(id, input);
			mLastTask.executeOnExecutor(serailExecutor);
			break;
		case PARLLEL:
			Executor parallelExecutor = getExecutor();
			mLastTask = createTask(id, input);
			mLastTask.executeOnExecutor(parallelExecutor);
			break;
		}

		return id;
	}

	private ParseTask createTask(long id, Object input) {
		if (input instanceof InputStream) {
			InputStream is = (InputStream) input;
			return new ParseTask(id, is);
		} else if (input instanceof String) {
			String url = (String) input;
			return new ParseTask(id, url);
		} else {
			throw new IllegalStateException("unsupport input type: " + input.getClass());
		}
	}

	private void verifyParameter() {
		String lint = "";
//		if (mInputStream == null && mUrl == null) {
//			lint = "Must give at least one kind of input, see setInput(...)";
//			throw new InvalidParameterException(lint);
//		}
		if (mListener == null) {
			lint = "Must give a ParseInterface instance by call setParserListener(...)";
			throw new InvalidParameterException(lint);
		}
	}

	private Executor mExecutor;
	private Executor getExecutor() {
		if (mExecutor == null) {
			switch (mParallelMode) {
			case ONCE:
				break;
			case SINGLE:
			case SERIAL:
				mExecutor = Executors.newSingleThreadExecutor();
				break;
			case PARLLEL:
				mExecutor = Executors.newCachedThreadPool();
//				throw new InvalidParameterException("Unsupport PARLLEL mode currently");
				break;
			}			
		}

		return mExecutor;
	}
	
	private void postResult(long id, Result result) {
		if (mParallelMode == Parallelmode.SINGLE && mLastTask.getId() == id) {
			Log.i(TAG, "Ignore previous task.");
			return;
		}
		mListener.done(id, result);
	}
	
	class ParseTask extends AsyncTask<Object, Integer, Result> {
		private long id;
		private InputStream inputStream;
		private String url;

		public ParseTask() {
			
		}

		public ParseTask(long id, String url) {
			this.id = id;
			this.url = url;
		}
		
		public ParseTask(long id, InputStream is) {
			this.id = id;
			this.inputStream = is;
		}
		
		public long getId() {
			return this.id;
		}
		
		@Override
		protected Result doInBackground(Object... params) {
			InputStream is = getInput();
			if (is == null) {
				Log.e(TAG, "get input stream error!");
				return null;
			}
			Result result = mListener.parse(is, mEncode);
			closeInput();
			return result;
		}
		


		@Override
		protected void onPostExecute(Result result) {
			postResult(this.id, result);
		}
		
		
		/**
		 * Must run in background.
		 * @return
		 */
		private InputStream getInput() {
			if (inputStream == null) {
				inputStream = DownloadUtils.download(this.url);
			}
			
			return inputStream;
		}
		private void closeInput() {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.w(TAG, "close input stream error: "+ e);
				}
				inputStream = null;
			}
		}
	}
	
//	interface OnPostResultListener<Result> {
//		void onBeforePostResult(int id, Result result);
//	}
	
//	/**
//	 * 
//	 * @return The id of the current task, it doesn't make sense in ONCE mode.
//	 */
//	@Deprecated
//	public long parse() {
////		Executor executor = generateTask();
//		// bg: is = getInput();
//		// bg: result = callback.parse(is, mEncode);
//		// bg: postResult(result);
//		verifyParameter();
//		
//		switch (mParallelMode) {
//		case ONCE:
//			if (mLastTask != null) {
//				throw new IllegalStateException("The current parallel mode is ONCE, but you called parser() more than once.");
//			}
//			mLastTask = new ParseTask();
//			mLastTask.execute();
//			break;
//		case SINGLE:
//			// TODO
//			break;
//		case SERIAL:
//			Executor sexecutor = getExecutor();
//			if (mLastTask != null && mLastTask.getStatus() != Status.FINISHED) {
//				mLastTask.cancel(true);
//			}
//			
//			mLastTask = new ParseTask();
//			mLastTask.executeOnExecutor(sexecutor);
//			break;
//		case PARLLEL:
//			// TODO
//			break;
//		}
//		
////		mInputStream.hashCode();
//		long id = mRandom.nextLong();
//		return id;
//	}


}
