package br.com.softplan.jsversioning;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by mattos on 10/12/16.
 */
@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.3.9"})
public class JsVersioningStub {

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public JsVersioningStub (MavenRuntime.MavenRuntimeBuilder mavenRuntimeBuilder) throws Exception {
        this.mavenRuntime = mavenRuntimeBuilder.build();
    }

    @Test
    public void testMojoGoal() throws Exception {
        File basedir = this.resources.getBasedir("testproject");
        MavenExecutionResult result = this.mavenRuntime
                .forProject(basedir)
                .execute("clean", "package");
        result.assertErrorFreeLog();
    }

}
