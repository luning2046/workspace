import java.math.BigInteger;
import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *hmac主要应用在身份验证中，它的使用方法是这样的：
　　1. 客户端发出登录请求（假设是浏览器的GET请求）
　　2. 服务器返回一个随机值，并在会话中记录这个随机值
　　3. 客户端将该随机值作为密钥，用户密码进行hmac运算，然后提交给服务器
　　4. 服务器读取用户数据库中的用户密码和步骤2中发送的随机值做与客户端一样的hmac运算，然后与用户发送的结果比较，如果结果一致则验证用户合法
　　在这个过程中，可能遭到安全攻击的是服务器发送的随机值和用户发送的hmac结果，而对于截获了这两个值的黑客而言这两个值是没有意义的，
	绝无获取用户密码的可能性，随机值的引入使hmac只在当前会话中有效，大大增强了安全性和实用性。大多数的语言都实现了hmac算法，
	比如php的mhash、python的hmac.py、java的MessageDigest类，在web验证中使用hmac也是可行的，用js进行md5运算的速度也是比较快的。　 
 *http://www.baike.com/wiki/HMAC
 */
public class HMAC {

	public static final String KEY_SHA = "SHA";  
    public static final String KEY_MD5 = "MD5";
    /**
     * 
     * MAC算法可选以下多种算法  
     * HmacMD5   
     * HmacSHA1   
     * HmacSHA256   
     * HmacSHA384   
     * HmacSHA512  
     **/
    public static final String KEY_MAC = "HmacMD5";
    
    // BASE64解密
    public static byte[] decryptBASE64(String key)throws Exception{
    	return (new BASE64Decoder()).decodeBuffer(key);
    }
    
    /**   
     * BASE64加密  
     * @param key  
     * @return   
     * @throws Exception  
     * */
    public static String encryptBASE64(byte[] key) throws Exception {    
    	return (new BASE64Encoder()).encodeBuffer(key);
    } 
    
    /**
     * MD5加密  
     * @param data
     * @return 
     * @throws Exception 
     * */ 
    public static byte[] encryptMD5(byte[] data) throws Exception {
    	MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
    	md5.update(data);
    	return md5.digest();
    }
    /** 
     * SHA加密  
     * @param data 
     * @return
     * @throws Exception
     * */    
    public static byte[] encryptSHA(byte[] data) throws Exception {
    	MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
    	sha.update(data);
    	return sha.digest();
    }
    /** 
     * 初始化HMAC密钥 
	   @return
	   @throws Exception 
	 */   
    public static String initMacKey() throws Exception {
    	KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC); 
    	SecretKey secretKey = keyGenerator.generateKey(); 
    	return encryptBASE64(secretKey.getEncoded());   
   } 
    /**
     * HMAC加密  
     * @param data  
     * @param key  
     * @return 
     * @throws Exception*/    
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {   
    	SecretKey secretKey = new SecretKeySpec(decryptBASE64(key),KEY_MAC);  
    	Mac mac = Mac.getInstance(secretKey.getAlgorithm()); 
    	mac.init(secretKey);  
    	return mac.doFinal(data);
    }
    public static void main(String[] args) throws Exception{
    	String inputStr = "简单加密";
    	System.err.println("原文:\n" + inputStr);
    	byte[] inputData = inputStr.getBytes();   
    	String code = encryptBASE64(inputData); 
    	System.err.println("BASE64加密后:\n" + code);   
    	byte[] output = decryptBASE64(code);  
    	String outputStr = new String(output);   
    	System.err.println("BASE64解密后:\n" + outputStr); 
    	// 验证BASE64加密解密一致性    
//    	assertEquals(inputStr, outputStr);  
    	// 验证MD5对于同一内容加密是否一致   
//    	assertArrayEquals(encryptMD5(inputData),encryptMD5(inputData));
    	// 验证SHA对于同一内容加密是否一致  
//    	assertArrayEquals(encryptSHA(inputData),encryptSHA(inputData));  
    	String key = initMacKey();   
    	System.err.println("Mac密钥:\n" + key);  
    	// 验证HMAC对于同一内容，同一密钥加密是否一致   
//    	assertArrayEquals(encryptHMAC(inputData, key),encryptHMAC(inputData, key));   
    	BigInteger md5 = new BigInteger(encryptMD5(inputData));
    	System.err.println("MD5:\n" + md5.toString(16));  
    	BigInteger sha = new BigInteger(encryptSHA(inputData));  
    	System.err.println("SHA:\n" + sha.toString(32));   
    	BigInteger mac = new BigInteger(encryptHMAC(inputData,inputStr));   
    	System.err.println("HMAC:\n" + mac.toString(16)); 
	}
}
