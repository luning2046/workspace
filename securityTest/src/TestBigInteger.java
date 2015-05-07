import java.math.BigDecimal;

public class TestBigInteger {
	
	/**
	 * BigInteger的用法demo
	 * http://hbzwt.iteye.com/blog/983287
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder para = new StringBuilder();
		StringBuilder rightReusult = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			if (i == 500) {
				para = para.append(".3");
				rightReusult = rightReusult.append(".6");
			} else {
				para = para.append("3");
				rightReusult = rightReusult.append("6");
			}
		}

		BigDecimal bigDec = new BigDecimal(para.toString());
		BigDecimal two = new BigDecimal("2");

		System.out.println("para:                 " + para);
		System.out.println("bigInt.toString():    " + bigDec.toString());
		System.out.println("bigInt.multiply(two): " + bigDec.multiply(two));

//		Assert.assertEquals("Wrong: ", rightReusult.toString(), bigDec
//				.multiply(two).toString());

	}
}
