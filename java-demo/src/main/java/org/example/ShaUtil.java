package org.example;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 验签
 *
 * @author xieky
 */

public class ShaUtil {

  private static Logger logger = LoggerFactory.getLogger(ShaUtil.class);

  public static String sha256Mac(String content, String secret, String algorithm) {
    try {
      Mac hMacSha256 = Mac.getInstance(algorithm);
      SecretKeySpec secKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
      hMacSha256.init(secKey);
      byte[] hash = hMacSha256.doFinal(content.getBytes(StandardCharsets.UTF_8));
      return java.util.Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("No such algorithm: " + e.getMessage());
    } catch (InvalidKeyException e) {
      throw new RuntimeException("Invalid key: " + e.getMessage());
    }
  }

  public static boolean checkSign(String content, String sign, String secret, String algorithm) {
    String actualSign = sha256Mac(content, secret, algorithm);
    logger.debug("url content:{}", content);
    logger.debug("sign:{}, actualSign:{}", sign, actualSign);
    return sign.equals(actualSign);
  }

}
