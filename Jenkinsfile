@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	ciNotificationChannel: '#pd-cxz-cicd',
	integrationTests : [
		[
			artifactName: 'sse-integration-test',
			skip: false
		]
	]
)
