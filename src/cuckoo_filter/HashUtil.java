package cuckoo_filter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil
{
	public static byte[] rawSHA256(String input)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hValue = digest.digest(input.getBytes());
			return hValue;
		}
		catch( NoSuchAlgorithmException e )
		{
			System.err.println("algorithm SHA-256 is not found");
			return null;
		}
	};
	
	public static byte[] rawSHA256(Byte input)
	{
		return rawSHA256(Byte.toString(input));
	};
}
