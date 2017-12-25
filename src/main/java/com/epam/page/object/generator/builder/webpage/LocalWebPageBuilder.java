package com.epam.page.object.generator.builder.webpage;

import com.epam.page.object.generator.error.NotValidPathsException;
import com.epam.page.object.generator.model.WebPage;
import com.epam.page.object.generator.util.SearchRuleExtractor;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Class for building correct web pages from local file
 */
public class LocalWebPageBuilder implements WebPageBuilder {

    private final static Logger logger = LoggerFactory.getLogger(LocalWebPageBuilder.class);

    /**
     * Method creates web pages by using url
     * @param paths
     * @param searchRuleExtractor
     * @return webPages
     */
    @Override
    public List<WebPage> generate(List<String> paths, SearchRuleExtractor searchRuleExtractor) {
        List<WebPage> webPages = new ArrayList<>();
        List<String> invalidPaths = new ArrayList<>();
        for (String path : paths) {
            try {
                String html = Files
                    .toString(new File(LocalWebPageBuilder.class.getResource(path).getFile()),
                        Charsets.UTF_8);
                Document doc = Jsoup.parse(html);
                URI uri = new URI("www.test.com");
                webPages.add(new WebPage(uri, doc, searchRuleExtractor));
            } catch (IOException | NullPointerException e) {
                String message = "File = '" + path + "' doesn't exist!";
                logger.error(message, e);
                invalidPaths.add(message);
            } catch (URISyntaxException e) {
                String message = "Not correct URI for the '" + path + "' file";
                logger.error(message, e);
                invalidPaths.add(message);
            }
        }
        if (!invalidPaths.isEmpty()) {
            String message = invalidPaths.stream().collect(Collectors.joining("\n"));
            NotValidPathsException e = new NotValidPathsException(message);
            logger.error(message, e);
            throw e;
        }

        return webPages;
    }
}