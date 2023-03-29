package org.example;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AntalPhaHttpClient {

  private final OkHttpClient httpClient;


  static final MediaType JSON_TYPE = MediaType.parse("application/json");

  private AntalPhaHttpClient() {

    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectionPool(new ConnectionPool(200, 10, TimeUnit.SECONDS))
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS);
    httpClient = builder.build();
  }

  public static AntalPhaHttpClient getInstance() {
    return new AntalPhaHttpClient();
  }


  public String doGet(String url, Map<String, Object> params) {
    Request.Builder reqBuild = new Request.Builder();
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    if (!params.isEmpty()) {
      params.forEach((k, v) -> {
        urlBuilder.addQueryParameter(k, v.toString());
      });
    }
    reqBuild.url(urlBuilder.build());

    Response response = null;
    try {
      response = httpClient.newCall(reqBuild.build()).execute();
    } catch (IOException e) {
      throw new RuntimeException("http执行异常，url=" + url, e);
    }
    if (response.isSuccessful()) {
      try {
        return response.body().string();
      } catch (IOException e) {
        throw new RuntimeException("http结果解析异常", e);
      }
    } else {
      int statusCode = response.code();
      throw new RuntimeException(
          "响应码不为200，返回响应码：" + statusCode + "，url：" + urlBuilder.build());
    }
  }


  public String doPost(String appKey, String appSecretKey, String uri, Map<String, Object> params) {
    ApiSignature sign = new ApiSignature();
    Map<String, Object> signMap = new HashMap<>();
    sign.createSignature(appKey, appSecretKey, "POST", uri, signMap);
    try {
      RequestBody body = RequestBody.create(JSON_TYPE, new Gson().toJson(params));
      Request.Builder builder = new Request.Builder()
          .post(body)
          .url(uri + "?" + toQueryString(signMap));
      Request request = builder.build();
      Response response = httpClient.newCall(request).execute();
      return response.body().string();
    } catch (IOException e) {
      throw new RuntimeException("IOException 目标url：" + uri, e);
    }
  }


  private String toQueryString(Map<String, Object> params) {
    return String.join("&",
        params.entrySet().stream()
            .map((entry) -> entry.getKey() + "=" + ApiSignature.urlEncode(
                entry.getValue().toString()))
            .collect(Collectors.toList()));
  }


  public String doGetKey(String appKey, String appSecretKey, String url,
      Map<String, Object> params) {
    ApiSignature sign = new ApiSignature();
    sign.createSignature(appKey, appSecretKey, "GET", url, params);

    Request.Builder reqBuild = new Request.Builder();
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    if (!params.isEmpty()) {
      params.forEach((k, v) -> {
        urlBuilder.addQueryParameter(k, v.toString());
      });
    }
    reqBuild.url(urlBuilder.build());

    Response response = null;
    try {
      response = httpClient.newCall(reqBuild.build()).execute();
    } catch (IOException e) {
      throw new RuntimeException("http执行异常，url=" + url, e);
    }
    if (response.isSuccessful()) {
      try {
        return response.body().string();
      } catch (IOException e) {
        throw new RuntimeException("http结果解析异常", e);
      }
    } else {
      int statusCode = response.code();
      throw new RuntimeException(
          "响应码不为200，返回响应码：" + statusCode + "，url：" + urlBuilder.build());
    }
  }

}
