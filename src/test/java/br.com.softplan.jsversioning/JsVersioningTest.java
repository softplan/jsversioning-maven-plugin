package br.com.softplan.jsversioning;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by mattos on 10/12/16.
 */
@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.3.9"})
public class JsVersioningTest {

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public JsVersioningTest(MavenRuntime.MavenRuntimeBuilder mavenRuntimeBuilder) throws Exception {
        this.mavenRuntime = mavenRuntimeBuilder.build();
    }

    @Test
    public void itWorksOnProjectsWithoutJavaSource() throws Exception {
        File basedir = this.resources.getBasedir("testproject");
        MavenExecutionResult result = this.mavenRuntime
                .forProject(basedir)
                .execute("clean", "package");
        result.assertErrorFreeLog();

        File fileGenerated = new File(basedir, "target/test-project-0.0.1/index.html");
        File fileExpected = new File("src/test/resources/testproject/index.html");
        Assert.assertTrue(FileUtils.contentEquals(fileGenerated, fileExpected));
    }

}
