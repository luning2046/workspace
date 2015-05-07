
import java.io.IOException;    
import java.math.BigInteger;    
import java.security.MessageDigest;    
import java.security.NoSuchAlgorithmException;    
    
/**  
 * 本例是  为字符串生成一个MD5码。MD5Util类是为文件生成MD5码
 * 应用场景  常用于文件校验
 * MD5码的使用过程是：
 * 		1 为一个文件生成一个MD5码
 * 		2 MD5码发给另一个人
 * 		3 之后这个人又得到了这个文件（这个文件可能被别人篡改了）
 *      4这个人得到这个文件后 在生成一遍MD5码  如果和那个人发给我的Md5码一致  说明这个文件没有被篡改，否则。。
 *      
 * 应用场景2  用户登录系统
	 * 用户的密码是以MD5（或其它类似的算法）经Hash运算后存储在文件系统中。当用户登录的时候，系统把用户输入的密码进行MD5 Hash运算，
	 * 然后再去和保存在文件系统中的MD5值进行比较，进而确定输入的密码是否正确。
	 * 通过这样的步骤，系统在并不知道用户密码的明码的情况下就可以确定用户登录系统的合法性。这可以避免用户的密码被具有系统管理员权限的用户知道
 * http://baike.baidu.com/link?url=_GaKP6kgv-xkqgU51JJ4Gz_Ci-MPOGWp1GfRSY3IzTXYTHj2YCwjf_zxPnvDsx5w
 * 
 * SHA方式类似于MD5  比Md5更安全
 * 
 * SHA是一种数据加密算法，该算法经过加密专家多年来的发展和改进已日益完善，现在已成为公认的最安全的散列算法之一，
 * 并被广泛使用。该算法的思想是接收一段明文，然后以一种不可逆的方式将它转换成一段（通常更小）密文，
 * 也可以简单的理解为取一串输入码（称为预映射或信息），
 * 并把它们转化为长度较短、位数固定的输出序列即散列值（也称为信息摘要或信息认证代码）的过程。
 * 散列函数值可以说时对明文的一种“指纹”或是“摘要”所以对散列值的数字签名就可以视为对此明文的数字签名
 * 
 * 
 * MD5、SHA、HMAC这三种加密算法，可谓是非可逆加密，就是不可解密的加密方法
 * 怎么理解这句话呢？就是  如源文字符串  为  “123sdf”  生成的md5密文“d253a31d67bb8beb7645b231c20b9fe9”
 *  不可逆就是  不能通过密文再翻译成成明文     只能是相同的多个原文  生成同一个密文
 *  也就是不能解密   如一个明文  通过一个算法生成密文   而这个密文再通过另一个特定的算法生成原文     这种算法就是可逆的   
 */    
public class MD5 {    
        
    /**  
     * 用MD5算法进行加密  
     * @param str 需要加密的字符串  
     * @return MD5加密后的结果  
     */    
    public static String encodeMD5String(String str) {    
        return encode(str, "MD5");    
    }    
    
    /**  
     * 用SHA算法进行加密  
     * @param str 需要加密的字符串  
     * @return SHA加密后的结果  
     */    
    public static String encodeSHAString(String str) {    
        return encode(str, "SHA");    
    }    
    
    //为一个字符串生成MD5码
    private static String encode(String str, String method) {    
        MessageDigest md = null;    
        String dstr = null;    
        try {    
            md = MessageDigest.getInstance(method);    
            md.update(str.getBytes());    
            dstr = new BigInteger(1, md.digest()).toString(16);    
        } catch (NoSuchAlgorithmException e) {    
            e.printStackTrace();    
        }    
        return dstr;    
    }    
    
    public static void main(String[] args) throws IOException {    
//        String user = "oneadmin好好呵呵";   
    	String user = "apache-tomcat-6.0.39-windows-x86.zip";
        System.out.println("原始字符串 " + user);    
        System.out.println("MD5加密 " + encodeMD5String(user));    
        System.out.println("SHA加密 " + encodeSHAString(user));    
    }    
}    
