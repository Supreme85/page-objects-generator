package com.epam.page.object.generator;

import com.epam.jdi.uitests.web.selenium.elements.composite.WebPage;
import com.epam.jdi.uitests.web.selenium.elements.composite.WebSite;
import com.epam.jdi.uitests.web.selenium.elements.pageobjects.annotations.JPage;
import com.epam.jdi.uitests.web.selenium.elements.pageobjects.annotations.JSite;
import com.epam.page.object.generator.builder.IFieldsBuilder;
import com.epam.page.object.generator.errors.NotUniqueSelectorsException;
import com.epam.page.object.generator.model.SearchRule;
import com.epam.page.object.generator.validators.SearchRuleValidator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epam.page.object.generator.builder.StringUtils.firstLetterDown;
import static com.epam.page.object.generator.builder.StringUtils.firstLetterUp;
import static com.epam.page.object.generator.builder.StringUtils.splitCamelCase;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class SiteFieldSpecBuilder {
	private boolean checkLocatorsUniqueness;
	private String packageName;
	private Map<String, IFieldsBuilder> builders;
	private JavaFileWriter fileWriter;

	public SiteFieldSpecBuilder(boolean checkLocatorsUniqueness,
	                            String packageName,
	                            Map<String, IFieldsBuilder> builders,
	                            JavaFileWriter fileWriter) {
		this.checkLocatorsUniqueness = checkLocatorsUniqueness;
		this.packageName = packageName;
		this.builders = builders;
		this.fileWriter = fileWriter;
	}

	public TypeSpec build(List<String> urls, List<SearchRule> searchRules) throws IOException, URISyntaxException, NotUniqueSelectorsException {
		List<FieldSpec> siteClassFields = new ArrayList<>();
		for (String url : urls) {
			String titleName = splitCamelCase(getPageTitle(url));
			String pageFieldName = firstLetterDown(titleName);
			String pageClassName = firstLetterUp(titleName);
			ClassName pageClass = createPageClass(pageClassName, searchRules, url);

			siteClassFields.add(FieldSpec.builder(pageClass, pageFieldName)
					.addModifiers(PUBLIC, STATIC)
					.addAnnotation(AnnotationSpec.builder(JPage.class)
							.addMember("url", "$S", getUrlWithoutDomain(url))
							.addMember("title", "$S", getPageTitle(url))
							.build())
					.build());
		}

		return TypeSpec.classBuilder("Site")
				.addModifiers(PUBLIC)
				.addAnnotation(AnnotationSpec.builder(JSite.class)
						.addMember("domain", "$S", getDomainName(urls))
						.build())
				.superclass(WebSite.class)
				.addFields(siteClassFields)
				.build();
	}

	private ClassName createPageClass(String pageClassName, List<SearchRule> searchRules, String url) throws IOException, NotUniqueSelectorsException {
		List<FieldSpec> fields = new ArrayList<>();

		if (checkLocatorsUniqueness) {
			SearchRuleValidator.checkLocatorUniquenessExceptions(searchRules, url);
		}

		for (SearchRule searchRule : searchRules) {
			fields.addAll(
					builders.get(searchRule.getType().toLowerCase()).buildField(searchRule, url));
		}

		TypeSpec pageClass = TypeSpec.classBuilder(pageClassName)
				.addModifiers(PUBLIC)
				.superclass(WebPage.class)
				.addFields(fields)
				.build();

		fileWriter.write(packageName, pageClass);

		return ClassName.get(packageName + ".pages", pageClassName);
	}

	private String getDomainName(List<String> urls) throws URISyntaxException {
		return new URI(urls.get(0)).getHost();
	}

	private String getPageTitle(String url) throws IOException {
		Document document = Jsoup.connect(url).get();
		return document.title();
	}

	private String getUrlWithoutDomain(String url) throws URISyntaxException {
		return new URI(url).getPath();
	}
}
