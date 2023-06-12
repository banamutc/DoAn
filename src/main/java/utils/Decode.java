package utils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public class Decode {

	public static String encode(String key, String data) {
	    try {
	        byte[] keySect = "nqQiVSgDMy809JoPF6OzP5OdBUB550Y4".getBytes();
	        HmacUtils hm256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, keySect);
	        //hm256 object can be used again and again
			return hm256.hmacHex(data);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	    
	    return null;
	}
}