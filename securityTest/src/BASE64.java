import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * 
 *Base64 主要不是加密，它主要的用途是把一些特殊符号如（？，“）等转成普通字母和数字字符用于网络传输。 
		由于一些二进制字符在传输协议中属于控制字符，不能直接传送需要转换一下就可以了。类似于  URLEncoding 对url上的信息进行编码
		如 原值为"onead , ?好好呵呵"  加密后值为  "b25lYWQgLCA/5aW95aW95ZG15ZG1"
		http://wenku.baidu.com/link?url=atpYe02FZrliw-dmmngNN0xfobr7HN9-WAkhw7ICGAbp6HTCpK5VKrXulKFSzYn8G4Gc00RrDMQJsXgsjejc_HsJ69ukRLarAdsgw0eHBmK
 */
public class BASE64 {

	  /**  
     * 用base64算法进行加密  
     * @param str 需要加密的字符串  
     * @return base64加密后的结果  
     */    
    public static String encodeBase64String(String str) {    
        BASE64Encoder encoder =  new BASE64Encoder();    
        return encoder.encode(str.getBytes());    
    }    
        
    /**  
     * 用base64算法进行解密  
     * @param str 需要解密的字符串  
     * @return base64解密后的结果  
     * @throws IOException   
     */    
    public static String decodeBase64String(String str) throws IOException {    
        BASE64Decoder encoder =  new BASE64Decoder();    
        return new String(encoder.decodeBuffer(str));    
    }    
    public static void main(String[] args) throws IOException {    
        String user = "onead , ?好好呵呵";    
        System.out.println("原始字符串 " + user);    
        String base64Str = encodeBase64String(user);    
        System.out.println("Base64加密 " + base64Str);    
        System.out.println("Base64解密 " + decodeBase64String(base64Str));    
    }    
}
