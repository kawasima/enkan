package enkan.decision;

import enkan.Decision;

/**
 * @author kawasima
 */
public class AllDecision<REQ> implements Decision<REQ> {
    @Override
    public boolean decide(REQ req) {
        return true;
    }
}
