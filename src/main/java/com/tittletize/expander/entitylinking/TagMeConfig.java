package com.tittletize.expander.entitylinking;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagMeConfig {

    public enum Languages {
        ENGLISH("en"),
        GERMAN("de"),
        ITALIAN("it"),
        DEFAULT("en");

        private String lang;

        Languages(String lang) {
            this.lang = lang;
        }

        public String lang() {
            return lang;
        }

        public static boolean contains(String s) {
            if (s.equals("en") || s.equals("de") || s.equals("it"))
                return true;
            return false;
        }
    }

    private String token;
    private Languages language;
    private boolean tweet;
    private boolean include_abstract;
    private boolean include_categories;

    public TagMeConfig(JSONObject config) throws TagMeConfigFormatException {
        String token = config.get("token").toString();
        if (token == null) {
            throw new TagMeConfigFormatException("TagMe API access token is required under \"token\" field in the config file.");
        }

        this.token = token;
        this.include_abstract = false;
        this.include_categories = false;
        this.tweet = false;
        this.language = Languages.DEFAULT;

        if (config.containsKey("include_abstract") && config.get("include_abstract").toString() == "true") {
            this.include_abstract = true;
        }

        if (config.containsKey("include_categories") && config.get("include_categories").toString() == "true") {
            this.include_categories = true;
        }

        if (config.containsKey("tweet") && config.get("tweet").toString() == "true") {
            this.tweet = true;
        }

        if (config.containsKey("language") && Languages.contains(config.get("language").toString())) {
            switch (config.get("language").toString()){
                case "en":
                    this.language = Languages.ENGLISH;
                    break;
                case "de":
                    this.language = Languages.ENGLISH;
                    break;
                case "it":
                    this.language = Languages.ENGLISH;
                    break;
                default:
                    this.language = Languages.DEFAULT;
            }
        }
    }

    public static TagMeConfig parseConfiguration(File configFile) throws IOException, ParseException, TagMeConfigFormatException {
        JSONParser parser = new JSONParser();
        JSONObject config = (JSONObject) parser.parse(new FileReader(configFile));
        return new TagMeConfig(config);
    }

    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("gcube-token", this.getToken());
        parameters.put("lang", this.getLang().lang());
        parameters.put("tweet", Boolean.toString(this.isTweet()));
        parameters.put("include_abstract", Boolean.toString(this.isInclude_abstract()));
        parameters.put("include_categories", Boolean.toString(this.isInclude_categories()));

        return parameters;
    }

    public String getToken() {
        return token;
    }

    public Languages getLang() {
        return language;
    }

    public boolean isTweet() {
        return tweet;
    }

    public boolean isInclude_abstract() {
        return include_abstract;
    }

    public boolean isInclude_categories() {
        return include_categories;
    }

    @Override
    public String toString() {
        return this.getParameters().toString();
    }

    public static void main(String args[]) throws TagMeConfigFormatException, IOException, ParseException {
        File configFile = new File("tagme.config");
        TagMeConfig config = TagMeConfig.parseConfiguration(configFile);
        System.out.println(config);
    }

}
