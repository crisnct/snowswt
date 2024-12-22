**Snowing simulator - demo for SWT capabilities**
- Requires jdk 22 in order to run
- Requires the following changes in maven settings.xml
```
<?xml version="1.0" encoding="UTF-8"?>
  <settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd" xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  
  <mirrors>
      <mirror>
          <id>netbeans-http-unblocker</id>
          <mirrorOf>external:http:*</mirrorOf>
          <name>netbeans-http-unblocker</name>
          <url>http://netbeans.apidesign.org/maven2/</url>
          <blocked>false</blocked>
      </mirror>
		
      <mirror>
          <id>nattable-releases</id>
          <mirrorOf>external:http:*</mirrorOf>
          <name>nattable-releases</name>
          <url>http://repo.eclipse.org/content/repositories/nattable-releases/</url>
          <blocked>false</blocked>
      </mirror>
  </mirrors>

  <profiles>
    <profile>
      <id>artifactory-maven-central</id>

      <repositories>
        <repository>
          <id>central</id>
          <name>Central Repository</name>
          <url>https://repo.maven.apache.org/maven2</url>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>

      <pluginRepositories>
        <pluginRepository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>Central Repository</name>
          <url>https://repo.maven.apache.org/maven2</url>
        </pluginRepository>
      </pluginRepositories>

  </profile>

</profiles>

<activeProfiles>
  <activeProfile>artifactory-maven-central</activeProfile>
</activeProfiles>

</settings>
```
