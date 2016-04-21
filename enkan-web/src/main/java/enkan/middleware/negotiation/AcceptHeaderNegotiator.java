package enkan.middleware.negotiation;

import enkan.util.CodecUtils;

import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

/**
 * @author kawasima
 */
public class AcceptHeaderNegotiator implements ContentNegotiator {
    private static final Pattern ACCEPT_FRAGMENT_PARAM_RE = Pattern.compile("([^()<>@,;:\"/\\[\\]?={} 	]+)=([^()<>@,;:\"/\\[\\]?={} 	]+|\"[^\"]*\")$");
    private static final Pattern ACCEPTS_DELIMITER = Pattern.compile("[\\s\\n\\r]*,[\\s\\n\\r]*");
    private static final Pattern ACCEPT_DELIMITER = Pattern.compile("\\s*;\\s*");

    private double clamp(double min, double max, double val) {
        return Math.max(Math.min(max, val), min);
    }

    public double parseQ(String qstr) {
        try {
            return clamp(0.0, 1.0, Double.parseDouble(qstr));
        } catch (Throwable e) {
            return 0.0;
        }
    }

    public AcceptFragment parseAcceptFragment(String accept) {
        String[] tokens = ACCEPT_DELIMITER.split(accept);
        if (tokens.length > 0) {
            MediaType mediaType = CodecUtils.parseMediaType(tokens[0]);
            Optional<Double> q = Arrays.stream(tokens).skip(1)
                    .map(param -> ACCEPT_FRAGMENT_PARAM_RE.matcher(param))
                    .filter(Matcher::find)
                    .filter(m -> m.group(1).equals("q"))
                    .map(m -> parseQ(m.group(2)))
                    .findFirst();
            return new AcceptFragment(mediaType, q.orElse(1.0));
        }
        return null;
    }

    protected Function<AcceptFragment, AcceptFragment> createServerWeightFunc(Set<MediaType> allowedTypes) {
        return fragment -> {
            Optional<MediaType> matched = allowedTypes.stream()
                    .map(mt -> {
                        if (fragment.mediaType.isCompatible(mt)) {
                            return fragment.mediaType;
                        } else if(mt.isCompatible(fragment.mediaType)) {
                            return mt;
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();
            if (matched.isPresent()) {
                return new AcceptFragment(matched.get(), 1.0);
            } else {
                return fragment;
            }

        };
    }

    @Override
    public MediaType bestAllowedContentType(String accept, Set<String> allowedTypes) {
        Function<AcceptFragment, AcceptFragment> serverWeightFunc = createServerWeightFunc(allowedTypes.stream()
                .map(CodecUtils::parseMediaType)
                .collect(Collectors.toSet()));
        return Arrays.stream(ACCEPTS_DELIMITER.split(accept))
                .map(this::parseAcceptFragment)
                .filter(Objects::nonNull)
                .map(serverWeightFunc)
                .sorted(Comparator.comparing(AcceptFragment::getQ, reverseOrder()))
                .findFirst()
                .get()
                .mediaType;
    }


    private static class AcceptFragment implements Serializable {
        private double q;
        private MediaType mediaType;

        public AcceptFragment(MediaType mediaType, double q) {
            this.mediaType = mediaType;
            this.q = q;
        }
        public AcceptFragment(String primary, String sub, double q) {
            this(new MediaType(primary, sub), q);
        }

        public double getQ() {
            return q;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%s/%s; q=%.1f",
                    mediaType.getType(), mediaType.getSubtype(), q);
        }

    }

}
