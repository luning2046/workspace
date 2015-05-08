package io.rong.imlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PushUtil {
	
	private static final String PUSHSERVICE_PACKAGE = "io.rong.imlib.PushService";
	private static final String PUSHPROCCESS_PACKAGE = "io.rong.push.service";
	private static final String FILENAME = "Android/rc_push_map.txt";
	
	
	/**
	 * 开启服务
	 * @param context
	 */
	public static void startPushSerive(Context context){
		if(processesIsAlive(context, PUSHPROCCESS_PACKAGE)){//如果存在相关service
			Log.d("service create","====================存在相关service正在运行==================");
		}else{
			context.startService(new Intent(context,PushService.class));
		}
	}
	
	/**
	 * 初始化appId与activity的action的映射     注意 此方法最好只执行一次
	 * @param context
	 * @param appId
	 * @param action
	 */
	public static void initAppIdActionMapping(Context context, String appId,int icon_R){
		String action = "io.rong.imkit.conversationList.action";
		StringBuilder  content = new StringBuilder();// appId+"="+action+"="+pacakgeName;
		String packageName = getAppName(context);
		content.append(appId).append("=").append(action).append("=").append(packageName).append("=").append(icon_R);
		File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
		
		if(file.exists()){
			String old_content = readFile(file);
			StringBuilder temp = new StringBuilder();
			temp.append(appId).append("=").append(action).append("=").append(packageName);
			if(old_content.contains(temp)){
				temp = null;
				return;
			}
			content.append(" ").append(old_content);
		}
		
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file);
			outStream.write(content.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(outStream!=null){
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param appId
	 */
	private static String readFile(File file){
		String content = "";
		try {
			FileInputStream in = new FileInputStream(file);
			byte[] buffer = new byte[1024*10];
			int len = in.read(buffer);
			content = new String(buffer,0,len);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	
	/**
	 * 获得appId对应的action的信息
	 * @param appId
	 */
	public static String getActionByAppid(String appId){
		
		String actionInfo = "";
		File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
		String allInfo = readFile(file);
		
		if(allInfo.contains(appId)){
			String[] keyValues = allInfo.split(" ");
			for(String keyValue : keyValues){
				if(keyValue.contains(appId)){
					actionInfo = keyValue.split("=")[1];
				}
			}
		}
		
		return actionInfo;
	}
	
	/**
	 * 获得appId对应的PacakgeName的信息
	 * @param appId
	 */
	public static String getPackageNameByAppid(String appId){
		
		String actionInfo = "";
		File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
		String allInfo = readFile(file);
		
		if(allInfo.contains(appId)){
			String[] keyValues = allInfo.split(" ");
			for(String keyValue : keyValues){
				if(keyValue.contains(appId)){
					actionInfo = keyValue.split("=")[2];
				}
			}
		}
		
		return actionInfo;
	}
	
	/**
	 * 获得appId对应的Icon_R的信息
	 * @param appId
	 */
	public static int getIconRByAppid(String appId){
		
		String iconR = "";
		File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
		String allInfo = readFile(file);
		
		if(allInfo.contains(appId)){
			String[] keyValues = allInfo.split(" ");
			for(String keyValue : keyValues){
				if(keyValue.contains(appId)){
					iconR = keyValue.split("=")[3];
				}
			}
		}
		
		return Integer.parseInt(iconR);
	}
	

	/**
	 * 删除指定packageName的mapping
	 * @param packageName
	 */
	public synchronized static void removeMappingByPacageName(String packageName){
		
		File file = new File(Environment.getExternalStorageDirectory(), FILENAME);
		String allInfo = readFile(file);
		
		if(allInfo.contains(packageName)){
			StringBuilder sb = new StringBuilder();
			String[] keyValues = allInfo.split(" ");
			for(String keyValue : keyValues){
				if(!keyValue.contains(packageName)){
					sb.append(keyValue).append(" ");
				}
			}
//			Log.d("afff","=============remove================="+sb.toString());
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(file);
				outStream.write(sb.toString().trim().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(outStream!=null){
					try {
						outStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	/**
	 * 判断指定service是否正在运行
	 * @param context
	 * @param servicePackage  如：com.feinno.pushserver.PushService
	 * @return
	 */
	private static boolean serviceIsAlive(Context context,String servicePackage) {

		ActivityManager manager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (servicePackage.equals(service.service.getClassName())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * 判断指定service是否正在运行
	 * @param context
	 * @param servicePackage  如：com.feinno.pushserver.PushService
	 * @return
	 */
	public static boolean processesIsAlive(Context context,String processPackage) {

		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		for(RunningAppProcessInfo ra : manager.getRunningAppProcesses()){
//			Log.d("aaaa","======================="+ra.processName);
			if (processPackage.equals(ra.processName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取本机mac地址
	 * @param context
	 * @return
	 */
	public static String getMac(Context context){
        // 获取wifi管理器
        WifiManager wifiMng = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfor = wifiMng.getConnectionInfo();
        return wifiInfor.getMacAddress();
	}
	
	/**
	 * 获取本app的信息
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getAppName(Context context){
    	String pkName = context.getPackageName();
		String versionName;
		try {
			versionName = context.getPackageManager().getPackageInfo(
						pkName, 0).versionName;
			int versionCode = context.getPackageManager()
					.getPackageInfo(pkName, 0).versionCode;
			context.getPackageManager().getApplicationIcon(pkName);
			Log.d("afff","===================getAppName================="+pkName+"=="+versionName+"=="+versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        return pkName;
	}
	
	/**
	 * 获取指定app的icon
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Drawable getAppIcon(Context context,String packageName){
		Drawable drawable = null;
       try {
    	   drawable = context.getPackageManager().getApplicationIcon(packageName);
       } catch (NameNotFoundException e) {
    	   e.printStackTrace();
       }
       return drawable;
	}
	
	/**
	 * 发起get请求
	 */
	public static void httpConnect(){
		//baseUrl             
		String baseUrl = "http://www.baidu.com";  
		  
		//将URL与参数拼接  
		HttpGet getMethod = new HttpGet(baseUrl);  
		              
		HttpClient httpClient = new DefaultHttpClient();  
		  
		try {  
		    HttpResponse response = httpClient.execute(getMethod); //发起GET请求  
		  
		    Log.i("", "=======resCode = " + response.getStatusLine().getStatusCode()); //获取响应码  
		    Log.i("", "=======result = " + EntityUtils.toString(response.getEntity(), "utf-8"));//获取服务器响应内容  
		} catch (ClientProtocolException e) {  
		    e.printStackTrace();  
		} catch (IOException e) {  
		    e.printStackTrace();  
		}  
	}

	/**
	 * 发起Post请求 POST方法。参数IdeviceId
	 */
	public static void httpPostConnect() {
		String url = "http://bj.rongcloud.net:9000/navipush.json";

		HttpPost httpPost = new HttpPost(url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("IdeviceId", "333"));

		HttpResponse httpResponse = null;
		try {
			// 设置httpPost请求参数
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			// System.out.println(httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				System.out.println("result:" + result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * {appId:"e0x9wycfx7flq",fromUserId:"10001",fromUserName:"我是谁",fromUserPo:"",
		title:"你收到一条新消息",content:"这是第3条测试消息",channelType:1,channelId:"",
		channelName:""}
	 * @param jstr
	 * @throws JSONException 
	 */
	public static Bundle parseJson(String jstr){
		Bundle bundle = new Bundle();
		JSONObject json;
		try {
			json = new JSONObject(jstr);
			bundle.putString("objectName",json.getString("objectName"));
			bundle.putString("appId", json.getString("appId"));
			bundle.putString("fromUserId", json.getString("fromUserId"));
			bundle.putString("fromUserName", json.getString("fromUserName"));
			bundle.putString("fromUserPo", json.getString("fromUserPo"));
			bundle.putString("title", json.getString("title"));
			bundle.putString("content", json.getString("content"));
			
			bundle.putString("channelType", json.getString("channelType"));
			bundle.putString("channelId", json.getString("channelId"));
			bundle.putString("channelName", json.getString("channelName"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bundle;
	}
	
	public  static String getDeviceId(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mDeviceId = mTelephonyManager.getDeviceId();
		Log.d("deviceId","============================="+mDeviceId);
		return mDeviceId;
	}
	
	
//	private static final String URL_NAVIPUSH = "http://nav.cn.rong.io/navipush.json";//线上
	private static final String URL_NAVIPUSH = "http://bj.rongcloud.net:9000/navipush.json";//线下
	/**
	 * 
	 * @param deviceId
	 * @return  {"code":200,"server":"bj.rongcloude.net:8085"}
	 */
	public static String httpPostConnect(String deviceId) {

		HttpPost httpPost = new HttpPost(URL_NAVIPUSH);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("deviceId", deviceId));

		HttpResponse httpResponse = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			Log.d("", "=======resCode================= " + httpResponse.getStatusLine().getStatusCode()); //获取响应码
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				Log.d("aaaa","==================result====:" + result);
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 
	 * @param jstr   {"code":200,"server":"bj.rongcloude.net:8085"}
	 * @return
	 */
	public static Map<String,String> parseServerIdAndPort(String jstr){
		JSONObject json;
		Map<String,String> serverIpAndPort = new HashMap<String, String>();
		try {
			json = new JSONObject(jstr);
			String val = json.getString("server");
			serverIpAndPort.put("ip",val.split(":")[0]);
			serverIpAndPort.put("port",val.split(":")[1]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return serverIpAndPort;
	}
	
	
//	private String encodeNickName(String str) {
//
//		String result = "";
//		String result2 = "";
//		String numlist = Base64.encodeToString(str.getBytes(),Base64.URL_SAFE);
//		result2 = numlist.substring(0, numlist.length() - 1); // 换行符号的去掉
//
//		// = 号的特殊处理！！！
//		result = result2.replaceAll("=", "");
//		return result;
//	}
//	
//	
//	private String decodeNickName(String encodeStr) {
//		if (TextUtils.isEmpty(encodeStr)) {
//			return null;
//		}
//
//		String result = "";
//		int start = 0;
//		String nickNamenumlist = encodeStr.substring(start);
//		if (TextUtils.isEmpty(nickNamenumlist)) {
//			return null;
//		}
//		try {
//			result = new String(Base64.decode(nickNamenumlist, Base64.URL_SAFE));
//		} catch (IllegalArgumentException e) {
//		}
//		return result;
//	}
	
}
