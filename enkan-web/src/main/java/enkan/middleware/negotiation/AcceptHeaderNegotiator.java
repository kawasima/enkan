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
    private static final Pattern ACCEPT_DELIMITER = Pattern.compile("[\\s\\n\\r]*;[\\s\\n\\r]*");

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

    public <T> AcceptFragment<T> parseAcceptFragment(String accept, Class<? extends T> acceptType) {
        String[] tokens = ACCEPT_DELIMITER.split(accept);
        if (tokens.length > 0) {
            Optional<Double> q = Arrays.stream(tokens).skip(1)
                    .map(ACCEPT_FRAGMENT_PARAM_RE::matcher)
                    .filter(Matcher::find)
                    .filter(m -> m.group(1).equals("q"))
                    .map(m -> parseQ(m.group(2)))
                    .findFirst();
            if (acceptType.equals(MediaType.class)) {
                return new AcceptFragment(CodecUtils.parseMediaType(tokens[0]),
                        q.orElse(1.0));
            } else if (acceptType.equals(String.class)) {
                return new AcceptFragment(tokens[0], q.orElse(1.0));
            }
        }
        return null;
    }

    protected Function<AcceptFragment<MediaType>, AcceptFragment<MediaType>> createServerWeightFunc(Set<MediaType> allowedTypes) {
        return fragment -> {
            Optional<MediaType> matched = allowedTypes.stream()
                    .map(mt -> {
                        if (fragment.fragment.isCompatible(mt)) {
                            return fragment.fragment;
                        } else if(mt.isCompatible(fragment.fragment)) {
                            return mt;
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();
            return matched
                    .map(mediaType -> new AcceptFragment<>(mediaType, 1.0))
                    .orElse(fragment);

        };
    }

    protected Optional<String> selectBest(Set<String> candidates, Function<String, Double> scoreFunc) {
        return candidates.stream()
                .map(c -> new AcceptFragment<>(c, scoreFunc.apply(c)))
                .sorted(Comparator.comparing(AcceptFragment::getQ, reverseOrder()))
                .filter(af -> af.getQ() > 0.0)
                .map(AcceptFragment::getFragment)
                .findFirst();
    }

    @Override
    public MediaType bestAllowedContentType(String acceptsHeader, Set<String> allowedTypes) {
        Function<AcceptFragment<MediaType>, AcceptFragment<MediaType>> serverWeightFunc = createServerWeightFunc(allowedTypes.stream()
                .map(CodecUtils::parseMediaType)
                .collect(Collectors.toSet()));
        return Arrays.stream(ACCEPTS_DELIMITER.split(acceptsHeader))
                .map(accept -> parseAcceptFragment(accept, MediaType.class))
                .filter(Objects::nonNull)
                .map(serverWeightFunc)
                .min(Comparator.comparing(AcceptFragment::getQ, reverseOrder()))
                .map(af -> af.fragment)
                .orElse(null);
    }

    @Override
    public String bestAllowedCharset(String acceptsHeader, Set<String> available) {
        Map<String, Double> accepts = Arrays
                .stream(ACCEPTS_DELIMITER.split(acceptsHeader))
                .map(accept -> parseAcceptFragment(accept, String.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        AcceptFragment::getFragment,
                        AcceptFragment::getQ));
        return selectBest(available, charset -> {
            charset = charset.toLowerCase(Locale.US);
            return accepts.getOrDefault(charset,
                    accepts.getOrDefault("*",
                            charset.equals("ISO_8859_1") ? 1.0 : 0.0));
        }).orElse(null);
    }

    @Override
    public String bestAllowedEncoding(String acceptsHeader, Set<String> available) {
        Map<String, Double> accepts = Arrays
                .stream(ACCEPTS_DELIMITER.split(acceptsHeader))
                .map(accept -> parseAcceptFragment(accept, String.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        AcceptFragment::getFragment,
                        AcceptFragment::getQ));
        available = new HashSet<>(available);
        available.add("identity");
        return selectBest(available, encoding ->
                accepts.getOrDefault("encoding",
                        accepts.get("*")))
                .orElseGet(() -> {
                    if (! (accepts.getOrDefault("identity", 1.0) == 0.0
                            || (accepts.getOrDefault("*", 1.0) == 0 && !accepts.containsKey("identity")))) {
                        return "identity";
                    } else {
                        return null;
                    }
                });

    }

    @Override
    public String bestAllowedLanguage(String acceptsHeader, Set<String> available) {
        Map<String, Double> accepts = Arrays
                .stream(ACCEPTS_DELIMITER.split(acceptsHeader))
                .map(accept -> parseAcceptFragment(accept, String.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        AcceptFragment::getFragment,
                        AcceptFragment::getQ));
        Function<String, Double> score = langtag -> {
            for (String x = langtag;  x != null; x = x.substring(0, x.lastIndexOf('-'))) {
                Double q = accepts.get(x);
                if (q != null) return q;
                if (!x.contains("-")) break;
            }
            return Objects.equals(langtag, "*") ? 0.01 : 0;
        };

        return selectBest(available, score).orElse(null);
    }


    private static class AcceptFragment<T> implements Serializable {
        private final double q;
        private final T fragment;

        public AcceptFragment(T fragment, double q) {
            this.fragment = fragment;
            this.q = q;
        }

        public T getFragment() {
            return fragment;
        }

        public double getQ() {
            return q;
        }
    }

}
