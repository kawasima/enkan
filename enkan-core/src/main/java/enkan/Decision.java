package enkan;

/**
 * @author kawasima
 */
public interface Decision<REQ> {
    boolean decide(REQ req);
}
