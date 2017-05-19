package com.tittletize.expander.knowledgegraph;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
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
import java.util.ArrayList;
import java.util.List;

public class KnowledgeGraph {

    private static boolean initialized = false;

    private static String ACCESS_TOKEN = "";
    private static List<String> TOKEN_POOL = new ArrayList<>();

    static {
        try {
            TOKEN_POOL = FileUtils.readLines(new File("kgsearch.pool"));
            ACCESS_TOKEN = TOKEN_POOL.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static final String RESULTS_LIMIT = "1";

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

        KnowledgeGraph.initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static URI generateRequest(String entity) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("kgsearch.googleapis.com/v1/").setPath("entities:search");
        builder.addParameter("query", entity);
        builder.addParameter("key", KnowledgeGraph.ACCESS_TOKEN);
        builder.addParameter("limit", KnowledgeGraph.RESULTS_LIMIT);

        URI uri = builder.build();
        return uri;
    }

    @SuppressWarnings("Duplicates")
    public static KnowledgeEntity execute(String entity) throws URISyntaxException, IOException, ParseException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        URI requestURI = KnowledgeGraph.generateRequest(entity);
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

        return new KnowledgeEntity(resultObject);
    }

    public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, UnrecoverableKeyException, CertificateException, KeyStoreException, ParseException, IOException {
        KnowledgeGraph.initialize();

        URI endpoint = KnowledgeGraph.generateRequest("Maradona");
        System.out.println(endpoint);

        KnowledgeEntity knowledgeChunk = KnowledgeGraph.execute("Maradona");
        System.out.println(knowledgeChunk);

    }

}
