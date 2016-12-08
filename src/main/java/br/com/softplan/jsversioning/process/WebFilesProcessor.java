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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class WebFilesProcessor {

    private Path webFilesDirectory;
    private File webAppOutputDirectory;
    private Log log;

    public WebFilesProcessor(File webFilesDirectory, File webAppOutputDirectory, Log log) {
        this.webFilesDirectory = webFilesDirectory.toPath();
        this.webAppOutputDirectory = webAppOutputDirectory;
        this.log = log;
    }

    public void process(Stream<String> webFilesPaths) throws IOException {
        ScriptTagProcessor scriptTagProcessor = new ScriptTagProcessor();
        webFilesPaths.forEach(scriptTagProcessor::processScriptTags);
    }

    private class ScriptTagProcessor {

        private Pattern pattern = Pattern.compile("src=(\\\".+\\.js)");

        public void processScriptTags(String webFileRelativePath) {
            try {
                String probablyHtmlFileText = FileUtils.readFileToString(Files.createFile(webFilesDirectory.
                (Paths.get(webFileRelativePath))),Charset.defaultCharset());
                Matcher matcher = this.pattern.matcher(probablyHtmlFileText);
                int changes = 0;
                String file;
                for (JsFile jsFile = new JsFile(); matcher.find(); probablyHtmlFileText = org.codehaus.plexus.interpolation.util.StringUtils.replace(probablyHtmlFileText, file, file + "?n=" + jsFile.getVersion() + "")) {
                    ++changes;
                    file = matcher.group(1);
                    jsFile.findSrcContent(file);
                }
                if (changes != 0) {
                    File var7 = new File(WebFilesProcessor.this.webAppOutputDirectory, webFileRelativePath);
                    FileUtils.writeStringToFile(var7, probablyHtmlFileText, "UTF-8");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private class JsFile {

            private String subFolder;
            private String fileName;
            private String separator = "/";
            private Map<String, String> jsHashCode = new ConcurrentHashMap();

            JsFile() {
            }

            void findSrcContent(String srcContent) {
                String[] filePath = StringUtils.split(srcContent, this.separator);
                if (filePath.length > 1) {
                    this.subFolder = filePath[filePath.length - 2];
                }
                this.fileName = filePath[filePath.length - 1];
            }

            String getVersion() {
                String key = this.subFolder + this.fileName;
                if (!this.jsHashCode.containsKey(key)) {
                    IOFileFilter nameFileFilter = FileFilterUtils.nameFileFilter(this.fileName);
                    Collection files = FileUtils.listFiles(WebFilesProcessor.this.webFilesDirectory, nameFileFilter, TrueFileFilter.INSTANCE);
                    String version = this.getVersion(files, this.fileName);
                    this.jsHashCode.put(key, version);
                }
                return this.jsHashCode.get(key);
            }

            private String getVersion(Collection<File> files, String fileName) {
                if (files.isEmpty()) {
                    return this.getRandomVersion(fileName);
                } else {
                    File file = files.iterator().next();
                    try {
                        String e = String.valueOf(FileUtils.checksumCRC32(file));
                        WebFilesProcessor.this.log.info("JS : " + file.toString() + ", CHECKSUM: " + e);
                        return e;
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
                    equals.append(jsFile.subFolder, this.subFolder);
                    equals.append(jsFile.fileName, this.fileName);
                    return equals.isEquals();
                }
                return false;
            }

            @Override
            public int hashCode() {
                HashCodeBuilder hash = new HashCodeBuilder();
                hash.append(this.subFolder);
                hash.append(this.fileName);
                return hash.toHashCode();
            }

        }
    }

}

