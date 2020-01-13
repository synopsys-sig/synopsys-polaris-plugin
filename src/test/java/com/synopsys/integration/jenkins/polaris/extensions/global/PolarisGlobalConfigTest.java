package com.synopsys.integration.jenkins.polaris.extensions.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.jenkins.SynopsysCredentialsHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.client.ConnectionResult;

import hudson.util.FormValidation;

@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({SynopsysCredentialsHelper.class, PolarisServerConfig.class})
public class PolarisGlobalConfigTest {

    public static final String POLARIS_URL = "https://polaris.domain.com";
    public static final String POLARIS_CREDENTIALS_ID = "123";
    public static final String POLARIS_TIMEOUT_STRING = "30";
    public static final int POLARIS_TIMEOUT_INT = 30;
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testInvalidCredentialsId() {
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection("https://blackduck.domain.com", "123", "30");

        assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
        assertTrue(formValidation.getMessage().contains("token"));
        System.out.printf("Message: %s\n", formValidation.getMessage());
    }


    @Test
    public void testValidConfig() {
        PowerMockito.mockStatic(SynopsysCredentialsHelper.class);
        Mockito.when(SynopsysCredentialsHelper.getApiTokenByCredentialsId(POLARIS_CREDENTIALS_ID)).thenReturn(Optional.of("testToken"));

        final PolarisServerConfigBuilder polarisServerConfigBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        PowerMockito.mockStatic(PolarisServerConfig.class);
        Mockito.when(PolarisServerConfig.newBuilder()).thenReturn(polarisServerConfigBuilder);
        Mockito.when(polarisServerConfigBuilder.setUrl(POLARIS_URL)).thenReturn(polarisServerConfigBuilder);
        Mockito.when(polarisServerConfigBuilder.setTimeoutInSeconds(POLARIS_TIMEOUT_INT)).thenReturn(polarisServerConfigBuilder);
        final PolarisServerConfig polarisServerConfig = Mockito.mock(PolarisServerConfig.class);
        Mockito.when(polarisServerConfigBuilder.build()).thenReturn(polarisServerConfig);
        final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = Mockito.mock(AccessTokenPolarisHttpClient.class);
        Mockito.when(polarisServerConfig.createPolarisHttpClient(Mockito.any(IntLogger.class))).thenReturn(accessTokenPolarisHttpClient);
        Mockito.when(accessTokenPolarisHttpClient.attemptConnection()).thenReturn(ConnectionResult.SUCCESS(200));

        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection(POLARIS_URL, POLARIS_CREDENTIALS_ID, POLARIS_TIMEOUT_STRING);

        assertEquals(FormValidation.Kind.OK, formValidation.kind);
        System.out.printf("Message: %s\n", formValidation.getMessage());
    }
}
