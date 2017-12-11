package com.epam.page.object.generator.model;

import com.epam.page.object.generator.utils.SearchRuleType;
import com.epam.page.object.generator.utils.searchRuleGroups.SearchRuleGroup;
import com.epam.page.object.generator.validators.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class RawSearchRule {

    private JSONObject element;
    private SearchRuleType type;
    private SearchRuleGroup group;
    private Schema schema;

    private List<ValidationResult> validationResults = new ArrayList<>();

    public RawSearchRule(JSONObject element, SearchRuleType type, SearchRuleGroup group,
                         Schema schema) {
        this.element = element;
        this.type = type;
        this.group = group;
        this.schema = schema;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getValue(String parameter) {
        Object param = element.get(parameter);
        if (param == null) {
            return null;
        }
        return param.toString();
    }

    public Selector getSelector() {
        JSONObject selector = element.getJSONObject("selector");
        if (selector == null) {
            return null;
        }
        return new Selector(selector.getString("type"), selector.getString("value"));
    }

    public void setValidationResults(List<ValidationResult> validationResults) {
        this.validationResults = validationResults;
    }

    public JSONObject getElement() {
        return element;
    }

    public boolean isInvalid() {
        return validationResults.stream().anyMatch(ValidationResult::isInvalid);
    }

    public boolean isValid() {
        return validationResults.stream().allMatch(ValidationResult::isValid);
    }

    public String getExceptionMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        validationResults.stream().filter(validationResult -> !validationResult.isValid()).forEach(
            validationResult -> stringBuilder.append(validationResult.getReason())
                .append("\n"));
        return stringBuilder.toString();
    }

    public String getGroupName() {
        if (isValid()) {
            return group.getName();
        }
        return null;
    }

    public SearchRuleType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SearchRule{" + element + '}';
    }
}
