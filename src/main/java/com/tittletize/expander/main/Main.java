package com.tittletize.expander.main;

import com.tittletize.expander.entitylinking.TagMeConfig;
import com.tittletize.expander.entitylinking.TagMeConfigFormatException;
import com.tittletize.expander.entitylinking.TagMeRequestFactory;
import com.tittletize.expander.queryexpansion.QueryExpansion;
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

public class Main {

    public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException, ParseException, TagMeConfigFormatException, IOException, URISyntaxException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        if (!TagMeRequestFactory.isInitialized()) {
            TagMeRequestFactory.initialize();
        }

        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);

        TagMeRequestFactory.setConfig(config);
        TagMeRequestFactory.setEndpoint(TagMeRequestFactory.Endpoints.SPOT);

        String query = "envoy";

        URI endpoint = TagMeRequestFactory.generateRequest(query);
        System.out.println(endpoint);

        JSONObject annotation = TagMeRequestFactory.execute(query);
        System.out.println(annotation);

        String expandedTagQuery = QueryExpansion.expandViaEntityLinking(query, annotation);
        System.out.println("\n" + expandedTagQuery);

        String expandedGoogleQuery = QueryExpansion.expandViaKnowledgeBase(query);
        System.out.println("\n" + expandedGoogleQuery);

        String multiExpandedQuery = QueryExpansion.multipleExpand(query, annotation);
        System.out.println("\n" + multiExpandedQuery);

    }

}
