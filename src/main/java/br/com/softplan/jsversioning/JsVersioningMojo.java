/**
 * MIT License
 * <p>
 * Copyright (c) 2016 Softplan
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package br.com.softplan.jsversioning;

import br.com.softplan.jsversioning.process.WebFilesProcessor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

@Mojo(name = "versioning", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class JsVersioningMojo extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}/src/main/webapp", required = true)
    private File webFilesDirectory;

    @Parameter(defaultValue = "${project.build.directory}/temp", required = true)
    private File webappOutputDirectory;

    @Parameter(required = false, property = "maven.skip.jsversioning")
    private boolean skipJsVersioning;

    @Component
    private MavenProject mavenProject;

    public void execute() throws MojoFailureException {
        if (!this.webappOutputDirectory.exists()) {
            this.webappOutputDirectory.mkdirs();
        }
        validateParameters();
        if (this.skipJsVersioning()) {
            getLog().info("Js versioning skipped");
        } else {
            new WebFilesProcessor(this.webFilesDirectory, this.webappOutputDirectory, getLog()).process();
        }
    }

    private void validateParameters() {
    }

    private boolean skipJsVersioning() {
        return this.skipJsVersioning || this.getSkipProperty();
    }

    private boolean getSkipProperty() {
        Properties properties = this.mavenProject.getProperties();
        Object object = properties.get("maven.skip.jsversioning");
        return object != null && Boolean.TRUE.equals(object);
    }

}

