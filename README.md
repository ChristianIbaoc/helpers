What's up?
This is a repository that contains helper files for QA.
To access the package in a maven way, follow these steps:

1.) Generate your OWN PAT in Github
    - Profile --> Dev Settings --> PAT
    - Assign it with read packages scope

2.) Go to your user folder C:/Users/<kimi-no-namae-wa>
3.) Find the ".m2" folder and edit/create the "settings.xml" file.
4.) Add this block:

    <servers>
     <server>
       <id>github</id>
       <username>YOUR_USERNAME</username>
       <password>YOUR_PAT</password>
     </server>
   </servers>

6.) For your maven project, add this in the pom.xml:

        <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/ChristianIbaoc/helpers</url>
        </repository>
        </repositories>

        <dependencies>
        <dependency>
            <groupId>com.yourname.helpers</groupId>
            <artifactId>selenium-abstractions</artifactId>
            <version>1.0.0 (change this to latest)</version>
        </dependency>
        </dependencies>

## Releasing a new version

[.github/workflows/publish.yml](.github/workflows/publish.yml) publishes the package to GitHub Packages, but it only runs when a GitHub Release is created — pushing a tag by itself does not trigger it. Steps to cut a release:

1.) Bump the version in pom.xml:

    <version>X.Y.Z</version>

2.) Commit and push the version bump:

    git add pom.xml
    git commit -m "Bump version to X.Y.Z"
    git push

3.) Tag the commit (match the pom.xml version, prefixed with "v") and push the tag:

    git tag vX.Y.Z
    git push origin vX.Y.Z

4.) Create a GitHub Release from that tag — this is the step that actually kicks off the publish workflow:
    - On GitHub: Releases --> Draft a new release
    - Pick the tag you just pushed (vX.Y.Z)
    - Publish the release

5.) Update the `<version>` in the usage snippet above (and any other places it's referenced) to X.Y.Z.