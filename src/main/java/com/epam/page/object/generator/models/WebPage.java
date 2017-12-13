package com.epam.page.object.generator.models;

import com.epam.page.object.generator.adapter.classbuildable.FormClassBuildable;
import com.epam.page.object.generator.adapter.classbuildable.JavaClassBuildable;
import com.epam.page.object.generator.models.searchrule.FormSearchRule;
import com.epam.page.object.generator.models.searchrule.Validatable;
import com.epam.page.object.generator.models.webgroup.FormWebElementGroup;
import com.epam.page.object.generator.models.webgroup.WebElementGroup;
import com.epam.page.object.generator.utils.SearchRuleExtractor;
import com.epam.page.object.generator.utils.SelectorUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import com.epam.page.object.generator.models.searchrule.SearchRule;
import org.jsoup.select.Elements;

public class WebPage {

    private final URI uri;
    private Document document;
    private List<WebElementGroup> webElementGroups;
    private SearchRuleExtractor searchRuleExtractor;

    public WebPage(URI uri, Document document,
                   SearchRuleExtractor searchRuleExtractor) {
        this.searchRuleExtractor = searchRuleExtractor;
        this.webElementGroups = new ArrayList<>();
        this.uri = uri;
        this.document = document;
    }

    public String getTitle() {
        return document.title();
    }

    public String getUrlWithoutDomain() {
        return uri.getPath();
    }

    public String getDomainName() {
        return uri.getHost();
    }

    public List<WebElementGroup> getWebElementGroups() {
        return webElementGroups;
    }

    public void addSearchRules(List<SearchRule> searchRules) {
        for (SearchRule searchRule : searchRules) {
            Elements elements = searchRuleExtractor
                .extractElementsFromElement(document, searchRule);
            if (elements.size() != 0) {
                searchRule.fillWebElementGroup(webElementGroups, elements);
            }
        }
    }

    public boolean isContainedFormSearchRule() {
        return webElementGroups.stream()
            .anyMatch(webElementGroup -> webElementGroup.getSearchRule() instanceof FormSearchRule);
    }

    public boolean hasInvalidWebElementGroup() {
        return webElementGroups.stream().anyMatch(Validatable::isInvalid);
    }

    public List<JavaClassBuildable> getFormClasses(SelectorUtils selectorUtils) {
        List<JavaClassBuildable> javaClasses = new ArrayList<>();

        for (WebElementGroup webElementGroup : webElementGroups) {
            if (webElementGroup instanceof FormWebElementGroup) {
                javaClasses.add(new FormClassBuildable((FormWebElementGroup) webElementGroup,
                    selectorUtils));
            }
        }

        return javaClasses;
    }
}