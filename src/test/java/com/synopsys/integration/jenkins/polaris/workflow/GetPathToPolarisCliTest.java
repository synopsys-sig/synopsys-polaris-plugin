package com.synopsys.integration.jenkins.polaris.workflow;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.OperatingSystemType;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ OperatingSystemType.class })
public class GetPathToPolarisCliTest {

    public static final String POLARIS_CLI_HOME_RELPATH = "src/test/resources/polarisCliHome";

    @Test
    public void test() throws IntegrationException {

        PowerMockito.mockStatic(OperatingSystemType.class);
        Mockito.when(OperatingSystemType.determineFromSystem()).thenReturn(OperatingSystemType.LINUX);

        final GetPathToPolarisCli getPathToPolarisCli = new GetPathToPolarisCli(POLARIS_CLI_HOME_RELPATH);
        final String pathToPolarisCliExecutable = getPathToPolarisCli.call();

        assertTrue(pathToPolarisCliExecutable.endsWith(POLARIS_CLI_HOME_RELPATH));
    }
}
