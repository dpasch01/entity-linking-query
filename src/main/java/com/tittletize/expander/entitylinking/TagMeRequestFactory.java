package com.tittletize.expander.entitylinking;

import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class TagMeRequestFactory {
    private static boolean initialized = false;

    private static TagMeConfig config;
    private static Endpoints endpoint;

    public static void initialize() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {

                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {

                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        TagMeRequestFactory.initialized = true;
    }

    public static void setEndpoint(Endpoints endpoint) {
        TagMeRequestFactory.endpoint = endpoint;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public enum Endpoints {
        SPOT("spot"),
        TAG("tag"),
        REL("rel"),
        DEFAULT("tag");

        private String endpoint;

        Endpoints(String endpoint) {
            this.endpoint = endpoint;
        }

        public String endpoint() {
            return endpoint;
        }

        public static boolean contains(String s) {
            if (s.equals("spot") || s.equals("tag") || s.equals("rel"))
                return true;
            return false;
        }
    }

    public static void setConfig(TagMeConfig config) {
        TagMeRequestFactory.config = config;
    }

    public static URI generateRequest(String text) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("tagme.d4science.org/tagme/").setPath(endpoint.endpoint());
        for (Map.Entry<String, String> parameter : config.getParameters().entrySet()) {
            builder.setParameter(parameter.getKey(), parameter.getValue());
        }

        builder.addParameter("text", text);
        URI uri = builder.build();
        return uri;
    }

    @SuppressWarnings("Duplicates")
    public static JSONObject execute(String text) throws URISyntaxException, IOException, ParseException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        URI requestURI = TagMeRequestFactory.generateRequest(text);
        URL url = new URL(requestURI.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-length", "0");
        connection.setUseCaches(true);
        connection.setAllowUserInteraction(false);
        connection.connect();
        int status = connection.getResponseCode();

        JSONObject resultObject;

        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                JSONParser parser = new JSONParser();
                resultObject = (JSONObject) parser.parse(sb.toString());
                break;

            default:
                resultObject = new JSONObject();
                resultObject.put("status", status);
                resultObject.put("message", connection.getResponseMessage());

        }

        return resultObject;
    }

    public static String normalize(String entity) throws URISyntaxException, IOException, ParseException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        URI requestURI = TagMeRequestFactory.generateRequest(entity);
        URL url = new URL(requestURI.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-length", "0");
        connection.setUseCaches(true);
        connection.setAllowUserInteraction(false);
        connection.connect();
        int status = connection.getResponseCode();

        JSONObject resultObject;

        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                JSONParser parser = new JSONParser();
                resultObject = (JSONObject) parser.parse(sb.toString());
                break;

            default:
                resultObject = new JSONObject();
                resultObject.put("status", status);
                resultObject.put("message", connection.getResponseMessage());

        }

        JSONArray annotations = (JSONArray) resultObject.get("annotations");
        if (annotations == null || annotations.size() == 0 || ((JSONObject) (annotations).get(0)).get("title") == null) {
            return null;
        }

        return ((JSONObject) (annotations).get(0)).get("title").toString();
    }

    public static void main(String args[]) throws TagMeConfigFormatException, IOException, ParseException, URISyntaxException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TagMeRequestFactory.initialize();

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(Endpoints.TAG);

        URI endpoint = TagMeRequestFactory.generateRequest("On this day 24 years ago Maradona scored his infamous \"Hand of God\" goal against England in the quarter-final of the 1986.");
        System.out.println(endpoint);

        JSONObject annotation = TagMeRequestFactory.execute("On this day 24 years ago Maradona scored his infamous \"Hand of God\" goal against England in the quarter-final of the 1986.");
        System.out.println(annotation);

    }

}
