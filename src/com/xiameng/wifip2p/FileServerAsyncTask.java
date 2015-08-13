package com.xiameng.wifip2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
	private static final String TAG = "FileServerAsyncTask";
	private Context context;

	public FileServerAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			ServerSocket serverSocket = new ServerSocket(8988);
			Socket client = serverSocket.accept();
			final File f = new File(Environment.getExternalStorageDirectory()
					+ "/" + "佳威p2p" + "/wifidirectp2p"
					+ System.currentTimeMillis() + ".jpg");
			// final File f = new File(
			// Environment.getExternalStorageDirectory()+ "/"
			// + context.getPackageName() +"/wifidirectp2p"+
			// System.currentTimeMillis() + ".mp3");

			File dirs = new File(f.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			f.createNewFile();
			InputStream inputstream = client.getInputStream();
			copyFile(inputstream, new FileOutputStream(f));
			serverSocket.close();
			return f.getAbsolutePath();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 打开传输文件
	 */
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + result), "image/*");
			context.startActivity(intent);
		}

	}

	public static boolean copyFile(InputStream inputStream, OutputStream out)

	{
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);

			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
