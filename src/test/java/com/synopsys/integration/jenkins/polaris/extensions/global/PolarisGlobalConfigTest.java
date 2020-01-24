package com.synopsys.integration.jenkins.polaris.extensions.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.compression.FilterServletOutputStream;
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
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({SynopsysCredentialsHelper.class, PolarisServerConfig.class})
public class PolarisGlobalConfigTest {

    private static final String POLARIS_URL = "https://polaris.domain.com";
    private static final String INVALID_URL = "polaris.domain.com";
    private static final String POLARIS_CREDENTIALS_ID = "123";
    private static final String POLARIS_TIMEOUT_STRING = "30";
    private static final int POLARIS_TIMEOUT_INT = 30;
    private static final String CONFIG_XML_CONTENTS = "<?xml version='1.0' encoding='UTF-8'?>\n"
                                                                   + "<com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig>\n"
                                                                   + "  <polarisUrl>https://dev01.dev.polaris.synopsys.com</polarisUrl>\n"
                                                                   + "  <polarisCredentialsId>0424ba25-4607-4a81-a809-0220c44d0fc1</polarisCredentialsId>\n"
                                                                   + "  <polarisTimeout>120</polarisTimeout>\n"
                                                                   + "</com.synopsys.integration.jenkins.polaris.extensions.global.PolarisGlobalConfig>";
    public static final String POLARIS_TOKEN = "testToken";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testInvalidUrl() {

        // This config should be found to be invalid (invalid URL)
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection(INVALID_URL, POLARIS_CREDENTIALS_ID, POLARIS_TIMEOUT_STRING);

        assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
        assertTrue(formValidation.getMessage().contains("valid"));
        assertTrue(formValidation.getMessage().contains("URL"));
    }

    @Test
    public void testInvalidCredentialsId() {

        // This config should be found to be invalid (invalid credentials ID)
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection(POLARIS_URL, POLARIS_CREDENTIALS_ID, POLARIS_TIMEOUT_STRING);

        assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
        assertTrue(formValidation.getMessage().contains("token"));
    }

    @Test
    public void testValidConfig() {
        PowerMockito.mockStatic(SynopsysCredentialsHelper.class);
        Mockito.when(SynopsysCredentialsHelper.getApiTokenByCredentialsId(POLARIS_CREDENTIALS_ID)).thenReturn(Optional.of(POLARIS_TOKEN));

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

        // This config should be found to be valid
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection(POLARIS_URL, POLARIS_CREDENTIALS_ID, POLARIS_TIMEOUT_STRING);

        assertEquals(FormValidation.Kind.OK, formValidation.kind);
    }

    @Test
    public void testConfigDotXmlGet() throws ServletException, ParserConfigurationException, IOException {
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final StaplerRequest req = Mockito.mock(StaplerRequest.class);
        final StaplerResponse rsp = Mockito.mock(StaplerResponse.class);
        Mockito.when(req.getMethod()).thenReturn("GET");

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ServletOutputStream servletOutputStream = new FilterServletOutputStream(byteArrayOutputStream);
        Mockito.when(rsp.getOutputStream()).thenReturn(servletOutputStream);

        final File pluginConfigFile = new File(Jenkins.getInstance().getRootDir(),PolarisGlobalConfig.class.getName() + ".xml");
        FileUtils.write(pluginConfigFile, CONFIG_XML_CONTENTS, StandardCharsets.UTF_8);

        // The config file created above should be written to rsp output stream (byteArrayOutputStream)
        detectGlobalConfig.doConfigDotXml(req, rsp);

        assertEquals(CONFIG_XML_CONTENTS, byteArrayOutputStream.toString());
    }

    @Test
    public void testConfigDotXmlPost() throws ServletException, ParserConfigurationException, IOException {
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final StaplerRequest req = Mockito.mock(StaplerRequest.class);
        final StaplerResponse rsp = Mockito.mock(StaplerResponse.class);
        Mockito.when(req.getMethod()).thenReturn("POST");

        final BufferedReader reader = new BufferedReader(new StringReader(CONFIG_XML_CONTENTS));
        Mockito.when(req.getReader()).thenReturn(reader);

        final File pluginConfigFile = new File(Jenkins.getInstance().getRootDir(), PolarisGlobalConfig.class.getName() + ".xml");
        assertFalse(pluginConfigFile.exists());

        // the XML read from the request should get saved to the plugin config file
        detectGlobalConfig.doConfigDotXml(req, rsp);

        assertTrue(pluginConfigFile.exists());
        final String pluginConfigFileContents = FileUtils.readFileToString(pluginConfigFile, StandardCharsets.UTF_8);
        assertEquals(CONFIG_XML_CONTENTS, pluginConfigFileContents);
    }

    @Test
    public void testDoFillPolarisCredentialsIdItems() {
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final ListBoxModel listBoxModel = detectGlobalConfig.doFillPolarisCredentialsIdItems();
        assertEquals("- none -", listBoxModel.get(0).name);
        assertEquals("", listBoxModel.get(0).value);
        assertEquals(false, listBoxModel.get(0).selected);
    }
}
