package com.synopsys.integration.jenkins.polaris.extensions.tools;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolLocationNodeProperty;
import hudson.tools.ZipExtractionInstaller;
import jenkins.model.Jenkins;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ToolLocationNodeProperty.class, ToolInstallation.class, Jenkins.class})
public class PolarisCliTest {

    public static final String INSTALLER_URL = "https://nodejs.org/dist/v4.2.4/node-v4.2.4-linux-x64.tar.gz";
    public static final String CLI_LABEL = "testLabel";
    public static final String CLI_HOME = "testHome";
    public static final String CLI_NAME = "testName";

    @Test
    public void testForNode() throws IOException, InterruptedException {

        List<ToolInstaller> installers = new ArrayList<ToolInstaller>();
        installers.add(new ZipExtractionInstaller("testLabel", INSTALLER_URL, ""));
        List<InstallSourceProperty> properties = new ArrayList<>();
        properties.add(new InstallSourceProperty(installers));

        final PolarisCli polarisCli = new PolarisCli("testName", "testHome", properties);
        final Node node = Mockito.mock(Node.class);
        final TaskListener taskListener = Mockito.mock(TaskListener.class);

        PowerMockito.mockStatic(ToolLocationNodeProperty.class);
        Mockito.when(ToolLocationNodeProperty.getToolHome(Mockito.any(Node.class), Mockito.any(ToolInstallation.class), Mockito.any(TaskListener.class))).thenReturn("testHome");

        final PolarisCli createdPolarisCli = polarisCli.forNode(node, taskListener);

        assertEquals(CLI_NAME, createdPolarisCli.getName());
        assertEquals(CLI_HOME, createdPolarisCli.getHome());
        final InstallSourceProperty installSourceProperty = (InstallSourceProperty)createdPolarisCli.getProperties().get(0);
        final ZipExtractionInstaller zipExtractionInstaller = (ZipExtractionInstaller)installSourceProperty.installers.get(0);
        assertEquals(INSTALLER_URL, zipExtractionInstaller.getUrl());
        assertEquals(CLI_LABEL, zipExtractionInstaller.getLabel());
    }
}
