package norg.javiki.parser;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import norg.javiki.download.DownloadUtils;


import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

public class GeneralParser<Result> {
	private static final String TAG = "GeneralParser";

	private InputStream mInputStream;
	private String mUrl;
//	private boolean mNeedDownload;
	private ParseInterface<Result> mListener;
	private String mEncode = "UTF-8";
	private Parallelmode mParallelMode = Parallelmode.ONCE;
	private ParseTask mParseTask;
	private Random mRandom = new Random();
	
	public static enum Parallelmode {
		ONCE,
		SINGLE,
		SERIAL,
		PARLLEL
	}
	
	public GeneralParser () {
		
	}
	
	/**
	 * 
	 * @param parallelMode : 
	 */
	public GeneralParser (Parallelmode parallelMode) {
		if (parallelMode == Parallelmode.PARLLEL) {	// TODO:
			throw new InvalidParameterException("This mode is unsupport currently!");
		}
		mParallelMode = parallelMode;
	}
	
	public GeneralParser<Result> setInput(String url) {
		// need download
		mUrl = url;
		return this;
	}
	public GeneralParser<Result> setInput(InputStream is) {
		mInputStream = is;
		return this;
	}
	public GeneralParser<Result> setParserListener(ParseInterface<Result> listener) {
		mListener = listener;
		return this;
	}
	public GeneralParser<Result> setEncode(String encode) {
		mEncode = encode;
		return this;
	}
	
//	public GeneralParser setParallelMode(int parallelMode) {
//		mParallelMode = parallelMode;
//		return this;
//	} 
	
	public long parse() {
//		Executor executor = generateTask();
		// bg: is = getInput();
		// bg: result = callback.parse(is, mEncode);
		// bg: postResult(result);
		verifyParameter();
		
		switch (mParallelMode) {
		case ONCE:
			if (mParseTask != null) {
				throw new IllegalStateException("The current parallel mode is ONCE, but you called parser() more than once.");
			}
			mParseTask = new ParseTask();
			mParseTask.execute();
			break;
		case SERIAL:
			Executor sexecutor = getExecutor();
			if (mParseTask != null && mParseTask.getStatus() != Status.FINISHED) {
				mParseTask.cancel(true);
			}
			
			mParseTask = new ParseTask();
			mParseTask.executeOnExecutor(sexecutor, 0);
			break;
		case PARLLEL:
			// TODO
			break;
		}
		
//		mInputStream.hashCode();
		long id = mRandom.nextLong();
		return id;
	}

	private void verifyParameter() {
		String lint = "";
		if (mInputStream == null && mUrl == null) {
			lint = "Must give at least one kind of input, see setInput(...)";
		}
		if (mListener == null) {
			lint = "Must give a ParseInterface instance by call setParserListener(...)";
		}
			
		throw new InvalidParameterException(lint);
	}

	private Executor mExecutor;
	private Executor getExecutor() {
		if (mExecutor == null) {
			switch (mParallelMode) {
			case SERIAL:
				mExecutor = Executors.newSingleThreadExecutor();
				break;
			case PARLLEL:
				// TODO
				throw new InvalidParameterException("This mode is unsupport currently!");
			}			
		}

		return mExecutor;
	}
	
	/**
	 * Must run in background.
	 * @return
	 */
	private InputStream getInput() {
		if (mInputStream == null) {
			mInputStream = DownloadUtils.download(mUrl);
		}
		
		return mInputStream;
	}
	private void closeInput() {
		if (mInputStream != null) {
			try {
				mInputStream.close();
			} catch (IOException e) {
				Log.w(TAG, "close input stream error: "+ e);
			}
			mInputStream = null;
		}
	}

	private void postResult(Result result) {
		// cancel
		mListener.done(result);
	}
	
	class ParseTask extends AsyncTask<Object, Integer, Result> {

		public ParseTask() {
			
		}
		
		public ParseTask(long id, String url) {
			
		}
		
		public ParseTask(long id, InputStream is) {
			
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
			postResult(result);
		}
	}
}
