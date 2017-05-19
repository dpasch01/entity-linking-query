package com.tittletize.expander.knowledgegraph;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeEntity {
    private List<String> types;
    private String title;
    private String description;
    private String imageURL;
    private String id;
    private String summary;
    private double score;

    public KnowledgeEntity(JSONObject knowledgeResults) {
        this.types = new ArrayList<>();

        JSONArray items = (JSONArray) knowledgeResults.get("itemListElement");
        if(items == null || items.isEmpty()){
            return;
        }
        JSONObject knowledgeChunk = (JSONObject) ((JSONObject) items.get(0)).get("result");

        this.id = (String) knowledgeChunk.get("@id");
        this.title = (String) knowledgeChunk.get("name");
        for (Object type : (JSONArray) knowledgeChunk.get("@type")) {
            types.add(type.toString());
        }
        this.description = (String) knowledgeChunk.get("description");
        if(knowledgeChunk.get("image") != null) {
            this.imageURL = (String) ((JSONObject) knowledgeChunk.get("image")).get("url");
        }
        if(knowledgeChunk.get("detailedDescription") != null) {
            this.summary = (String) ((JSONObject) knowledgeChunk.get("detailedDescription")).get("articleBody");
        }

        String link_probability = ((JSONObject) items.get(0)).get("resultScore").toString();
        if (link_probability.length() > 6) {
            link_probability = link_probability.substring(0, 7);
        }
        this.score = Double.parseDouble(link_probability);
    }

    public List<String> getTypes() {
        return types;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return this.getSummary();
    }
}

