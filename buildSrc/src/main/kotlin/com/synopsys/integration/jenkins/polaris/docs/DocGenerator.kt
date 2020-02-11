/**
 * buildSrc
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.jenkins.polaris.docs

import com.synopsys.integration.detect.docs.content.Terms
import com.synopsys.integration.detect.docs.markdown.MarkdownOutputFormat
import freemarker.template.Configuration
import freemarker.template.Template
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream

open class GenerateDocsTask : DefaultTask() {
    @TaskAction
    fun generateDocs() {
        val outputDir = project.file("docs/generated");

        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val templateProvider = TemplateProvider(project.file("docs/templates"), project.version.toString())
        handleContent(outputDir, templateProvider)
    }

    private fun handleContent(outputDir: File, templateProvider: TemplateProvider) {
        val templatesDir = File(project.projectDir, "docs/templates")
        project.file("docs/templates").walkTopDown().forEach {
            if (it.canonicalPath.endsWith((".ftl"))) {
                createContentMarkdownFromTemplate(templatesDir, it, outputDir, templateProvider)
            }
        }
    }

    private fun createContentMarkdownFromTemplate(templatesDir: File, templateFile: File, baseOutputDir: File, templateProvider: TemplateProvider) {
        val helpContentTemplateRelativePath = templateFile.toRelativeString(templatesDir)
        val outputFile = deriveOutputFileForContentTemplate(templatesDir, templateFile, baseOutputDir)
        println("Generating markdown from template file: ${helpContentTemplateRelativePath} --> ${outputFile.canonicalPath}")
        createFromFreemarker(templateProvider, helpContentTemplateRelativePath, outputFile, HashMap<String, String>())
    }

    private fun deriveOutputFileForContentTemplate(contentDir: File, helpContentTemplateFile: File, baseOutputDir: File): File {
        val templateSubDir = helpContentTemplateFile.parentFile.toRelativeString(contentDir)
        val outputDir = File(baseOutputDir, templateSubDir)
        val outputFile = File(outputDir, "${helpContentTemplateFile.nameWithoutExtension}.md")
        return outputFile
    }

    private fun createFromFreemarker(templateProvider: TemplateProvider, outputDir: File, templateName: String, data: Any) {
        createFromFreemarker(templateProvider, "$templateName.ftl", File(outputDir, "$templateName.md"), data);
    }

    private fun createFromFreemarker(templateProvider: TemplateProvider, templateRelativePath: String, to: File, data: Any) {
        to.parentFile.mkdirs()
        val template = templateProvider.getTemplate(templateRelativePath)
        FileOutputStream(to, true).buffered().writer().use { writer ->
            template.process(data, writer)
        }
    }
}

class TemplateProvider(templateDirectory: File, projectVersion: String) {
    private val configuration: Configuration = Configuration(Configuration.VERSION_2_3_26);

    init {
        configuration.setDirectoryForTemplateLoading(templateDirectory)
        configuration.defaultEncoding = "UTF-8"
        configuration.registeredCustomOutputFormats = listOf(MarkdownOutputFormat.INSTANCE);

        val terms = Terms()
        terms.termMap.put("program_version", projectVersion)
        configuration.setSharedVaribles(terms.termMap)
    }

    fun getTemplate(templateName: String): Template {
        val template =  configuration.getTemplate(templateName)
        return template;
    }
}