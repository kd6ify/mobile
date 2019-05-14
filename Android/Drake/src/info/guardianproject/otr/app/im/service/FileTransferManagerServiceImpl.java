package info.guardianproject.otr.app.im.service;

import java.util.HashMap;

import info.guardianproject.otr.app.im.engine.EngineFileTransferManager;
import info.guardianproject.otr.app.im.engine.FileTransferListener;
import info.guardianproject.otr.app.im.engine.ImConnection;


import com.futureconcepts.drake.client.FileTransferParcel;
import com.futureconcepts.drake.client.FileTransferRequestParcel;
import com.futureconcepts.drake.client.IFileTransferListener;
import com.futureconcepts.drake.client.IFileTransferManager;
import com.futureconcepts.drake.client.Imps.FileTransfer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * manages file transfers for a client
 */

public class FileTransferManagerServiceImpl extends IFileTransferManager.Stub
{
	private static final String TAG = FileTransferManagerServiceImpl.class.getSimpleName();
    private StatusBarNotifier _statusBarNotifier;
    private ContentResolver _resolver;
    private EngineFileTransferManager _fileTransferManager;
    private MyFileTransferListener _fileTransferListener;
    private final RemoteCallbackList<IFileTransferListener> _remoteListeners = new RemoteCallbackList<IFileTransferListener>();
	private HashMap<String, Long> _streamIdToDbIdMap = new HashMap<String, Long>();
	private HashMap<Long, String> _dbIdToStreamIdMap = new HashMap<Long, String>();
    
    public FileTransferManagerServiceImpl(ImConnectionServiceImpl connection)
    {
        ImConnection connAdaptee = connection.getAdaptee();
        _fileTransferManager = connAdaptee.getFileTransferManager();
        _fileTransferListener = new MyFileTransferListener();
        _fileTransferManager.setFileTransferListener(_fileTransferListener);
        RemoteImService service = connection.getContext();
        _resolver = service.getContentResolver();
        _statusBarNotifier = service.getStatusBarNotifier();
    }

	@Override
	public void registerFileTransferListener(IFileTransferListener listener) throws RemoteException
	{
		_remoteListeners.register(listener);
	}

	@Override
	public void unregisterFileTransferListener(IFileTransferListener listener) throws RemoteException
	{
		_remoteListeners.unregister(listener);
	}

	@Override
	public void sendFile(String path) throws RemoteException
	{
	}

	@Override
	public void acceptFileTransferRequest(long dbid) throws RemoteException
	{
		Log.d(TAG, "acceptFileTransferRequest " + dbid);
		try
		{
			_fileTransferManager.acceptFileTransferRequest(_dbIdToStreamIdMap.get(dbid));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void rejectFileTransferRequest(long dbid) throws RemoteException
	{
		Log.d(TAG, "rejectFileTransferRequest " + dbid);
		try
		{
			_fileTransferManager.rejectFileTransferRequest(_dbIdToStreamIdMap.get(dbid));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    private final class MyFileTransferListener implements FileTransferListener
    {
		@Override
		public void onFileTransferRequest(FileTransferRequestParcel fileTransferRequestParcel)
		{
			Uri uri = insertRequestIntoDb(fileTransferRequestParcel);
			if (uri != null)
			{
				Long dbid = Long.valueOf(uri.getPathSegments().get(1));
				_streamIdToDbIdMap.put(fileTransferRequestParcel.getStreamID(), dbid);
				_dbIdToStreamIdMap.put(dbid, fileTransferRequestParcel.getStreamID());
	            int N = _remoteListeners.beginBroadcast();
	            for (int i = 0; i < N; i++)
	            {
	                IFileTransferListener listener = _remoteListeners.getBroadcastItem(i);
	                try
	                {
	                    listener.onFileTransferRequest(dbid);
	                }
	                catch (RemoteException e)
	                {
	                    // The RemoteCallbackList will take care of removing the
	                    // dead listeners.
	                }
	            }
	            _remoteListeners.finishBroadcast();
	            _statusBarNotifier.notifyFileTransferRequest(fileTransferRequestParcel, uri);
			}
        }

		@Override
		public void onFileTransferStatusChanged(FileTransferParcel fileTransferParcel)
		{
			String streamID = fileTransferParcel.getStreamID();
			if (_streamIdToDbIdMap.containsKey(streamID))
			{
				long dbid = _streamIdToDbIdMap.get(fileTransferParcel.getStreamID());
				Uri uri = Uri.withAppendedPath(FileTransfer.CONTENT_URI, String.valueOf(dbid));
				if (updateStatusInDb(uri, fileTransferParcel))
				{
		            int N = _remoteListeners.beginBroadcast();
		            for (int i = 0; i < N; i++)
		            {
		                IFileTransferListener listener = _remoteListeners.getBroadcastItem(i);
		                try
		                {
		                    listener.onFileTransferStatusChanged(dbid);
		                }
		                catch (RemoteException e)
		                {
		                    // The RemoteCallbackList will take care of removing the
		                    // dead listeners.
		                }
		            }
		            _remoteListeners.finishBroadcast();
				}
			}
		}
		
		private Uri insertRequestIntoDb(FileTransferRequestParcel request)
		{
			Uri result = null;
			try
			{
				ContentValues values = new ContentValues();
				values.put(FileTransfer.STREAM_ID, request.getStreamID());
				values.put(FileTransfer.PEER, request.getRequestor());
				values.put(FileTransfer.FILENAME, request.getFileName());
				values.put(FileTransfer.FILE_SIZE, request.getFileSize());
				values.put(FileTransfer.DESCRIPTION, request.getDescription());
				values.put(FileTransfer.MIME_TYPE, request.getMimeType());
				values.put(FileTransfer.STATUS, "pending");
				result = _resolver.insert(FileTransfer.CONTENT_URI, values);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}
		
		private boolean updateStatusInDb(Uri uri, FileTransferParcel fileTransferParcel)
		{
			boolean result = false;
			try
			{
				ContentValues values = new ContentValues();
				values.put(FileTransfer.STATUS, fileTransferParcel.getStatus());
				values.put(FileTransfer.FILE_PATH, fileTransferParcel.getFilePath());
				values.put(FileTransfer.AMOUNT_WRITTEN, fileTransferParcel.getAmountWritten());
				if (_resolver.update(uri, values, null, null) == 1)
				{
					result = true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}
    }
}
