import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.component.external.model.TestFixturesSupport
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the

class FixturesConsumerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val fixtures =
            project.configurations.create(FixturesSupport.FIXTURES_CONSUMER_CONFIGURATION_NAME) {
                isCanBeConsumed = false
                isCanBeResolved = true
            }
        project.dependencies.extensions.create<FixturesContainer>(
            FixturesSupport.FIXTURES_EXTENSION_NAME, fixtures, project.objects
        )
    }
}

abstract class FixturesContainer(fixtures: Configuration, private val factory: ObjectFactory) {
    private val fixturesConfiguration: Configuration = fixtures
    fun fixturesDependency(project: Dependency): FileCollection {
        val fixturesFileName = buildString {
            append(project.name)
            append(TestFixturesSupport.TEST_FIXTURES_CAPABILITY_APPENDIX)
            append(".jar")
        }
        return fixturesConfiguration.copy().run {
            factory.fileCollection().from(files.single { it.name == fixturesFileName })
        }
    }

    fun dependency(project: Dependency): FileCollection {
        return fixturesConfiguration.copy().run {
            factory.fileCollection().from(files.single { it.name == "${project.name}.jar" })
        }
    }
}

// For copy project test-fixtures as JAR file
fun DependencyHandler.projectFixtures(path: String): FileCollection {
    the(FixturesContainer::class).run {
        val dependency = testFixtures(project(path))

        val res = add(FixturesSupport.FIXTURES_CONSUMER_CONFIGURATION_NAME, dependency)!!
        println("name: ${dependency.name}")

        val fixturesFileName = buildString {
            append(dependency.name)
            append(TestFixturesSupport.TEST_FIXTURES_CAPABILITY_APPENDIX)
            append(".jar")
        }


        println("filename: $fixturesFileName")
        return fixturesDependency(dependency)
    }
}

// For copy project as JAR file
fun DependencyHandler.projectCopy(path: String): FileCollection {
    the(FixturesContainer::class).run {
        val dependency = project(path)
        add(FixturesSupport.FIXTURES_CONSUMER_CONFIGURATION_NAME, dependency)!!
        return dependency(dependency)
    }
}
