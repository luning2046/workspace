import static org.junit.Assert.*;  
import java.util.Map;   
import org.junit.Test;

//甲乙双方在获得对方公钥后可以对发送给对方的数据加密，同时也能对接收到的数据解密，达到了数据安全通信的目的
public class Type3_DH_test {

	@Test    
	public void test() throws Exception {   
		// 生成甲方密钥对儿    
		Map<String, Object> aKeyMap = Type3_DH.initKey();   
		String aPublicKey = Type3_DH.getPublicKey(aKeyMap);   
		String aPrivateKey = Type3_DH.getPrivateKey(aKeyMap);   
		System.err.println("甲方公钥:\r" + aPublicKey);   
		System.err.println("甲方私钥:\r" + aPrivateKey);   
		// 由甲方公钥产生本地密钥对儿    
		Map<String, Object> bKeyMap = Type3_DH.initKey(aPublicKey);   
		String bPublicKey = Type3_DH.getPublicKey(bKeyMap);   
		String bPrivateKey = Type3_DH.getPrivateKey(bKeyMap);   
		System.err.println("乙方公钥:\r" + bPublicKey);   
		System.err.println("乙方私钥:\r" + bPrivateKey);   
		String aInput = "abc ";    
		System.err.println("原文: " + aInput);   
		// 由甲方公钥，乙方私钥构建密文    
		byte[] aCode = Type3_DH.encrypt(aInput.getBytes(), aPublicKey,bPrivateKey);  
		// 由乙方公钥，甲方私钥解密    
		byte[] aDecode = Type3_DH.decrypt(aCode, bPublicKey, aPrivateKey);    
		String aOutput = (new String(aDecode));   
		System.err.println("解密: " + aOutput);     
		assertEquals(aInput, aOutput);  
		System.err.println(" ===============反过来加密解密============== ==== "); 
		String bInput = "def ";    
		System.err.println("原文: " + bInput);  
		// 由乙方公钥，甲方私钥构建密文    
		byte[] bCode = Type3_DH.encrypt(bInput.getBytes(), bPublicKey, aPrivateKey);   
		// 由甲方公钥，乙方私钥解密    
		byte[] bDecode = Type3_DH.decrypt(bCode, aPublicKey, bPrivateKey);  
		String bOutput = (new String(bDecode));   
		System.err.println("解密: " + bOutput);   
		assertEquals(bInput, bOutput);   
	}
}
