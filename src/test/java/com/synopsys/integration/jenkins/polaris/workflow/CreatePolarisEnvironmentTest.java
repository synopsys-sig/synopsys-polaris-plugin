package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.jenkins.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.stepworkflow.SubStepResponse;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class CreatePolarisEnvironmentTest {
    private static final String TEST_KEY = "TESTKEY";
    private static final String TEST_VALUE = "testValue";
    private static final String POLARIS_URL = "http://test.polaris.com";
    private static final String POLARIS_ACCESS_TOKEN = "testAccessToken";

    @Test
    public void test() throws MalformedURLException {
        final JenkinsIntLogger logger = Mockito.mock(JenkinsIntLogger.class);
        final IntEnvironmentVariables intEnvironmentVariables = Mockito.mock(IntEnvironmentVariables.class);
        final JenkinsVersionHelper jenkinsVersionHelper = Mockito.mock(JenkinsVersionHelper.class);
        final PolarisGlobalConfig polarisGlobalConfig = Mockito.mock(PolarisGlobalConfig.class);
        final PolarisServerConfigBuilder polarisServerConfigBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        Mockito.when(polarisGlobalConfig.getPolarisServerConfigBuilder()).thenReturn(polarisServerConfigBuilder);
        final Map<BuilderPropertyKey, String> props = new HashMap<>();
        props.put(new BuilderPropertyKey(TEST_KEY), TEST_VALUE);
        Mockito.when(polarisServerConfigBuilder.getProperties()).thenReturn(props);
        final URL polarisUrl = Mockito.mock(URL.class);
        Mockito.when(polarisUrl.toString()).thenReturn(POLARIS_URL);

        // Creating a real PolarisServerConfig object simplifies verification of env variable setting
        final PolarisServerConfig polarisServerConfig = new PolarisServerConfig(polarisUrl, 0, POLARIS_ACCESS_TOKEN, Mockito.mock(ProxyInfo.class), false,
            Mockito.mock(Gson.class), Mockito.mock(AuthenticationSupport.class));
        Mockito.when(polarisServerConfigBuilder.build()).thenReturn(polarisServerConfig);
        Mockito.when(jenkinsVersionHelper.getPluginVersion("synopsys-polaris")).thenReturn("1.2.3");

        // Test
        final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, polarisGlobalConfig, intEnvironmentVariables, jenkinsVersionHelper);
        final SubStepResponse<Object> response = createPolarisEnvironment.run();

        // Verify
        assertTrue(response.isSuccess());
        Mockito.verify(intEnvironmentVariables).put(TEST_KEY, TEST_VALUE);
        Mockito.verify(intEnvironmentVariables).put("POLARIS_SERVER_URL", POLARIS_URL);
        Mockito.verify(intEnvironmentVariables).put("POLARIS_ACCESS_TOKEN", POLARIS_ACCESS_TOKEN);
    }
}
