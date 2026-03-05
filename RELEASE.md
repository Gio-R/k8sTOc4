# Release process for k8sToC4 CLI

1) Update version in pom.xml for the new release. 
2) Run: mvn -B -DskipTests=false clean package. 
3) Create a git tag for the release, e.g. `v1.0.0`. 
4) Push commits and tag to the remote and open a release in GitHub. 
5) If publishing to Maven Central, configure OSSRH credentials and perform a release flow as per Maven Central guidelines. 

Note: This document is intentionally concise; adapt steps to your release workflow.
