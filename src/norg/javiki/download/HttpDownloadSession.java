package norg.javiki.download;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.WriteAbortedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpGet;

import norg.javiki.ClassName;
import norg.javiki.download.DownloadManager.ForegroundHandler;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class HttpDownloadSession extends DownloadSession {
	private static final String TAG = ClassName.CALLED_CLASS_NAME();

	private static final int MAX_REDIRECTS = 5;
	private static final int DEFAULT_TIMEOUT = 5000;
	
	private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	private static final int HTTP_TEMP_REDIRECT = 307;

	private static final int BUFFER_SIZE = 4096;
	private int mRedirectionCount;
	private URL mUrl;
	private long mTotalBytes;
	private int mCurrentBytes;
	private boolean mIsPaused;
	private boolean mIsCanceled;

	public HttpDownloadSession(String url, ForegroundHandler handler) {
		super(url, handler);
//		AndroidHttpClient httpclient = AndroidHttpClient.newInstance("");
//		HttpGet httpGet = new HttpGet(url);
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean canPause() {
		return true;
	}
	
	@Override
	protected void doPause() {
		mIsPaused = true;
	}
	
	@Override
	protected void doResume() {
		mIsPaused = false;
		notifyAll();
	}
	
	@Override
	protected void doCancel() {
		mIsCanceled = true;
	}
	
	@Override
	protected void doDownload() throws NetworkErrorException, TimeoutException, WriteAbortedException  {
        while (mRedirectionCount++ < MAX_REDIRECTS) {
            // Open connection and follow any redirects until we have a useful
            // response with body.
            HttpURLConnection conn = null;
            try {
//                if (!isNetEnable(null)) {
//                	
//                }
                conn = (HttpURLConnection) mUrl.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);

                final int responseCode = conn.getResponseCode();
                Log.i(TAG, "in doDownload: redirection=" + mRedirectionCount + " responseCode=" + responseCode);
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
//                        if (state.mContinuingDownload) {
//                            throw new StopRequestException(
//                                    STATUS_CANNOT_RESUME, "Expected partial, but received OK");
//                        }
                        processResponseHeaders(conn);
                        onStart(mTotalBytes);
                        transferData(conn);
                        return;

                    case HttpURLConnection.HTTP_PARTIAL:
//                        if (!state.mContinuingDownload) {
//                            throw new StopRequestException(
//                                    STATUS_CANNOT_RESUME, "Expected OK, but received partial");
//                        }
                        transferData(conn);
                        return;

                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                    case HttpURLConnection.HTTP_SEE_OTHER:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("Location");
                        mUrl = new URL(mUrl, location);
//                        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
//                            // Push updated URL back to database
//                            state.mRequestUri = state.mUrl.toString();
//                        }
                        continue;

                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
//                        throw new StopRequestException(
//                                STATUS_CANNOT_RESUME, "Requested range not satisfiable");

                    case HttpURLConnection.HTTP_UNAVAILABLE:
//                        parseRetryAfterHeaders(state, conn);
//                        throw new StopRequestException(
//                                HTTP_UNAVAILABLE, conn.getResponseMessage());

                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
//                        throw new StopRequestException(
//                                HTTP_INTERNAL_ERROR, conn.getResponseMessage());
                    	throw new NetworkErrorException("error code: " + responseCode);

//                    default:
//                        StopRequestException.throwUnhandledHttpError(
//                                responseCode, conn.getResponseMessage());
                }
            } catch (IOException e) {
                // Trouble with low-level sockets
//                throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);
            	// TODO: may storage error;
            	if (e.getCause() instanceof FileNotFoundException) {
            		throw new WriteAbortedException("storage error: " + e.getCause(), e);
            	} else {
            		throw new NetworkErrorException("network error: ");
            	}
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

//        throw new StopRequestException(STATUS_TOO_MANY_REDIRECTS, "Too many redirects");
        throw new NetworkErrorException("Too many redirects");
    }

	private void transferData(HttpURLConnection conn) throws WriteAbortedException, NetworkErrorException {
		Log.d(TAG, "in transferData");
        InputStream in = null;
        OutputStream out = null;
        FileDescriptor outFd = null;
		try {
			in = conn.getInputStream();
		} catch (IOException e) {
			// Log.d(TAG, "getInputStream error");
			throw new WriteAbortedException("getInputStream error", e);
		}

		try {
			out = new FileOutputStream(getFilePath(), false);
			outFd = ((FileOutputStream) out).getFD();
		} catch (IOException e) {
			// Log.d(TAG, "FileOutputStream error: " + "out==null " +
			// (out==null) + e);
			throw new WriteAbortedException("FileOutputStream error", e);
		}

        try {
            transferData(in, out);
        } finally {
        	try {
        		in.close();
        		out.flush();
        		outFd.sync();
        		out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
    }
	
    private void transferData(InputStream in, OutputStream out) throws NetworkErrorException, WriteAbortedException {
		Log.d(TAG, "in transferData2");

    	final byte data[] = new byte[BUFFER_SIZE];
        boolean goOn = true;
		while (true) {
//            int bytesRead = readFromResponse(state, data, in);
        	int bytesRead = -1;
            try {
            	bytesRead = in.read(data);
            } catch (IOException ex) {
            	throw new NetworkErrorException("read from network error");
            }
            
            if (bytesRead == -1) { // success, end of stream already reached
//                handleEndOfStream();
            	Log.i(TAG, "transferData: finished");
                return;
            }

//            state.mGotData = true;
//            writeDataToDestination(data, bytesRead, out);
            try {
                out.write(data, 0, bytesRead);
            } catch (IOException ex) {
            	throw new WriteAbortedException("write to disk error!", ex);
            }
            mCurrentBytes += bytesRead;
            onProgress(mCurrentBytes);

//            if (Constants.LOGVV) {
//                Log.v(Constants.TAG, "downloaded " + state.mCurrentBytes + " for "
//                      + mInfo.mUri);
//            }

            if (mIsCanceled) {
            	break;
            }
            
            try {
				pauseIfNeed();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new NetworkErrorException("thread interupted when wait");
			}
        }
    }

	
	private void pauseIfNeed() throws InterruptedException {
		if (mIsPaused) {
			wait();
		}
	}

//	private void reportProgress() {
//		onProgress(mCurrentBytes);
//	}

//	private void handleEndOfStream() {
//		// TODO Auto-generated method stub
//		
//	}

	private void processResponseHeaders(HttpURLConnection conn) {
//		state.mContentDisposition = conn.getHeaderField("Content-Disposition");
//        state.mContentLocation = conn.getHeaderField("Content-Location");

//        if (state.mMimeType == null) {
//            state.mMimeType = Intent.normalizeMimeType(conn.getContentType());
//        }

//        state.mHeaderETag = conn.getHeaderField("ETag");

        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        if (transferEncoding == null) {
			try {
				mTotalBytes = Long.parseLong(conn.getHeaderField("Content-Length"));
			} catch (NumberFormatException e) {
	        	mTotalBytes = -1;
			}
        } else {
            Log.i(TAG, "Ignoring Content-Length since Transfer-Encoding is also defined");
            mTotalBytes = -1;
        }


//        final boolean noSizeInfo = (mTotalBytes == -1)
//                && (transferEncoding == null || !transferEncoding.equalsIgnoreCase("chunked"));
//        if (!mInfo.mNoIntegrity && noSizeInfo) {
//            throw new StopRequestException(STATUS_CANNOT_RESUME,
//                    "can't know size of download, giving up");
//        }
	}

	public static boolean isNetEnable(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = connManager.getActiveNetworkInfo(); 
		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}
		
		return true;
	}

}
