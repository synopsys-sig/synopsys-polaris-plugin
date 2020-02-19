package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.jenkins.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.stepworkflow.SubStepResponse;

import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.TaskListener;
import jenkins.model.GlobalConfiguration;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GlobalConfiguration.class, JenkinsVersionHelper.class })
public class PolarisWorkflowStepFactoryTest {

    // TODO move setup/mocking out of the test for readability
    @Test
    public void testCreateStepCreatePolarisEnvironment() throws IOException, InterruptedException {

        final String polarisCliName = "testpolariscli";
        final String polarisArguments = "test polaris arguments";
        final Node node = Mockito.mock(Node.class);
        final FilePath workspace = Mockito.mock(FilePath.class);
        final Map<String, String> testEnvVarsMap = new HashMap<>();
        testEnvVarsMap.put("envvarkey", "env var value");
        final EnvVars envVars = new EnvVars(testEnvVarsMap);
        final Launcher launcher = Mockito.mock(Launcher.class);
        final TaskListener listener = Mockito.mock(TaskListener.class);

        final ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        final PrintStream logPrintStream = new PrintStream(logOutputStream);
        Mockito.when(listener.getLogger()).thenReturn(logPrintStream);

        final PolarisWorkflowStepFactory factory = new PolarisWorkflowStepFactory(polarisCliName, polarisArguments, node, workspace, envVars, launcher, listener);
        final CreatePolarisEnvironment createPolarisEnvironment = factory.createStepCreatePolarisEnvironment();

        // GlobalConfiguration.all().get(PolarisGlobalConfig.class);
        PowerMockito.mockStatic(GlobalConfiguration.class);
        final ExtensionList<GlobalConfiguration> registeredDescriptors = Mockito.mock(ExtensionList.class);
        Mockito.when(GlobalConfiguration.all()).thenReturn(registeredDescriptors);
        final PolarisGlobalConfig polarisGlobalConfig = Mockito.mock(PolarisGlobalConfig.class);
        Mockito.when(registeredDescriptors.get(PolarisGlobalConfig.class)).thenReturn(polarisGlobalConfig);

        // final PolarisServerConfigBuilder polarisServerConfigBuilder = polarisGlobalConfig.getPolarisServerConfigBuilder();
        final PolarisServerConfigBuilder polarisServerConfigBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        Mockito.when(polarisGlobalConfig.getPolarisServerConfigBuilder()).thenReturn(polarisServerConfigBuilder);

        // polarisServerConfigBuilder.getProperties()
        final BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey("globalconfigkey");
        final Map<BuilderPropertyKey, String> builderProperties = new HashMap<>();
        builderProperties.put(builderPropertyKey, "global config value");
        Mockito.when(polarisServerConfigBuilder.getProperties()).thenReturn(builderProperties);

        final PolarisServerConfig polarisServerConfig = Mockito.mock(PolarisServerConfig.class);
        Mockito.when(polarisServerConfigBuilder.build()).thenReturn(polarisServerConfig);

        PowerMockito.mockStatic(JenkinsVersionHelper.class);
        Mockito.when(JenkinsVersionHelper.getPluginVersion("synopsys-polaris")).thenReturn("1.2.3");

        // Test created object
        final SubStepResponse<Object> response =  createPolarisEnvironment.run();

        assertTrue(response.isSuccess());
        final String logContents = logOutputStream.toString();
        assertTrue(logContents.contains("Synopsys Polaris for Jenkins version: 1.2.3"));
    }
}
