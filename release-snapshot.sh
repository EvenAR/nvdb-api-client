#./gradlew clean release -Prelease.pushTagsOnly -Prelease.disableRemoteCheck
./gradlew build publishMavenJavaPublicationToSnapshotRepository
