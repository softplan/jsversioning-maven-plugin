[![Build Status](https://travis-ci.org/softplan/jsversioning-maven-plugin.svg?branch=wip)](https://travis-ci.org/softplan/jsversioning-maven-plugin)

#JAVASCRIPT VERSIONING

You have something like this defined in your webapps files:

		<script language="javascript" type="text/JavaScript" src="script.js"></script>

This plugin will change to:

		<script language="javascript" type="text/JavaScript" src="script.js?n=version"></script>

The version will be user defined, and if its not defined the plugin will generate a random UUID.

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
							<goal>versioning</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			...
		</plugins>
	</build>

By default the plugin will copy the output of the execution to ${project.build.directory}/temp

###PARAMETERS

**webFilesDirectory** (*Default value = ${basedir}/src/main/webapp*): Where the plugin will look for the script tags.

**webappOutputDirectory** (*Default value = ${project.build.directory}/temp*): The output folder of the execution.

**version**: If the user wants to use a specific version on the scripts.

###MAVEN-CENTRAL

In the works 
