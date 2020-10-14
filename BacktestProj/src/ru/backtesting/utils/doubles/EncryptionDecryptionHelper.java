package ru.backtesting.utils.doubles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.UUID;

public class EncryptionDecryptionHelper  
{  
	private static EncryptionDecryptionHelper helper;
	
	private EncryptionDecryptionHelper() {
	}
	
	public static EncryptionDecryptionHelper instance() {
		if (helper == null) {
			helper = new EncryptionDecryptionHelper();
		}
		
		return helper;
	}
	
    public static void main(String args[ ])  
    {  
    	EncryptionDecryptionHelper helper = EncryptionDecryptionHelper.instance();
    	
    	String str = "tlt" + "__" + UUID.randomUUID().toString().substring(0, 3);
    	
    	System.out.println("Str = " + str);
    	
    	String encryptedText = helper.encodeBase64(str);
    	
        System.out.println("The encrypted string is: \n" + encryptedText);
                
        System.out.println("The decrypted  string is: \n" + helper.decodeBase64(encryptedText)); 
        
        assertEquals(str, helper.decodeBase64(encryptedText));
    }
    
    public String encodeBase64(String plain) {
    	   String b64encoded = Base64.getEncoder().encodeToString(plain.getBytes());

    	   // Reverse the string
    	   String reverse = new StringBuffer(b64encoded).reverse().toString();

    	   StringBuilder tmp = new StringBuilder();
    	   final int OFFSET = 4;
    	   for (int i = 0; i < reverse.length(); i++) {
    	      tmp.append((char)(reverse.charAt(i) + OFFSET));
    	   }
    	   return tmp.toString();
    }
    
    public String decodeBase64(String secret) {
    	  StringBuilder tmp = new StringBuilder();
    	   final int OFFSET = 4;
    	   for (int i = 0; i < secret.length(); i++) {
    	      tmp.append((char)(secret.charAt(i) - OFFSET));
    	   }

    	   String reversed = new StringBuffer(tmp.toString()).reverse().toString();
    	   return new String(Base64.getDecoder().decode(reversed));
    }
}