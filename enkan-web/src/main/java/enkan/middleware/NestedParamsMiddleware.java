package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
@Middleware(name = "nestedParams", dependencies = {"params"})
public class NestedParamsMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private static final Pattern RE_NESTED_NAME = Pattern.compile("^(?s)(.*?)((?:\\[.*?\\])*)$");
    private static final Pattern RE_NESTED_TOKEN = Pattern.compile("\\[(.*?)\\]");
    protected Function<String, String[]> parseNestedKeys = (paramName) -> {
        if (paramName == null) return new String[]{};

        Matcher m = RE_NESTED_NAME.matcher(paramName);
        List<String> keys = new ArrayList<>();

        if (m.find()) {
            keys.add(m.group(1));
            String ks = m.group(2);
            if (ks != null && !ks.isEmpty()) {
                Matcher mt = RE_NESTED_TOKEN.matcher(ks);
                while (mt.find()) {
                    keys.add(mt.group(1));
                }
            } else {
                return new String[]{ m.group(1) };
            }
        }

        return keys.toArray(new String[0]);
    };

    protected Parameters assocVector(Parameters map, String key, Object value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        return assocConj(map, key, value);
    }

    /**
     * Association
     *
     * <ul>
     *   <li>foo[bar]=[aaa,bbb] =&gt; {foo: {bar: [aaa, bbb]}}</li>
     *   <li>for[][bar]=[aaa,bbb] =&gt; {foo: [{bar: aaa}, {bar: bbb}]}</li>
     *   <li>for[]=[aaa,bbb] =&gt; {foo: [aaa, bbb]}</li>
     * </ul>
     *
     * @param map    a Parameters
     * @param key    a String key
     * @param value  a Object associated with the key
     * @return a Parameters contains the given key and value
     */
    @SuppressWarnings("unchecked")
    protected Parameters assocConj(Parameters map, String key, Object value) {
        Object cur = map.getRawType(key);
        if (cur != null) {
            if (cur instanceof List) {
                if (value instanceof List) {
                    // cur is instance of List
                    ((List<Object>) cur).addAll((List<?>) value);
                } else {
                    // cur is instance of List
                    ((List<Object>) cur).add(value);
                }
            } else {
                List<Object> values = new ArrayList<>();
                values.add(cur);
                if (value instanceof List) {
                    values.addAll((List<?>) value);
                } else {
                    values.add(value);
                }
                map.put(key, values);
            }
        } else {
            if (value instanceof List) {
                List<?> values = (List) value;
                if (values.size() > 1) {
                    assocVector(map, key, value);
                } else if (values.size() == 1) {
                    map.put(key, values.get(0));
                }
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    protected Object assocNested(Parameters map, String[] keys, List<String> values) {
        if (keys.length > 0) {
            String[] ks = new String[keys.length - 1];

            if (ks.length > 0) {
                System.arraycopy(keys, 1, ks, 0, ks.length);

                String j = ks[0];
                if (j.isEmpty()) {
                    // Array
                    String[] js = new String[keys.length - 2];
                    if (js.length > 0) {
                        System.arraycopy(keys, 2, js, 0, js.length);
                    }

                    List<Object> nestedList = map.getList(keys[0]);

                    for (int i = nestedList.size(); i < values.size(); i++) nestedList.add(null);

                    for (int i = 0; i < values.size(); i++) {
                        List<String> vs = new ArrayList<>();
                        vs.add(values.get(i));
                        if (js.length > 0) {
                            nestedList.set(i,
                                    assocNested((Parameters) Optional.ofNullable(nestedList.get(i)).orElse(Parameters.empty())
                                            , js, vs));
                        } else {
                            nestedList.set(i, vs.get(0));
                        }

                    }
                    if (map.containsKey(keys[0])) {
                        map.replace(keys[0], nestedList);
                    } else {
                        map.put(keys[0], nestedList);
                    }
                    return map;
                } else {
                    // Map
                    Parameters submap = (Parameters) map.getRawType(keys[0]);
                    if (submap == null) submap = Parameters.empty();
                    map.put(keys[0], assocNested(submap, ks, values));
                    return map;
                }
            } else {
                return assocConj(map, keys[0], values);
            }
        } else {
            return values;
        }
    }

    public HttpRequest nestedParamsRequest(HttpRequest request, Function<String, String[]> keyParser) {
        Parameters params = request.getParams();
        Parameters nestedParams = Parameters.empty();

        params.keySet().forEach(key -> assocNested(nestedParams, keyParser.apply(key), params.getList(key)));
        request.setParams(nestedParams);
        return request;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        return castToHttpResponse(chain.next(nestedParamsRequest(request, parseNestedKeys)));
    }
}
