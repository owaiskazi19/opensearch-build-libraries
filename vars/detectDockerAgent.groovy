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
    String manifest = args.manifest ?: "manifests/${INPUT_MANIFEST}"
    def inputManifest = new InputManifest(readYaml(file: manifest))
    dockerImage = inputManifest.ci?.image?.name ?: 'opensearchstaging/ci-runner:ci-runner-centos7-v1'
    dockerArgs = inputManifest.ci?.image?.args
    // Using default javaVersion as openjdk-17
    String javaVersion = 'openjdk-17'
    java.util.regex.Matcher jdkMatch = (dockerArgs =~ /openjdk-\d+/)
    if (jdkMatch.find()) {
        def jdkMatchLine = jdkMatch[0]
        javaVersion = jdkMatchLine
    }
    echo "Using Docker image ${dockerImage} (${dockerArgs})"
    echo "Using java version ${javaVersion}"
    return [
        image: dockerImage,
        args: dockerArgs,
        javaVersion: javaVersion
    ]
}
