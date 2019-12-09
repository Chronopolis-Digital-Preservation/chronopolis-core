# Creating Releases

Releases are mainly handled through the CI pipeline and should not involve much
work other than bumping the maven version to a RELEASE and tagging the release
version.

* create maven release with `mvn versions:set -DnewVersion=a.b.c-RELEASE`
* run `mvn clean package` to ensure we still build
* `find . -type f -name "*.versionsBackup" -print0 | xargs -0 rm` to remove unwanted pom backups
* `git commit -a` to commit new poms
* `git push` and wait for ci to succeed
* `git tag release-a.b.c` push to remote

# CI Pipeline

The `.gitlab-ci.yml` has two deploy tasks defined:

* deploy:openjdk
  * Deploys maven artifacts to lib-artifacts 
* deploy:rpms
  * Builds rpms using `build_rpms.sh` and pushes them to lib-artifacts
