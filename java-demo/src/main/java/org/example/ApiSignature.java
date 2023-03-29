package org.example;

import com.google.gson.Gson;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApiSignature {

  final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
   *
   * @param appKey       AppKeyId.
   * @param appSecretKey AppKeySecret.
   * @param method       请求方法，"GET"或"POST"
   * @param uri          请求路径，注意不含?以及后的参数，例如"/v1/api/info"
   * @param params       原始请求参数，以Key-Value存储，注意Value不要编码
   */
  public void createSignature(String appKey, String appSecretKey, String method, String uri,
      Map<String, Object> params) {
    StringBuilder sb = new StringBuilder(1024);
    int index = uri.indexOf("//");
    String subString = uri.substring(index + 2);
    int index2 = subString.indexOf("/");
    String host = subString.substring(0, index2);
    String constant = subString.substring(index2);
    sb.append(method.toUpperCase()).append('\n') // GET
        .append(host.toLowerCase()).append('\n') // Host
        .append(constant).append('\n'); // /path
    params.put("AccessKeyId", appKey);
    params.put("SignatureVersion", "1");
    if (StringUtils.isNotEmpty(appSecretKey)) {
      params.put("SignatureMethod", "HmacSHA256");
    } else {
      params.put("SignatureMethod", "SHA256WithRSA");
    }
    params.put("Timestamp", gmtNow());
    // build signature:
    SortedMap<String, Object> map = new TreeMap<>(params);
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue().toString();
      sb.append(key).append('=').append(urlEncode(value)).append('&');
    }
    // remove last '&':
    sb.deleteCharAt(sb.length() - 1);
    // sign:
    String actualSign = "";
    if (StringUtils.isNotEmpty(appSecretKey)) {
      actualSign = ShaUtil.sha256Mac(sb.toString(), appSecretKey, "HmacSHA256");
    } else {
      actualSign = RsaUtil.sign(sb.toString(), appSecretKey, "SHA256WithRSA");
    }
    params.put("Signature", actualSign);
    log.info("content:{}, params:{}", sb, new Gson().toJson(params));
    log.info("Signature:{}", actualSign);
  }

  /**
   * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
   *
   * @param s String字符串
   * @return URL编码后的字符串
   */
  public static String urlEncode(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  String gmtNow() {
    return String.valueOf(Instant.now().toEpochMilli());
  }
}
