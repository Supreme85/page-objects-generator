package com.epam.page.object.generator.models.searchrule;

import com.epam.page.object.generator.models.ClassAndAnnotationPair;
import com.epam.page.object.generator.models.Selector;
import com.epam.page.object.generator.models.webgroup.FormWebElementGroup;
import com.epam.page.object.generator.models.webgroup.WebElementGroup;
import com.epam.page.object.generator.models.webelement.WebElement;
import com.epam.page.object.generator.utils.SearchRuleType;
import com.epam.page.object.generator.validators.ValidationResult;
import com.epam.page.object.generator.validators.ValidatorVisitor;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormSearchRule implements SearchRule {

    private String section;
    private SearchRuleType type;
    private Selector selector;
    private List<FormInnerSearchRule> innerSearchRules;

    private ClassAndAnnotationPair classAndAnnotation;

    private List<ValidationResult> validationResults = new ArrayList<>();
    private final static Logger logger = LoggerFactory.getLogger(FormSearchRule.class);

    public FormSearchRule(String section, SearchRuleType type, Selector selector,
                          List<FormInnerSearchRule> innerSearchRules,
                          ClassAndAnnotationPair classAndAnnotation) {
        this.section = section;
        this.type = type;
        this.selector = selector;
        this.innerSearchRules = innerSearchRules;
        this.classAndAnnotation = classAndAnnotation;
    }

    public String getSection() {
        return section;
    }

    public SearchRuleType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getName();
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public List<WebElement> getWebElements(Elements elements) {
        List<WebElement> webElements = new ArrayList<>();
        for (FormInnerSearchRule innerSearchRule : innerSearchRules) {
            webElements.addAll(innerSearchRule.getWebElements(elements));
        }
        return webElements;
    }

    @Override
    public void fillWebElementGroup(List<WebElementGroup> webElementGroups, Elements elements) {
        webElementGroups.add(new FormWebElementGroup(this, getWebElements(elements)));
    }

    public List<FormInnerSearchRule> getInnerSearchRules() {
        return innerSearchRules;
    }

    public ClassAndAnnotationPair getClassAndAnnotation() {
        return classAndAnnotation;
    }

    @Override
    public void accept(ValidatorVisitor validatorVisitor) {
        ValidationResult visit = validatorVisitor.visit(this);
        logger.info(this + " is '" + visit.isValid() + "', reason '" + visit.getReason() + "'");
        validationResults.add(visit);
    }

    @Override
    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }

    @Override
    public boolean isValid() {
        return validationResults.stream().allMatch(ValidationResult::isValid);
    }

    @Override
    public boolean isInvalid() {
        return validationResults.stream()
            .anyMatch(validationResultNew -> !validationResultNew.isValid());
    }

    @Override
    public String toString() {
        return "SearchRule{" +
            "section='" + section + '\'' +
            ", type='" + type + '\'' +
            ", selector=" + selector +
            ", innerSearchRules=" + innerSearchRules +
            '}';
    }
}