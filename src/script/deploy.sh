#! /bin/sh
#
# This script deploys the artifacts to https://oss.sonatype.org/
# see https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7b.StageExistingArtifacts
#
# (c)reated: 10-Mar-2018 by ob@aosd.de
#

# set up some constants
URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
VERSION=2.7.1
options="gpg:sign-and-deploy-file -Durl=$URL -DrepositoryId=sonatype-nexus-staging"

# passphrase is needed for signing
echo "passphrase for GPG: "
stty_orig=`stty -g`
stty -echo
read passphrase
stty $stty_orig

options="gpg:sign-and-deploy-file -Durl=$URL -DrepositoryId=sonatype-nexus-staging -Dgpg.passphrase=$passphrase"

deploy_pom_for() {
    subdir=$1
	module=$2
	pushd $subdir
	echo deploying $module in $subdir...
	mvn -N $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION.pom
    popd
    echo
}

deploy_jar_for() {
    subdir=$1
	module=$2
	pushd $subdir
	echo deploying $module in $subdir...
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION.jar
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION-sources.jar -Dclassifier=sources
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION-javadoc.jar -Dclassifier=javadoc
    popd
    echo
}

# start deployment
deploy_pom_for . clazzfish
deploy_jar_for monitor clazzfish-monitor
deploy_jar_for agent clazzfish-agent
deploy_jar_for jdbc clazzfish-jdbc
deploy_pom_for spi clazzfish-spi
deploy_jar_for spi/git clazzfish-spi-git
