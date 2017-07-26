node {
    git url: 'git@github.com:facebook/screenshot-tests-for-android.git'
    try {
        sh './gradlew :plugin:test'
    } catch (any) {
        currentBuild.status = 'FAILURE'
    }

    properties([
        pipelineTriggers([
            [$class: "GitHubPushTrigger"],
        ])
    ])
}
