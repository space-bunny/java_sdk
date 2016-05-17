package io.spacebunny;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Utilities {

    /**
     * Function that add custom CA
     * @param path of the certificate
     */
    public static void addCA(String path) throws SpaceBunnyConfigurationException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"),
                    "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath),
                    "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            File f = new File(path);
            if (f.exists()) {
                try (InputStream caInput = new BufferedInputStream(
                        new FileInputStream(path))) {
                    Certificate crt = cf.generateCertificate(caInput);

                    keyStore.setCertificateEntry(f.getName(), crt);
                }

                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLContext.setDefault(sslContext);
            } else {
                throw new SpaceBunnyConfigurationException("Error with custom CA path.");
            }
        } catch (Exception e) {
            throw new SpaceBunnyConfigurationException("Error with custom CA.");
        }
    }
}
