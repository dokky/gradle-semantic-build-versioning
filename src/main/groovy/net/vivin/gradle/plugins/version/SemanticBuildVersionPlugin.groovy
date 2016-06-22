package net.vivin.gradle.plugins.version

import net.vivin.gradle.plugins.version.git.VersionComponent
import net.vivin.gradle.plugins.version.tasks.BumpTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.tooling.BuildException

class SemanticBuildVersionPlugin implements Plugin<Project> {

    private final Logger logger = Logging.getLogger("semantic-build-versioning")

    @Override
    void apply(Project project) {
        SemanticBuildVersion version = new SemanticBuildVersion(project)

        if(project.tasks.findByName('release') == null) {
            project.task('release') << {
                logger.lifecycle("Releasing $project.name version $project.version")
            }
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?release/
        }) {
            version.snapshot = false
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?promoteToRelease/
        }) {
            if(version.bump != null || version.autobump) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.promoteToRelease = true
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?bumpPreRelease/
        }) {
            if(version.bump != null || version.autobump || version.promoteToRelease) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.bump = VersionComponent.PRERELEASE
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?bumpPatch/
        }) {
            if(version.bump != null || version.autobump || version.promoteToRelease) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.bump = VersionComponent.PATCH
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?bumpMinor/
        }) {
            if(version.bump != null || version.autobump || version.promoteToRelease) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.bump = VersionComponent.MINOR
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?bumpMajor/
        }) {
            if(version.bump != null || version.autobump || version.promoteToRelease) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.bump = VersionComponent.MAJOR
        }

        if(project.gradle.startParameter.taskNames.find { name ->
            name ==~ /:?autobump/
        }) {
            if(version.bump != null || version.promoteToRelease) {
                throw new BuildException("Only one of promoteToRelease, bumpPreRelease, bumpPatch, bumpMinor, bumpMajor, or autobump can be used", null)
            }

            version.autobump = true
        }

        project.setVersion(version)
        project.task('promoteToRelease', group: "versioning")
        project.task('bumpPreRelease', type: BumpTask, group: "versioning")
        project.task('bumpPatch', type: BumpTask, group: "versioning")
        project.task('bumpMinor', type: BumpTask, group: "versioning")
        project.task('bumpMajor', type: BumpTask, group: "versioning")
        project.task('autobump', type: BumpTask, group: "versioning")

        project.task('printVersion') << {
            println project.version
        }

        project.tasks.getByName('printVersion').mustRunAfter('promoteToRelease').mustRunAfter(project.tasks.withType(BumpTask))
        project.tasks.getByName('release').mustRunAfter('promoteToRelease').mustRunAfter(project.tasks.withType(BumpTask))
    }
}
