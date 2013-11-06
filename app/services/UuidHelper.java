package services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UuidHelper {
	public static UUID randomUUID(String name) {
		final UUID uuid = UUID.randomUUID();
		final long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		lsb |= 0xffffffffffffL;
		long md5_6 = md5(name) & 0xffffffffffffL;
		lsb ^= md5_6;
		return new UUID(msb, lsb);
	}

	public static long nameMd5(UUID uuid) {
		return uuid.getLeastSignificantBits() & 0xffffffffffffL;
	}

	public static String nameMd5String(UUID uuid) {
		return Long
				.toHexString(uuid.getLeastSignificantBits() & 0xffffffffffffL);
	}

	public static long md5(String s) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			throw new InternalError("MD5 not supported");
		}
		byte[] md5Bytes;
		try {
			md5Bytes = md.digest(s.getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("MD5 not supported");
		}
		long bits = 0L;
		for (int i = 0; i < md5Bytes.length; i++)
			bits = (bits << 8) | (md5Bytes[i] & 0xff);
		return bits;
	}

	public static String randomUUIDString(String name) {
		return randomUUID(name).toString();
	}
}
