package com.synopsys.integration.jenkins.polaris.extensions.buildstep;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;

public class JobTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test() throws Exception {
//        final WaitForIssues waitForIssues = new WaitForIssues();
//        waitForIssues.setBuildStatusForIssues(ChangeBuildStatusTo.SUCCESS);
//        waitForIssues.setJobTimeoutInMinutes(5);

        // Jenkins CLI tool


//        final FreeStyleProject project = jenkinsRule.createFreeStyleProject("free");
        //////////////////
//        project.getBuildersList().add(new Shell("echo hello"));
//        final FreeStyleBuild build = project.scheduleBuild2(0).get();
//        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
//        final String s = FileUtils.readFileToString(build.getLogFile());
//        System.out.printf("******** %s\n", s);

        /////////////////////////////////////
//        final PolarisBuildStep before = new PolarisBuildStep();
//        before.setWaitForIssues(waitForIssues);
//        before.setPolarisCliName("testcliname");
//        before.setPolarisArguments("test polaris arguments");
//        project.getBuildersList().add(before);

//        final FreeStyleBuild build = project.scheduleBuild2(0).get();
//        System.out.println(build.getDisplayName() + " completed");
//        final String s = FileUtils.readFileToString(build.getLogFile());
//        System.out.printf("******** %s\n", s);

        final FreeStyleProject project = jenkinsRule.createFreeStyleProject("free");
        final JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();
        final WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);

//        webClient.goTo("buildStatus/buildIcon?job=free", "image/svg+xml");
        final HtmlPage managePage = webClient.goTo("manage");
        // Sometimes I get the page, but there's a stack trace in the log.
        final List<HtmlForm> manageForms = managePage.getForms();
        for (final HtmlForm form : manageForms) {
            final String formString = form.toString();
            System.out.printf("\nFORM: %s\n\n\n", formString);
        }
//        WebClientUtil.waitForJSExec(webClient);
        //exceptionListener.assertHasException();
//        final ScriptException e = exceptionListener.getScriptException();
//        if (e != null) {
//            System.out.printf("Exception: %s\n", e.getMessage());
//        } else {
//            System.out.printf("exceptionListener.getScriptException() returned null\n");
//        }
        //Assert.assertTrue(e.getMessage().contains("simulated error"));
//        jenkinsRule.buildAndAssertSuccess(project);
//        webClient.login("admin", "admin");
        final HtmlPage projectPage = webClient.getPage(project /*,"configure"*/);
        final List<HtmlForm> forms = projectPage.getForms();
        for (final HtmlForm form : forms) {
            final String formString = form.toString();
            System.out.printf("\nFORM: %s\n\n\n", formString);
        }
//        final HtmlForm form = page.getFormByName("config");
//        jenkinsRule.submit(form);
//
//        final PolarisBuildStep after = project.getBuildersList().get(PolarisBuildStep.class);
//
//        jenkinsRule.assertEqualBeans(before,after,"prop1,prop2,prop3,...");
    }
}
