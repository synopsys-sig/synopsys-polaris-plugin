package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;

public class GetPolarisCliResponseContentTest {

    public static final String WORKSPACE_RELPATH = "src/test/resources/workspace";

    @Test
    public void test() throws PolarisIntegrationException, IOException {
        final String jsonContent = FileUtils.readFileToString(new File("src/test/resources/workspace/.synopsys/polaris/cli-scan.json"));

        final GetPolarisCliResponseContent getPolarisCliResponseContent = new GetPolarisCliResponseContent(WORKSPACE_RELPATH);
        final String cliScanJsonFileContents = getPolarisCliResponseContent.call();

        assertTrue(cliScanJsonFileContents.contains(jsonContent));
    }
}
