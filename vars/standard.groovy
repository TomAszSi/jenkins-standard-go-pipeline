def call(String goToolName = 'go-1.13', String golangCiVersion = 'v1.13.5') {
    pipeline {
        agent any
        tools {
            go "$goToolName"
        }
        environment {
            GO111MODULE = 'on'
        }
        stages {
            stage('Compile') {
                steps {
                    sh 'go build'
                }
            }
            stage('Test') {
                environment {
                    CODECOV_TOKEN = credentials('CODECOV_TOKEN')
                }
                steps {
                    sh 'go test ./... -coverprofile=coverage.txt'
                    sh "curl -s https://codecov.io/bash | bash -s -"
                }
            }
            stage('Code Analysis') {
                steps {
                    sh "curl -sfL https://install.goreleaser.com/github.com/golangci/golangci-lint.sh | bash -s -- -b $GOPATH/bin $golangCiVersion"
                    sh 'golangci-lint run'
                }
            }
            stage('Release') {
                when {
                    buildingTag()
                }
                environment {
                    GITHUB_TOKEN = credentials('GITHUB_TOKEN')
                }
                steps {
                    sh 'curl -sL https://git.io/goreleaser | bash'
                }
            }
        }
    }
}
