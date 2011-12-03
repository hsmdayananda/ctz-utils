package ru.concerteza.util.crypto;

import org.junit.Test;

import static org.apache.commons.lang.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;

/**
 * User: alexey
 * Date: 12/11/10
 */
public class SecurityUtilTest {
    private static final String AES_KEY = "One Ring to rule them all, .....";

    @Test
    public void testEncryptDecryptAESLong() {
        String secret = random(42001);
        String encrypted = SecurityUtils.encryptAES(secret, AES_KEY);
        String decrypted = SecurityUtils.decryptAES(encrypted, AES_KEY);
        assertEquals("decrypted not equals", secret, decrypted);
    }

    @Test
    public void testEncryptDecryptAESShort() {
        String secret = "foo";
        String encrypted = SecurityUtils.encryptAES(secret, AES_KEY);
        String decrypted = SecurityUtils.decryptAES(encrypted, AES_KEY);
        assertEquals("decrypted not equals", secret, decrypted);
    }

    @Test
    public void testSha1Sum() {
        String fooSum = "0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33";
        String res = SecurityUtils.sha1Digest("foo");
        assertEquals("sha1 fail", fooSum, res);
    }

    @Test
    public void testCreateKey() {
        String secret = "foo";
        String key1 = SecurityUtils.createKey("foo", "bar");
        String encrypted = SecurityUtils.encryptAES(secret, key1);
        String key2 = SecurityUtils.createKey("foo", "bar");
        String decrypted = SecurityUtils.decryptAES(encrypted, key2);
        assertEquals("decrypted not equals", secret, decrypted);
    }

}
