package kotowari.scope;

import java.util.HashMap;
import java.util.Map;

import static kotowari.scope.ExportableScope.*;

/**
 * @author kawasima
 */
public class ExportSetting {
    private Map<ExportableScope, String> exports;

    public static final ExportSetting DEFAULT_EXPORTS = ExportSetting.of(
            REQUEST, "request",
            SESSION, "session",
            PARAMS,  "params",
            USER_PRINCIPAL, "userPrincipal",
            CONVERSATION, "conversation",
            CONVERSATION_STATE, "conversationState"
    );

    private ExportSetting() {
        exports = new HashMap<>();
    }

    public static ExportSetting of(Object... keyOrVals) {
        ExportSetting setting = new ExportSetting();
        for (int i=0; i<keyOrVals.length; i += 2) {
            setting.exports.put((ExportableScope) keyOrVals[i], (String) keyOrVals[i+1]);
        }
        return setting;
    }

    public String getExportName(ExportableScope scope) {
        return exports.get(scope);
    }

    public boolean contains(ExportableScope scope) {
        return exports.containsKey(scope);
    }
}
