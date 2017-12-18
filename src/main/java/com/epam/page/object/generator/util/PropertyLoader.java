package com.epam.page.object.generator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader {

    private String propertyFile;

    private final static Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    public PropertyLoader(String propertyFile) {
        this.propertyFile = propertyFile;
    }

    public SearchRuleGroupsScheme getMapWithScheme() {
        Map<String, Schema> schemeMap = new HashMap<>();

        logger.info("Start reading list of schemes...");
        JSONObject jsonObject = new JSONObject(
            new JSONTokener(PropertyLoader.class.getResourceAsStream(propertyFile)));

        JSONArray typeGroups = jsonObject.getJSONArray("typeGroups");
        for (int i = 0; i < typeGroups.length(); i++) {

            JSONObject group = typeGroups.getJSONObject(i);
            String groupName = group.getString("name");
            logger.debug("Read '" + groupName + "' group...");

            String schemaPath = group.getString("schema");

            try {
                JSONObject jsonSchema = new JSONObject(
                    new JSONTokener(
                        PropertyLoader.class.getResourceAsStream(schemaPath)));
                Schema schema = SchemaLoader.load(jsonSchema);
                schemeMap.put(groupName, schema);
                logger.debug("Add schema = '" + schemaPath + "' for '" + groupName + "' group");
            } catch (NullPointerException ex){
                logger.error("Schema = '" + schemaPath + "' doesn't exist!", ex);
                throw new NullPointerException("\nSchema = '" + schemaPath + "' doesn't exist!");
            }
        }
        logger.info("Finish reading list of schemes\n");

        return new SearchRuleGroupsScheme(schemeMap);
    }

    public SearchRuleGroups getSearchRuleGroupList() {
        List<SearchRuleGroup> searchRuleGroups = new ArrayList<>();

        logger.info("Start reading list of group types...");
        JSONObject jsonObject = new JSONObject(
            new JSONTokener(PropertyLoader.class.getResourceAsStream(propertyFile)));

        JSONArray typeGroups = jsonObject.getJSONArray("typeGroups");
        for (int i = 0; i < typeGroups.length(); i++) {
            JSONObject group = typeGroups.getJSONObject(i);

            String groupName = group.getString("name");
            logger.debug("Read '" + groupName + "' group...");
            List<SearchRuleType> searchRuleTypes = new ArrayList<>();
            JSONArray types = group.getJSONArray("searchRuleTypes");
            for (int j = 0; j < types.length(); j++) {
                searchRuleTypes.add(SearchRuleType.getSearchRuleTypeByString(types.getString(j)));
                logger.debug("Add type = '" + types.getString(j) + "' in '" + groupName + "' group");
            }
            searchRuleGroups.add(new SearchRuleGroup(groupName, searchRuleTypes));
            logger.debug("Finish read group types - '" + groupName + "'\n");
        }
        logger.info("Finish reading list of group types\n");

        return new SearchRuleGroups(searchRuleGroups);
    }
}
