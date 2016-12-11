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
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebFilesProcessor {

    private Path webFilesDirectory;
    private Path webAppOutputDirectory;
    private Log log;

    public WebFilesProcessor(File webFilesDirectory, File webAppOutputDirectory, Log log) {
        this.webFilesDirectory = webFilesDirectory.toPath();
        this.webAppOutputDirectory = webAppOutputDirectory.toPath();
        this.log = log;
    }

    public void process() {
        this.log.debug("Start to walk the file tree of path on " + this.webFilesDirectory);
        try {
            Files.walkFileTree(this.webFilesDirectory, new WebFilesVisitor());
        } catch (Exception e) {
            throw new IllegalStateException("Error processing files: ", e);
        }
    }

    private class ScriptTagProcessor {

        private Pattern pattern = Pattern.compile("src=\"(.+\\.js)");
        private Map<String, String> jsVersionCache = new ConcurrentHashMap<>();

        public Optional<String> process(final Path filePath) throws IOException {

            String content = FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset());
            Matcher matcher = this.pattern.matcher(content);

            int changes = 0;
            while (matcher.find()) {
                changes++;
                String fileName = matcher.group(1);

                String version = this.jsVersionCache.get(fileName);
                if (StringUtils.isBlank(version)) {
                    version = new JsFile(fileName).getVersion();
                    this.jsVersionCache.put(fileName, version);
                }

                content = StringUtils.replace(content, String.format("\"%s\"", fileName), String.format("\"%s?n=%s\"", fileName, version));
            }

            if (changes > 0) {
                return Optional.ofNullable(content);
            }
            return Optional.empty();
        }

        private class JsFile {

            private String fileName;

            JsFile(String fileNamePath) {
                this.fileName = fileNamePath;
            }

            String getVersion() {
                Path jsFilePath = WebFilesProcessor.this.webFilesDirectory.resolve(fileName);
                return resolveFileChecksum(jsFilePath.toFile());
            }

            private String resolveFileChecksum(File file) {
                try {
                    String checksum = String.valueOf(FileUtils.checksumCRC32(file));
                    WebFilesProcessor.this.log.info("JS : " + file.toString() + ", CHECKSUM: " + checksum);
                    return checksum;
                } catch (IOException e) {
                    WebFilesProcessor.this.log.debug("Failed to generate checksum for file: " + file.getAbsolutePath(), e);
                    return getRandomVersion(fileName);
                }
            }

            private String getRandomVersion(String fileName) {
                String randomString = UUID.randomUUID().toString();
                WebFilesProcessor.this.log.info("JS NOT FOUND: " + fileName + ", VERSION: " + randomString);
                return randomString;
            }

        }
    }

    private class WebFileWriter {

        void write(Path filePath,
                   String content) {
            try {
                Path relativeFilePath = WebFilesProcessor.this.webFilesDirectory.relativize(filePath);
                Path destinationFilePath = WebFilesProcessor.this.webAppOutputDirectory.resolve(relativeFilePath);
                Files.createDirectories(destinationFilePath.getParent());
                Files.write(destinationFilePath, content.getBytes());
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't write modified web file: ", e);
            }
        }
        
    }

    private class WebFilesVisitor extends SimpleFileVisitor<Path> {

        private ScriptTagProcessor scriptTagProcessor = new ScriptTagProcessor();
        private WebFileWriter webFileWriter = new WebFileWriter();

        @Override
        public FileVisitResult visitFile(Path filePath,
                                         BasicFileAttributes attrs) throws IOException {
            String contentType = Files.probeContentType(filePath);
            if (!contentType.contains("image")) {
                log.debug("Checking file: " + filePath);
                this.scriptTagProcessor.process(filePath)
                        .ifPresent(content -> this.webFileWriter.write(filePath, content));
            } else {
                log.debug("File is an image or video: " + filePath);
            }
            return FileVisitResult.CONTINUE;
        }

    }

}

