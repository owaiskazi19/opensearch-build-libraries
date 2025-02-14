/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
import jenkins.BuildManifest
void call(Map args = [:]) {

    def buildManifest = new BuildManifest(readYaml(file: args.buildManifest))
    echo "Create yum repo metadata and repo file ${args.buildManifest}"

    def filename = buildManifest.build.getFilename()
    def name = buildManifest.build.name
    def version = buildManifest.build.version
    def repoFilePath = "rpm/dist/${filename}"

    sh([
        'createrepo',
        "\"${repoFilePath}\"",
    ].join(' '))

    def repoFileContent = [
        "[${filename}-${version}-${BUILD_NUMBER}-staging]",
        "name=${name} ${version} ${BUILD_NUMBER} Staging",
        "baseurl=${args.baseUrl}/${repoFilePath}/",
        "enabled=1",
        "gpgcheck=0",
        "autorefresh=1",
        "type=rpm-md"
    ].join('\n')

    writeFile file: "${repoFilePath}/${filename}-${version}.staging.repo", text: repoFileContent
}
