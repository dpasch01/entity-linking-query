package com.tittletize.expander.entitylinking;

import com.tittletize.expander.queryexpansion.QueryExpansion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("Duplicates")
public class EntityLinking {

    public static List<String> extractTopics(JSONObject jsonObject) {
        JSONArray annotations = (JSONArray) jsonObject.get("annotations");
        List<String> topics = new ArrayList<>();

        for (Object entry : annotations) {
            JSONObject annotation = (JSONObject) entry;
            if ((double) annotation.get("link_probability") > QueryExpansion.ENTITY_LINKING_THRESHOLD) {
                for (Object category : (JSONArray) annotation.get("dbpedia_categories")) {
                    topics.add(category.toString());
                }
            }
        }

        return topics;
    }

    public static List<String> extractEntities(String query, double threshold) throws KeyManagementException, NoSuchAlgorithmException, ParseException, TagMeConfigFormatException, IOException, URISyntaxException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        if (!TagMeRequestFactory.isInitialized()) {
            TagMeRequestFactory.initialize();
        }

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(TagMeRequestFactory.Endpoints.SPOT);

        JSONObject jsonObject = TagMeRequestFactory.execute(query);

        JSONArray spots = (JSONArray) jsonObject.get("spots");
        List<String> entities = new ArrayList<>();

        if (spots == null) {
            return entities;
        }

        for (Object entry : spots) {
            JSONObject spot = (JSONObject) entry;
            String link_probability = spot.get("lp").toString();
            if (link_probability.length() > 6) {
                link_probability = link_probability.substring(0, 7);
            }

            if (Double.parseDouble(link_probability) > threshold) {
                entities.add(spot.get("spot").toString());
            }
        }

        return entities;
    }

    public static List<String> extractEntities(String query) throws KeyManagementException, NoSuchAlgorithmException, ParseException, TagMeConfigFormatException, IOException, URISyntaxException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        if (!TagMeRequestFactory.isInitialized()) {
            TagMeRequestFactory.initialize();
        }

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(TagMeRequestFactory.Endpoints.SPOT);

        JSONObject jsonObject = TagMeRequestFactory.execute(query);

        JSONArray spots = (JSONArray) jsonObject.get("spots");
        List<String> entities = new ArrayList<>();

        if (spots == null) {
            return entities;
        }

        for (Object entry : spots) {
            JSONObject spot = (JSONObject) entry;
            String link_probability = spot.get("lp").toString();
            if (link_probability.length() > 6) {
                link_probability = link_probability.substring(0, 7);
            }

            if (Double.parseDouble(link_probability) > QueryExpansion.ENTITY_LINKING_THRESHOLD) {
                entities.add(spot.get("spot").toString());
            }
        }

        return entities;
    }

    private static class EntityEntry<K, V> implements Map.Entry {
        private final K key;
        private V value;

        @Override
        public String toString() {
            return key.toString() + ": " + value.toString();
        }

        private EntityEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Object getKey() {
            return this.key;
        }

        @Override
        public Object getValue() {
            return this.value;
        }

        @Override
        public Object setValue(Object value) {
            this.value = (V) value;
            return this.value;
        }
    }

    public static List<Map.Entry<String, Double>> extractScoredEntities(String query) throws KeyManagementException, NoSuchAlgorithmException, ParseException, TagMeConfigFormatException, IOException, URISyntaxException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        if (!TagMeRequestFactory.isInitialized()) {
            TagMeRequestFactory.initialize();
        }

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(TagMeRequestFactory.Endpoints.SPOT);

        JSONObject jsonObject = TagMeRequestFactory.execute(query);

        JSONArray spots = (JSONArray) jsonObject.get("spots");
        List<Map.Entry<String, Double>> entities = new ArrayList<>();

        for (Object entry : spots) {
            JSONObject spot = (JSONObject) entry;
            String link_probability = spot.get("lp").toString();
            if (link_probability.length() > 6) {
                link_probability = link_probability.substring(0, 7);
            }

            if (Double.parseDouble(link_probability) > QueryExpansion.ENTITY_LINKING_THRESHOLD) {
                entities.add(new EntityEntry<>(spot.get("spot").toString(), Double.parseDouble(link_probability)));
            }
        }

        return entities;
    }

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, ParseException, TagMeConfigFormatException, IOException, URISyntaxException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        TagMeRequestFactory.initialize();

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(TagMeRequestFactory.Endpoints.TAG);

        URI endpoint = TagMeRequestFactory.generateRequest("On this day 24 years ago Maradona scored his infamous \"Hand of God\" goal against England in the quarter-final of the 1986.");
        System.out.println(endpoint);

        JSONObject annotation = TagMeRequestFactory.execute("On this day 24 years ago Maradona scored his infamous \"Hand of God\" goal against England in the quarter-final of the 1986.");
        System.out.println(annotation);

        List<String> topics = EntityLinking.extractTopics(annotation);
        System.out.println(topics);

        List<String> entities = EntityLinking.extractEntities("On this day 24 years ago Maradona scored his infamous \"Hand of God\" goal against England in the quarter-final of the 1986.");
        System.out.println(entities);

    }
}
