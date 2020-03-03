package io.stallion.tests.unit;

import io.stallion.secrets.SecretsCommandLineManager;
import io.stallion.services.Log;
import org.junit.Test;

public class SecretsManagerTest {
    @Test
    public void testSecrets() {


        SecretsCommandLineManager m = new SecretsCommandLineManager();
        String password = m.findPasswordInKeyring();
        Log.info("password found: {0}", password);
    }
}
