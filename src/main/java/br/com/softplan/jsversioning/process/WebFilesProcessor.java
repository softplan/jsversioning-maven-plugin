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
package br.com.softplan.jsversioning.process;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebFilesProcessor {

    private File webFilesDirectory;
    private File webAppOutputDirectory;
    private Log log;

    public WebFilesProcessor(File webFilesDirectory, File webAppOutputDirectory, Log log) {
        this.webFilesDirectory = webFilesDirectory;
        this.webAppOutputDirectory = webAppOutputDirectory;
        this.log = log;
    }

    public void process() {
        Path start = this.webFilesDirectory.toPath();
        this.log.debug("Start to walk the file tree of path on " + start.toString() );
        try {
            Files.walkFileTree(start, new WebFilesVisitor());
        } catch (Exception e) {
            throw new IllegalStateException("Error processing files: ", e);
        }
    }

    private class ScriptTagProcessor {

        private Pattern pattern = Pattern.compile("src=(\\\".+\\.js)");
        private Map<String, String> jsVersionCache = new ConcurrentHashMap<>();

        public String process(final Path filePath) throws IOException {

            String content = FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset());
            Matcher matcher = this.pattern.matcher(content);
            String newContent = content;
            while (matcher.find()) {
                String fileName = matcher.group(1);

                String version = this.jsVersionCache.get(fileName);
                if (StringUtils.isBlank(version)) {
                    version = new JsFile(fileName).getVersion();
                    this.jsVersionCache.put(fileName, version);
                }

                newContent = StringUtils.replace(newContent, fileName, fileName + "?n=" + version + "");
            }

            return newContent;
        }

        private class JsFile {

            private String fileName;

            JsFile(String fileNamePath) {
                this.fileName = fileNamePath;
            }

            String getVersion() {
                IOFileFilter nameFileFilter = FileFilterUtils.nameFileFilter(this.fileName);
                Collection files = FileUtils.listFiles(WebFilesProcessor.this.webFilesDirectory, nameFileFilter, TrueFileFilter.INSTANCE);
                return this.getVersion(files, this.fileName);
            }

            private String getVersion(Collection<File> files, String fileName) {
                if (files.isEmpty()) {
                    return getRandomVersion(fileName);
                } else {
                    File file = files.iterator().next();
                    try {
                        String checksum = String.valueOf(FileUtils.checksumCRC32(file));
                        WebFilesProcessor.this.log.info("JS : " + file.toString() + ", CHECKSUM: " + checksum);
                        return checksum;
                    } catch (IOException var5) {
                        return this.getRandomVersion(fileName);
                    }
                }
            }

            private String getRandomVersion(String fileName) {
                String randomString = UUID.randomUUID().toString();
                WebFilesProcessor.this.log.info("JS NOT FOUND: " + fileName + ", VERSION: " + randomString);
                return randomString;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof EqualsBuilder) {
                    JsFile jsFile = (JsFile) obj;
                    EqualsBuilder equals = new EqualsBuilder();
                    equals.append(jsFile.fileName, this.fileName);
                    return equals.isEquals();
                }
                return false;
            }

            @Override
            public int hashCode() {
                HashCodeBuilder hash = new HashCodeBuilder();
                hash.append(this.fileName);
                return hash.toHashCode();
            }

        }
    }

    private class WebFilesVisitor extends SimpleFileVisitor<Path> {

        private ScriptTagProcessor scriptTagProcessor = new ScriptTagProcessor();

        @Override
        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
            this.scriptTagProcessor.process(filePath);
            return FileVisitResult.CONTINUE;
        }

    }

}
