package enkan.component;

import enkan.collection.OptionMap;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static enkan.util.ThreadingUtils.some;

/**
 * @author kawasima
 */
public abstract class WebServerComponent extends SystemComponent {
    @DecimalMax("65535")
    @DecimalMin("1")
    private Integer port = 80;

    private String host = "0.0.0.0";

    private boolean isHttp = true;
    private boolean isSsl = false;
    private int sslPort = 443;

    private File keystoreFile;
    private KeyStore keystore;
    private String keystorePassword;

    private File truststoreFile;
    private KeyStore truststore;
    private String truststorePassword;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isHttp() {
        return isHttp;
    }

    public void setHttp(boolean http) {
        isHttp = http;
    }

    public boolean isSsl() {
        return isSsl;
    }

    public void setSsl(boolean ssl) {
        isSsl = ssl;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public void setKeystoreFile(File keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public void setKeystorePath(String keystorePath) {
        if (keystorePath != null && !keystorePath.isEmpty()) {
            this.keystoreFile = new File(keystorePath);
        }
    }

    public KeyStore getKeystore() {
        if (keystore == null && keystoreFile != null) {
            try {
                keystore = KeyStore.getInstance("JKS");
                try (InputStream in = new FileInputStream(keystoreFile)) {
                    keystore.load(in, some(keystorePassword, String::toCharArray).orElse(null));
                }
            } catch (KeyStoreException e) {
                throw new MisconfigurationException("core.KEY_STORE", e.getMessage(), e);
            } catch (CertificateException e) {
                throw new MisconfigurationException("core.CERTIFICATE", e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                throw new UnreachableException(e);
            } catch (IOException e) {
                throw new MisconfigurationException("core.CANT_READ_KEYSTORE_FILE", truststoreFile, e);
            }
        }
        return keystore;
    }

    public void setKeystore(KeyStore keystore) {
        this.keystore = keystore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public void setTruststoreFile(File truststoreFile) {
        this.truststoreFile = truststoreFile;
    }

    public void setTruststorePath(String truststorePath) {
        if (truststorePath != null && !truststorePath.isEmpty()) {
            this.truststoreFile = new File(truststorePath);
        }
    }

    public KeyStore getTruststore() {
        if (truststore == null && truststoreFile != null) {
            try {
                truststore = KeyStore.getInstance("JKS");
                try (InputStream in = new FileInputStream(truststoreFile)) {
                    truststore.load(in, some(truststorePassword, String::toCharArray).orElse(null));
                }
            } catch (KeyStoreException e) {
                throw new MisconfigurationException("core.KEY_STORE", e);
            } catch (CertificateException e) {
                throw new MisconfigurationException("core.CERTIFICATE", e);
            } catch (NoSuchAlgorithmException e) {
                throw new UnreachableException(e);
            } catch (IOException e) {
                throw new MisconfigurationException("core.CANT_READ_TRUSTSTORE_FILE", truststoreFile, e);
            }
        }

        return truststore;
    }

    public void setTruststore(KeyStore truststore) {
        this.truststore = truststore;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    protected OptionMap buildOptionMap() {
        OptionMap options = OptionMap.of(
                "http?", isHttp,
                "ssl?",  isSsl,
                "port",  port,
                "host",  host,
                "sslPort", sslPort);

        KeyStore keystore = getKeystore();
        if (keystore != null) options.put("keystore", keystore);
        options.put("keystorePassword", keystorePassword);

        KeyStore truststore = getTruststore();
        if (truststore != null) options.put("truststore", truststore);
        options.put("truststorePassword", truststorePassword);

        return options;
    }
}
