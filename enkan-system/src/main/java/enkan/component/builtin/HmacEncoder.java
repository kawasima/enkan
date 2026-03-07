package enkan.component.builtin;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class HmacEncoder extends SystemComponent<HmacEncoder> {
    private static final Logger LOG = Logger.getLogger(HmacEncoder.class.getName());
    private static final String DEFAULT_SECRET = "This is secret";
    private static final char[] HEX_CHARACTERS = "0123456789ABCDEF".toCharArray();
    private String algorithm = "HmacSHA256";
    private String secret = DEFAULT_SECRET;

    private SecretKeySpec secretKeySpec;

    public HmacEncoder() {
        createKeySpec();
    }

    public String encodeToHex(String text) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            byte[] macBytes = mac.doFinal(text.getBytes());
            char[] macChars = new char[macBytes.length * 2];
            for (int i = 0; i < macBytes.length; i++) {
                macChars[i * 2    ] = HEX_CHARACTERS[(macBytes[i] >> 4) & 0xF];
                macChars[i * 2 + 1] = HEX_CHARACTERS[macBytes[i] & 0xF];
            }
            return new String(macChars);
        } catch (NoSuchAlgorithmException ex) {
            throw new MisconfigurationException("core.NO_SUCH_ALGORITHM", algorithm, "`HmacMD5`, `HmacSHA1`, and `HmacSHA256`",ex);
        } catch (InvalidKeyException ex) {
            throw new MisconfigurationException("core.INVALID_KEY", secretKeySpec, ex);
        }

    }

    @Override
    protected ComponentLifecycle<HmacEncoder> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override
            public void start(HmacEncoder component) {
                if (DEFAULT_SECRET.equals(component.secret)) {
                    LOG.warning("HmacEncoder is using the default secret. Set a strong secret before running in production.");
                }
            }

            @Override
            public void stop(HmacEncoder component) {

            }
        };
    }

    private void createKeySpec() {
        try {
            Mac.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new MisconfigurationException("core.NO_SUCH_ALGORITHM", algorithm,
                    "`HmacMD5`, `HmacSHA1`, and `HmacSHA256`", e);
        }
        secretKeySpec = new SecretKeySpec(secret.getBytes(), algorithm);
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        createKeySpec();
    }

    public void setSecret(String secret) {
        this.secret = secret;
        createKeySpec();
    }

    @Override
    public String toString() {
        return "#HmacEncoder {\n"
                + "  \"algorithm\": \""+ algorithm + "\",\n"
                + "  \"dependencies\": " + dependenciesToString()
                + "\n}";
    }
}
