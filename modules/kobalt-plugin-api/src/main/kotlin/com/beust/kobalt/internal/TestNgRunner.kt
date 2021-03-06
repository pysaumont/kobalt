package com.beust.kobalt.internal

import com.beust.kobalt.TestConfig
import com.beust.kobalt.api.IClasspathDependency
import com.beust.kobalt.api.KobaltContext
import com.beust.kobalt.api.Project
import com.beust.kobalt.misc.KFiles
import com.beust.kobalt.misc.warn
import java.io.File

class TestNgRunner : GenericTestRunner() {

    override val mainClass = "org.testng.TestNG"

    override val dependencyName = "testng"

    override val annotationPackage = "org.testng"

    fun defaultOutput(project: Project) = KFiles.joinDir(project.buildDirectory, "test-output")

    override fun args(project: Project, context: KobaltContext, classpath: List<IClasspathDependency>,
            testConfig: TestConfig) = arrayListOf<String>().apply {
        var addOutput = true
        testConfig.testArgs.forEach { arg ->
            if (arg == "-d") addOutput = false
        }

        if (testConfig.testArgs.size == 0) {
            // No arguments, so we'll do it ourselves. Either testng.xml or the list of classes
            val testngXml = File(project.directory, KFiles.joinDir("src", "test", "resources", "testng.xml"))
            if (testngXml.exists()) {
                add(testngXml.absolutePath)
            } else {
                val testClasses = findTestClasses(project, context, testConfig)
                if (testClasses.size > 0) {
                    if (addOutput) {
                        add("-d")
                        add(defaultOutput(project))
                    }
                    addAll(testConfig.testArgs)

                    add("-testclass")
                    add(testClasses.joinToString(","))
                } else {
                    if (! testConfig.isDefault) warn("Couldn't find any test classes for ${project.name}")
                    // else do nothing: since the user didn't specify an explicit test{} directive, not finding
                    // any test sources is not a problem
                }
            }
        } else {
            if (addOutput) {
                add("-d")
                add(defaultOutput(project))
            }
            addAll(testConfig.testArgs)
        }
    }
}
