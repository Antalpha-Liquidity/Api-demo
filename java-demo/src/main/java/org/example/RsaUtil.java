package org.example;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RsaUtil {

  private static Logger logger = LoggerFactory.getLogger(RsaUtil.class);

  public static final String CHAR_ENCODING = "UTF-8";
  public static final String RSA = "RSA";


  /**
   * 指定key的大小
   */
  private static final int KEY_SIZE = 1024;

  /**
   * 生成密钥对
   */
  public static Map<String, String> generateKeyPair() throws Exception {
    /** RSA算法要求有一个可信任的随机数源 */
    SecureRandom sr = new SecureRandom();
    /** 为RSA算法创建一个KeyPairGenerator对象 */
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    /** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
    kpg.initialize(KEY_SIZE, sr);
    /** 生成密匙对 */
    KeyPair kp = kpg.generateKeyPair();
    /** 得到公钥 */
    PublicKey publicKey = kp.getPublic();
    byte[] publicKeyBytes = publicKey.getEncoded();
    String pub = Base64.encodeBase64String(publicKeyBytes);
    /** 得到私钥 */
    PrivateKey privateKey = kp.getPrivate();
    byte[] privateKeyBytes = privateKey.getEncoded();
    String pri = Base64.encodeBase64String(privateKeyBytes);
    Map<String, String> map = new HashMap<String, String>();
    map.put("publicKey", pub);
    map.put("privateKey", pri);
    RSAPublicKey rsp = (RSAPublicKey) kp.getPublic();
    BigInteger bint = rsp.getModulus();
    byte[] b = bint.toByteArray();
    String retValue = Base64.encodeBase64String(b);
    map.put("modulus", retValue);
    return map;
  }

  /**
   * 加密方法 source： 源数据
   */
  public static String encrypt(String source, String publicKey)
      throws Exception {
    Key key = getPublicKey(publicKey);
    /** 得到Cipher对象来实现对源数据的RSA加密 */
    Cipher cipher = Cipher.getInstance(RSA);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] b = source.getBytes();
    /** 执行加密操作 */
    byte[] b1 = cipher.doFinal(b);
    return new String(Base64.encodeBase64(b1), CHAR_ENCODING);
  }

  /**
   * 解密算法 cryptograph:密文
   */
  public static String decrypt(String cryptograph, String privateKey)
      throws Exception {
    Key key = getPrivateKey(privateKey);
    /** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
    Cipher cipher = Cipher.getInstance(RSA);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] b1 = Base64.decodeBase64(cryptograph.getBytes());
    /** 执行解密操作 */
    byte[] b = cipher.doFinal(b1);
    return new String(b);
  }

  /**
   * 得到公钥
   *
   * @param key 密钥字符串（经过base64编码）
   */
  public static PublicKey getPublicKey(String key) throws Exception {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(key.getBytes()));
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }

  /**
   * 得到私钥
   *
   * @param key 密钥字符串（经过base64编码）
   */
  public static PrivateKey getPrivateKey(String key) throws Exception {
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key.getBytes()));
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
    return privateKey;
  }

  public static String sign(String content, String privateKey) {
    return sign(content, privateKey, "SHA1WithRSA");
  }

  public static String sign(String content, String privateKey, String algorithm) {
    String charset = CHAR_ENCODING;
    try {
      PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
          Base64.decodeBase64(privateKey.getBytes()));
      KeyFactory keyf = KeyFactory.getInstance(RSA);
      PrivateKey priKey = keyf.generatePrivate(priPKCS8);
      Signature signature = Signature.getInstance(algorithm);
      signature.initSign(priKey);
      signature.update(content.getBytes(charset));
      byte[] signed = signature.sign();
      return Base64.encodeBase64URLSafeString(signed);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public static boolean checkSign(String content, String sign, String publicKey) {
    return checkSign(content, sign, publicKey, "SHA1WithRSA");
  }

  public static boolean checkSign(String content, String sign, String publicKey, String algorithm) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(RSA);
      byte[] encodedKey = Base64.decodeBase64(publicKey);
      PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

      java.security.Signature signature = java.security.Signature
          .getInstance(algorithm);

      signature.initVerify(pubKey);
      signature.update(content.getBytes(StandardCharsets.UTF_8));
      return signature.verify(Base64.decodeBase64(sign));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

}
