package com.supermap.desktop.core.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.swing.event.EventListenerList;

import org.apache.http.HttpStatus;

import com.supermap.desktop.Application;
import com.supermap.desktop.utilties.StringUtilties;

public class HttpPostFile {
	private static final String POST = "POST";
	private static final int DEFAULT_TIMEOUT = 30000000; // 500分钟
	private static final int BUFFER_SIZE = 5120;
	private static final int DEFAULT_CHUNK_SIZE = 1024;

	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_CONTENT_LENGTH = "Content-Length";

	private String url = "";
	private String fileName = "";
	private String boundary = "";
	private boolean isCancel = false;

	private EventListenerList listenerList = new EventListenerList();

	public HttpPostFile(String url) throws UnsupportedEncodingException {
		this(url, "");
	}

	public HttpPostFile(String url, String fileName) throws UnsupportedEncodingException {

		// 很重要，必须要进行转码，请求以及请求体中不能直接识别空格等特殊字符
		this(url, fileName, URLEncoder.encode("------------------------" + new Date().toString(), "UTF-8"));
	}

	/**
	 * @param url
	 *            上传 api
	 * @param fileName
	 *            文件名（不带后缀）
	 * @param boundary
	 *            分隔符
	 */
	public HttpPostFile(String url, String fileName, String boundary) {
		this.url = url;
		this.fileName = fileName;
		this.boundary = boundary;
	}

	public String post(File file) {
		String result = "";
		FileInputStream fileInputStream = null;
		BufferedReader responseReader = null;

		try {
			long totalSize = file.length();
			if (StringUtilties.isNullOrEmpty(this.fileName)) {
				this.fileName = file.getName().split(".")[0];
			}

			StringBuilder prePostData = new StringBuilder();
			prePostData.append("--" + boundary); // 起始边界符
			prePostData.append(System.lineSeparator()); // 另起一行
			prePostData.append("Content-Disposition:form-data;name=\"file\";filename=\"");
			prePostData.append(this.fileName + ".zip\"");
			prePostData.append(System.lineSeparator()); // 另起一行
			prePostData.append("Content-Type:application/octet-stream");
			prePostData.append(System.lineSeparator());// 另起一行
			prePostData.append(System.lineSeparator());// 空一行
			// ----这里就填写二进制数据----
			// 然后是结束边界符
			String endBoundary = System.lineSeparator() + "--" + boundary + "--" + System.lineSeparator();

			// 初始化Http请求
			URL url = new URL(this.url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// 分块传输，很重要，必须要进行设置，否则大文件时会内存溢出
			connection.setChunkedStreamingMode(DEFAULT_CHUNK_SIZE);
			connection.setRequestMethod(POST);
			connection.setConnectTimeout(DEFAULT_TIMEOUT);
			connection.setRequestProperty(HEADER_CONTENT_TYPE, "multipart/form-data;boundary=" + boundary);
			connection.setRequestProperty(HEADER_CONTENT_LENGTH, String.valueOf(totalSize + prePostData.length() + endBoundary.length()));
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			long uploadOffset = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			// 写入起始边界符
			outputStream.writeBytes(prePostData.toString());

			// 写入数据
			fileInputStream = new FileInputStream(file);
			while (uploadOffset < totalSize) {
				if (this.isCancel) {
					break;
				}

				int realReadSize = fileInputStream.read(buffer, 0, BUFFER_SIZE);
				outputStream.write(buffer, 0, realReadSize);
				uploadOffset += realReadSize;
				outputStream.flush();
			}
			// 写入结束边界符
			outputStream.writeBytes(endBoundary);
			outputStream.flush();
			// 关闭请求流
			outputStream.close();

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_CREATED || responseCode == HttpStatus.SC_ACCEPTED) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					result += line;
					result += System.lineSeparator();
				}
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}

				if (responseReader != null) {
					responseReader.close();
				}
			} catch (IOException e) {
				Application.getActiveApplication().getOutput().output(e);
			}
		}
		return result;
	}

	public void addHttpPostListener(HttpPostListener listener) {
		this.listenerList.add(HttpPostListener.class, listener);
	}

	public void removeHttpPostListener(HttpPostListener listener) {
		this.listenerList.remove(HttpPostListener.class, listener);
	}

	protected void fireHttpPost(HttpPostEvent e) {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == HttpPostListener.class) {
				((HttpPostListener) listeners[i + 1]).httpPost(e);
			}
		}
		this.isCancel = e.isCancel();
	}
}
