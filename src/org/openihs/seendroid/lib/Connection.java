/*
 * Copyright (C) 2011, Valentin Lorentz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.openihs.seendroid.lib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Base64;

public class Connection {
	private String username, password;
	private String base_api_url = "https://seenthis.net/api";
	//private String base_api_url = "http://192.168.1.150:12345/api";

	
	public Connection(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse query(HttpRequestBase message) throws ClientProtocolException, IOException, UnknownHostException {
		// SSL fixes (javax.net.ssl.SSLPeerUnverifiedException: No peer certificate)
		// From http://www.virtualzone.de/2011-02-27/how-to-use-apache-httpclient-with-httpsssl-on-android/
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		
		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		 
		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
		
		// Real code:
		DefaultHttpClient client = new DefaultHttpClient(cm, params);
		
		HttpResponse response = client.execute(message);
		return response;
	}

	public HttpPost getHttpPost(String uri) {
		HttpPost post = new HttpPost(this.base_api_url + uri);
		this.addHeaders(post);
		post.addHeader("Content-type", "application/atom+xml;type=entry");
		return post;
	}
	public HttpPost getHttpPost(String uri, String data) {
		HttpPost post = this.getHttpPost(uri);
		try {
			
			StringEntity entity = new StringEntity(data, HTTP.UTF_8);
			post.setEntity(entity);
			return post;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public HttpPut getHttpPut(String uri) {
		HttpPut post = new HttpPut(this.base_api_url + uri);
		this.addHeaders(post);
		post.addHeader("Content-type", "application/atom+xml;type=entry");
		return post;
	}
	public HttpDelete getHttpDelete(String uri) {
		HttpDelete post = new HttpDelete(this.base_api_url + uri);
		this.addHeaders(post);
		post.addHeader("Content-type", "application/atom+xml;type=entry");
		return post;
	}
	public HttpGet getHttpGet(String uri) {
		HttpGet get = new HttpGet(this.base_api_url + uri);
		this.addHeaders(get);
		return get;
	}
	private void addHeaders(HttpMessage message) {
		String credentials = Base64.encodeToString((this.username + ":" + this.password).getBytes(),Base64.DEFAULT);
		message.addHeader("User-agent", "SeenDroid");
		message.addHeader("Authorization", "Basic " + credentials.substring(0, credentials.length() - 1));
	}
	
	public String getUsername() {
		return this.username;
	}
}
