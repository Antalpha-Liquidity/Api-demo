package org.example;

import java.util.HashMap;
import java.util.Map;

public class TestHttpClient {

  public static void main(String[] args) {
    AntalPhaHttpClient httpClient = AntalPhaHttpClient.getInstance();
    String appKey = "1112212";
    String appSecretKey = "4F65x5A2bLyMWVQj3Aqp+B4w+ivaA7n5Oi2SuYtCJ9o=";
    String uri = "http://127.0.0.1:8080/convert/order2";
    Map<String, Object> params = new HashMap<>();
    params.put("orderId", 1122312);
    params.put("orderType", 1);
     String response = httpClient.doPost(appKey, appSecretKey, uri, params);
    System.out.println("response:" + response);
  }

}
