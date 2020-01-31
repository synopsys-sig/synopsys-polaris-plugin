package com.synopsys.integration.jenkins.polaris.substeps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

// IF env var TEST_POLARIS_URL is set, testInstall() downloads the Polaris CLI from that Polaris server
public class FindOrInstallPolarisCliTest {
    private static final long NUM_POLARIS_EXECUTABLES_EXPECTED = 3L;
    private static final String POLARIS_VERSIONFILE_NAME = "polarisVersion.txt";
    private static final String POLARIS_EXE_PATH_SUBSTRING = "/bin/polaris";


    @Test
    public void testInstall() throws IntegrationException {

        final String polarisServerUrl = System.getenv("TEST_POLARIS_URL");
        if (StringUtils.isBlank(polarisServerUrl)) {
            System.out.println("Environment variable TEST_POLARIS_URL is not set. Skipping FindOrInstallPolarisCliTest.testInstall().");
            return;
        }
        System.out.printf("Attempting to download Polaris CLI from %s\n", polarisServerUrl);

        final JenkinsIntLogger jenkinsIntLogger = Mockito.mock(JenkinsIntLogger.class);

        final int timeout = 90;
        final String proxyHost = null;
        final int proxyPort = 0;
        final String proxyUsername = null;
        final String proxyPassword = null;
        final String proxyNtlmDomain = null;
        final String proxyNtlmWorkstation = null;
        final String requestedInstallationDirParentPath = "out/test/polariscli";

        final File requestedInstallationDirParent = new File(requestedInstallationDirParentPath);
        FileUtils.deleteQuietly(requestedInstallationDirParent); // Force a new download
        assertFalse(requestedInstallationDirParent.exists());

        final FindOrInstallPolarisCli findOrInstallPolarisCli = new FindOrInstallPolarisCli(jenkinsIntLogger, polarisServerUrl, timeout, proxyHost, proxyPort,
        proxyUsername, proxyPassword, proxyNtlmDomain, proxyNtlmWorkstation, requestedInstallationDirParentPath);

        // Test
        final String returnedInstallationDirPath = findOrInstallPolarisCli.call();

        // Verify
        long numVersionFiles = countMatchingFilepaths(requestedInstallationDirParent, POLARIS_VERSIONFILE_NAME);
        assertEquals(1L, numVersionFiles);

        // Verify that 3 polaris executables were installed
        final File returnedInstallationDir = new File(returnedInstallationDirPath);
        long numExesFoundInReturnedInstallationDir = countMatchingFilepaths(returnedInstallationDir, POLARIS_EXE_PATH_SUBSTRING);
        assertEquals(NUM_POLARIS_EXECUTABLES_EXPECTED, numExesFoundInReturnedInstallationDir);

        // Verify that they were installed in the location we requested
        long numExesFoundInRequestedInstallationDirParent = countMatchingFilepaths(requestedInstallationDirParent, POLARIS_EXE_PATH_SUBSTRING);
        assertEquals(NUM_POLARIS_EXECUTABLES_EXPECTED, numExesFoundInRequestedInstallationDirParent);
    }

    private long countMatchingFilepaths(final File installationDir, final String pathSubstring) {
        final Collection<File> installationDirContents = FileUtils.listFilesAndDirs(installationDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        return installationDirContents.stream()
                                .map(File::getAbsolutePath)
                                .filter(filePath -> filePath.contains(pathSubstring))
                                .count();
    }
}
