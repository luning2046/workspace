import static org.junit.Assert.*;  
import org.junit.Before;
import org.junit.Test;   
import java.util.Map; 

/**
 * 简要总结一下，使用公钥加密、私钥解密，完成了乙方到甲方的一次数据传递，通过私钥加密、公钥解密，同时通过私钥签名、公钥验证签名，
 * 完成了一次甲方到乙方的数据传递与验证，两次数据传递完成一
	整套的数据交互！     类似数字签名，数字信封是这样描述的：    
	数字信封     数字信封用加密技术来保证只有特定的收信人才能阅读信的内容。  
	流程：       信息发送方采用对称密钥来加密信息，然后再用接收方的公钥来加密此对称密钥（这部分称为数字信封），
	再将它和信息一起发送给接收方；接收方先用相应的私钥打开数字信封，得到对称密钥，然后使用对称密钥再解开信息 
 *
 */
public class Type3_RSA_test {
	
	private String publicKey;   
	private String privateKey; 
	
	@Before    
	public void setUp() throws Exception {  
		Map<String, Object> keyMap = Type3_RSA.initKey();   
		publicKey = Type3_RSA.getPublicKey(keyMap);   
		privateKey = Type3_RSA.getPrivateKey(keyMap);   
		System.err.println("公钥: \n\r" + publicKey);   
		System.err.println("私钥： \n\r" + privateKey);   
	}  
	
	@Test   
	public void test() throws Exception {
		System.err.println("公钥加密——私钥解密");  
		String inputStr = "abc";    
		byte[] data = inputStr.getBytes();   
		byte[] encodedData = Type3_RSA.encryptByPublicKey(data, publicKey);   
		byte[] decodedData = Type3_RSA.decryptByPrivateKey(encodedData,privateKey);
		String outputStr = new String(decodedData);  
		System.err.println("加密前: " + inputStr + "\n\r" + "解密 后: " + outputStr);  
	    assertEquals(inputStr, outputStr);  
	} 
	
	@Test    
	public void testSign() throws Exception {   
		System.err.println("私钥加密——公钥解密");   
		String inputStr = "sign";    
		byte[] data = inputStr.getBytes();   
		byte[] encodedData = Type3_RSA.encryptByPrivateKey(data, privateKey);   
		byte[] decodedData = Type3_RSA.decryptByPublicKey(encodedData, publicKey); 
		String outputStr = new String(decodedData);    
		System.err.println("加密前: " + inputStr + "\n\r" + "解密 后: " + outputStr);    
		assertEquals(inputStr, outputStr);   
		System.err.println("私钥签名——公钥验证签名");   
		// 产生签名    
		String sign = Type3_RSA.sign(encodedData, privateKey);   
		System.err.println("签名:\r" + sign);   
		// 验证签名  
		boolean status = Type3_RSA.verify(encodedData, publicKey, sign);    
		System.err.println("状态:\r" + status);   
		assertTrue(status);   
	}

}
