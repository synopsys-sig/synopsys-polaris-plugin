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

    @Test
    public void test() throws MalformedURLException {
        // TODO extract constants
        final JenkinsIntLogger logger = Mockito.mock(JenkinsIntLogger.class);
        final IntEnvironmentVariables intEnvironmentVariables = Mockito.mock(IntEnvironmentVariables.class);
        final JenkinsVersionHelper jenkinsVersionHelper = Mockito.mock(JenkinsVersionHelper.class);
        final PolarisGlobalConfig polarisGlobalConfig = Mockito.mock(PolarisGlobalConfig.class);
        final PolarisServerConfigBuilder polarisServerConfigBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        Mockito.when(polarisGlobalConfig.getPolarisServerConfigBuilder()).thenReturn(polarisServerConfigBuilder);
        final Map<BuilderPropertyKey, String> props = new HashMap<>();
        props.put(new BuilderPropertyKey("TESTKEY"), "testValue");
        Mockito.when(polarisServerConfigBuilder.getProperties()).thenReturn(props);

        // TODO Best option I've found so far: create some real objects here; any way to reduce that?
        final URL polarisUrl = new URL("http://test.polaris.com");
        final AuthenticationSupport authenticationSupport = Mockito.mock(AuthenticationSupport.class);
        final ProxyInfo proxyInfo = Mockito.mock(ProxyInfo.class);
        final PolarisServerConfig polarisServerConfig = new PolarisServerConfig(polarisUrl, 0, "testAccessToken", proxyInfo, false, new Gson(), authenticationSupport);

        Mockito.when(polarisServerConfigBuilder.build()).thenReturn(polarisServerConfig);
        Mockito.when(jenkinsVersionHelper.getPluginVersion("synopsys-polaris")).thenReturn("1.2.3");

        final CreatePolarisEnvironment createPolarisEnvironment = new CreatePolarisEnvironment(logger, polarisGlobalConfig, intEnvironmentVariables, jenkinsVersionHelper);

        final SubStepResponse<Object> response = createPolarisEnvironment.run();

        assertTrue(response.isSuccess());

        // verify: polarisServerConfig.populateEnvironmentVariables(...)
        Mockito.verify(intEnvironmentVariables).put("TESTKEY", "testValue");
        Mockito.verify(intEnvironmentVariables).put("POLARIS_SERVER_URL", "http://test.polaris.com");
        Mockito.verify(intEnvironmentVariables).put("POLARIS_ACCESS_TOKEN", "testAccessToken");
    }
}
