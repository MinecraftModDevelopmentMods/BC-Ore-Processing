#!/bin/groovy
pipeline {
    agent any

    stages() {
        stage("Checkout") {
            steps {
                checkout scm
            }
        }

        stage("Clean & Setup") {
            steps {
                sh """set -x
                      chmod 755 gradlew
                   """
            }
        }

        stage("Build") {
            steps {
                sh """set -x
                      ./gradlew curseforge
                   """

                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true
            }
        }

//        stage("Maven") {
//            when {
//                expression {
//                    return !env.BRANCH_NAME.startsWith('PR-')
//                }
//            }
//            steps {
//                sh """set -x
//                      ./gradlew uploadArchives
//                   """
//            }
//        }
    }
}