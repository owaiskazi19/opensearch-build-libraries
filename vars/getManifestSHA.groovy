/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
import jenkins.InputManifest

Map call(Map args = [:]) {
    String inputManifest = args.inputManifest ?: "manifests/${INPUT_MANIFEST}"
    String jobName = args.jobName ?: "${JOB_NAME}"

    buildManifest(
        args + [
            inputManifest: inputManifest,
            lock: true
        ]
    )

    String manifestLock = "${inputManifest}.lock"
    String manifestSHA = sha1(manifestLock)
    echo "Manifest SHA: ${manifestSHA}"

    def inputManifestObj = new InputManifest(readYaml(file: manifestLock))
    String shasRoot = inputManifestObj.getSHAsRoot(jobName)
    String manifestSHAPath = "${shasRoot}/${manifestSHA}.yml"
    echo "Manifest lock: ${manifestLock}"
    echo "Manifest SHA path: ${manifestSHAPath}"

    Boolean manifestSHAExists = false
    withCredentials([string(credentialsId: 'jenkins-aws-account-public', variable: 'AWS_ACCOUNT_PUBLIC'),
    string(credentialsId: 'jenkins-artifact-bucket-name', variable: 'ARTIFACT_BUCKET_NAME')]) {
        withAWS(role: 'opensearch-bundle', roleAccount: "${AWS_ACCOUNT_PUBLIC}", duration: 900, roleSessionName: 'jenkins-session') {
            if (s3DoesObjectExist(bucket: "${ARTIFACT_BUCKET_NAME}", path: manifestSHAPath)) {
                manifestSHAExists = true
        }
    }
    }

    echo "Manifest SHA exists: ${manifestSHAExists}"

    return [
        sha: manifestSHA,
        lock: manifestLock,
        path: manifestSHAPath,
        exists: manifestSHAExists
    ]
}
