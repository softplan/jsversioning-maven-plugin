[![Build Status](https://travis-ci.org/softplan/jsversioning-maven-plugin.svg?branch=wip)](https://travis-ci.org/softplan/jsversioning-maven-plugin)

#JAVASCRIPT VERSIONING

This plugins searches scripts tags inside web files, html, jsp's and actually any files inside the web app directory with the exception of images.

This plugin will change

		<script language="javascript" type="text/JavaScript" src="script.js"></script>
    
to

		<script language="javascript" type="text/JavaScript" src="script.js?n=version"></script>

The version value will be the checksum of the javascript file, if it can be found inside the project. In this example script.js checksum. 
So files that don't have any changes keep the same name and version, maintaining cache.
If the file is not found inside the web apps directory, this plugin will generate a random value and use it as version.

###USAGE

You have to define the execution like the example below, the default phase of the plugin is process-resources.

	<build>
		<plugins>
			...
			<plugin>
				<groupId>br.com.softplan.unj</groupId>
				<artifactId>jsversioning</artifactId>
				<version>0.0.1</version>
				<executions>
					<execution>
						<id>jsVersioning</id>
						<goals>
							<goal>js-versioning</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			...
		</plugins>
	</build>

By default the plugin will send the output to ${project.build.directory}/temp. You can use the war plugin to then copy the output to the generated war.

WAR plugin configuration to demonstrate:
        
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.0.0</version>
        <configuration>
            <webResources>
                <resource>
                    <directory>${project.build.directory}/temp</directory>
                </resource>
            </webResources>
        </configuration>
    </plugin>

###PARAMETERS

**webFilesDirectory** (*Default value = ${basedir}/src/main/webapp*): Where the plugin will look for the script tags.

**webappOutputDirectory** (*Default value = ${project.build.directory}/temp*): The output folder of the execution.

###MAVEN-CENTRAL

In the works 
