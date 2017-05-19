package com.tittletize.expander.queryexpansion;

import com.tittletize.expander.entitylinking.EntityLinking;
import com.tittletize.expander.entitylinking.TagMeConfigFormatException;
import com.tittletize.expander.knowledgegraph.KnowledgeEntity;
import com.tittletize.expander.knowledgegraph.KnowledgeGraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

@SuppressWarnings("Duplicates")
public class QueryExpansion {
    public static double ENTITY_LINKING_THRESHOLD = 0.1;
    public static double KNOWLEDGE_GRAPH_THRESHOLD = 0f;

    public static String expandViaEntityLinking(String query, JSONObject jsonObject) {
        JSONArray annotations = (JSONArray) jsonObject.get("annotations");
        StringBuilder expansion = new StringBuilder();
        expansion.append(query);

        if (annotations == null) {
            return query;
        }

        for (Object entry : annotations) {
            JSONObject annotation = (JSONObject) entry;
            String link_probability = annotation.get("link_probability").toString();
            if (link_probability.length() > 6) {
                link_probability = link_probability.substring(0, 7);
            }

            if (Double.parseDouble(link_probability) > QueryExpansion.ENTITY_LINKING_THRESHOLD) {
                expansion.append(" " + annotation.get("title"));

                /*
                    For a more detailed expansion you can use the abstract field of the annotation, if it is available, which
                    contains a summary of the found article in the wikipedia pages.

                    expansion.append(" " + annotation.get("abstract"));
                 */
            }
        }

        return expansion.toString();
    }



    public static String expandViaKnowledgeBase(String query) throws IOException, CertificateException, NoSuchAlgorithmException, URISyntaxException, TagMeConfigFormatException, ParseException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        List<String> entities = EntityLinking.extractEntities(query);
        StringBuilder expansion = new StringBuilder();
        expansion.append(query);

        for (String entity : entities) {
            KnowledgeEntity ke = KnowledgeGraph.execute(entity);

            if (ke == null) {
                continue;
            }

            expansion.append(" " +ke.getTitle());
            /*
                For a more detailed expansion you can use the other fields of the KnowledgeEntity object, like "summary"
                or "description" etc.

                expansion.append(" " + ke.getSummary());
                expansion.append(" " + ke.getSummary());
                expansion.append(" " + ke.getTitle());
             */
        }

        return expansion.toString();
    }

    public static String multipleExpand(String query, JSONObject jsonObject) throws IOException, CertificateException, NoSuchAlgorithmException, URISyntaxException, TagMeConfigFormatException, ParseException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        JSONArray annotations = (JSONArray) jsonObject.get("annotations");
        StringBuilder expansion = new StringBuilder();
        expansion.append(query);

        if (annotations != null) {
            for (Object entry : annotations) {
                JSONObject annotation = (JSONObject) entry;

                String link_probability = annotation.get("link_probability").toString();
                if (link_probability.length() > 6) {
                    link_probability = link_probability.substring(0, 7);
                }

                if (Double.parseDouble(link_probability) > QueryExpansion.ENTITY_LINKING_THRESHOLD) {
                    expansion.append(" " + annotation.get("title"));
                    expansion.append(" " + annotation.get("abstract"));
                /*
                    For a more detailed expansion you can use the abstract field of the annotation, if it is available, which
                    contains a summary of the found article in the wikipedia pages.

                    expansion.append(" " + annotation.get("abstract"));
                 */
                }
            }
        }

        List<String> entities = EntityLinking.extractEntities(query);

        for (String entity : entities) {
            KnowledgeEntity ke = KnowledgeGraph.execute(entity);
//            expansion.append(" " + ke.getDescription());
//            expansion.append(" " + ke.getSummary());
            expansion.append(" " + ke.getTitle());
            /*
                For a more detailed expansion you can use the other fields of the KnowledgeEntity object, like "summary"
                or "description" etc.

                expansion.append(" " + ke.getSummary());
                expansion.append(" " + ke.getTitle());
             */
        }

        return expansion.toString();
    }

}
