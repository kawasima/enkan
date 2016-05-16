package enkan.predicate;

import enkan.Env;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kawasima
 */
public class EnvPredicate<REQ> implements PrintablePredicate<REQ> {
    private Set<String> allowedEnv = new HashSet<>();

    public EnvPredicate(String... envs) {
        allowedEnv.addAll(Arrays.asList(envs));
    }

    @Override
    public boolean test(REQ req) {
        return allowedEnv.contains(Env.getString("enkan.env", "development"));
    }
}
