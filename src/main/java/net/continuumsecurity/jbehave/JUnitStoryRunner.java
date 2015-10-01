package net.continuumsecurity.jbehave;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import net.continuumsecurity.Config;
import net.continuumsecurity.clients.Browser;
import net.continuumsecurity.steps.*;
import net.continuumsecurity.web.WebApplication;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by stephen on 09/02/15.
 */
@RunWith(JUnitReportingRunner.class)
public class JUnitStoryRunner extends BaseStoryRunner {

    public JUnitStoryRunner() {
        super();
        List<String> filters = createFilters();
        String filter = System.getProperty("filters");
        if (filter == null) filter = "";
        String stories = System.getProperty("stories");
        if (stories != null) {
            filters.addAll(createStoryMetaFilters(stories));
        } else if (!filter.contains("groovy:")) {
            filters.add("-skip");
        }
        filters.add(filter);
        filters.add(createFilterForBrowserOnlyScenarios(filters));
        configuredEmbedder().useMetaFilters(filters);
        configuredEmbedder().generateReportsView();
        JUnitReportingRunner.recommandedControls(configuredEmbedder());
        try {
            prepareReportsDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    If not a webApplication then disable scenarios that require a browser.  Return a different filter depending on whether we're using
    groovy filtering or not
     */
    public String createFilterForBrowserOnlyScenarios(List<String> filters) {
        if (!(Config.getInstance().createApp() instanceof WebApplication)) {
            if (filters != null && StringUtils.join(filters).contains("groovy:")) return " && browser_only == false";
            return "-browser_only";
        }
        return null;
    }

    private Collection<? extends String> createStoryMetaFilters(String stories) {
        String[] storyArray = stories.split(",");
        StringBuilder groovyMatcher = new StringBuilder("groovy: (");
        Iterator i = Arrays.asList(storyArray).iterator();
        while (i.hasNext()) {
            groovyMatcher.append("story == '").append(i.next()).append("'");
            if (i.hasNext()) groovyMatcher.append(" || ");
        }
        groovyMatcher.append(") && skip == false");
        return Arrays.asList(groovyMatcher.toString());
    }

    protected void prepareReportsDir() throws IOException {
        FileUtils.deleteQuietly(new File(WrapUpScanSteps.LATEST_REPORTS));
        FileUtils.forceMkdir(new File(WrapUpScanSteps.LATEST_REPORTS+File.separator+"zap"));
        File viewDir = new File(WrapUpScanSteps.LATEST_REPORTS + File.separator+"view");
        FileUtils.copyDirectory(new File(WrapUpScanSteps.RESOURCES_DIR), viewDir);
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(),
                new WebApplicationSteps(),
                new InfrastructureSteps(),
                new NessusScanningSteps(),
                new SSLyzeSteps(),
                new AppScanningSteps(),
                new WrapUpScanSteps(),
                new CorsSteps()
                );
    }

    public List<String> createFilters() {
        return new ArrayList<>();
    }

    @Override
    public List<String> storyPaths() {
        List<String> includes = new ArrayList<String>();
        String[] storyArray = System.getProperty("stories").split(",");
        for (String story : storyArray){
            String includedStoryPath = String.format("**/*%s*.story", story);
            includes.add(includedStoryPath);
        }

        List<String> excludes = new ArrayList<String>();
        excludes.add("**/configuration.story");
        return new StoryFinder().findPaths(
                CodeLocations.codeLocationFromURL(storyUrl), includes,
                excludes);
    }

}
