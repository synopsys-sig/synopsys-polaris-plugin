package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;

public class GetPolarisCliResponseContentTest {

    public static final String WORKSPACE_RELPATH = "src/test/resources/workspace";
    public static final String TEST_CLI_SCAN_JSON_FILE_CONTENTS = "test cli-scan.json file contents";

    @Test
    public void test() throws PolarisIntegrationException {

        final GetPolarisCliResponseContent getPolarisCliResponseContent = new GetPolarisCliResponseContent(WORKSPACE_RELPATH);
        final String cliScanJsonFileContents = getPolarisCliResponseContent.call();

        assertTrue(cliScanJsonFileContents.contains(TEST_CLI_SCAN_JSON_FILE_CONTENTS));
    }
}
