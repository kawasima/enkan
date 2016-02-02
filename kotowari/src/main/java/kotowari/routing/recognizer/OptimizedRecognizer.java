package kotowari.routing.recognizer;

import enkan.collection.OptionMap;
import kotowari.routing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class OptimizedRecognizer implements Recognizer {
    private SegmentNode tree;
    private List<Route> routes;

    public String[] toPlainSegments(String str) {
        str = str.replaceAll("^/+", "").replaceAll("/+$", "");
        return str.split("\\.[^/]+\\/+|" + "[" + RegexpUtils.escape(String.join("", RouteBuilder.SEPARATORS)) + "]+|\\.[^/]+\\Z");
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
        optimize();
    }

    @Override
    public boolean isOptimized() {
        return tree != null;
    }

    @Override
    public void optimize() {
        tree = new SegmentNode(0);

        int i = -1;
        for (Route route : routes) {
            i += 1;
            SegmentNode node = tree;
            String[] segments = toPlainSegments(String.join("", route.getSegments().stream().map(Segment::toString).collect(Collectors.toList())));
            for (String seg : segments) {
                if (!seg.isEmpty() && seg.charAt(0) == ':') {
                    seg = ":dynamic";
                }
                if (node.isEmpty() || !seg.equals(node.lastChild().getLabel()))
                    node.add(new SegmentNode(seg, i));
                node = node.lastChild();
            }
        }
    }
    private int calcIndex(String[] segments, SegmentNode node, int level) {
        if (node.size() <= 1 || segments.length == level)
            return node.getIndex();
        String seg = segments[level];
        for (SegmentNode item : node.getChildNodes()) {
            if (":dynamic".equals(item.getLabel()) || seg.equals(item.getLabel())) {
                return calcIndex(segments, item, level + 1);
            }
        }
        return node.getIndex();
    }

    @Override
    public OptionMap recognize(String path, String method) {
        String[] segments = toPlainSegments(path);

        int index = calcIndex(segments, tree, 0);
        while (index < routes.size()) {
            OptionMap result = routes.get(index).recognize(path, method);
            if (result != null) return result;
            index += 1;
        }
        return OptionMap.of();
    }

    private class SegmentNode {
        private int index;
        private String label;
        private List<SegmentNode> childNodes;

        public SegmentNode(int index) {
            this(null, index);
        }

        public SegmentNode(String label, int index) {
            this.index = index;
            this.label = label;
            childNodes = new ArrayList<>();
        }

        public void add(SegmentNode child) {
            childNodes.add(child);
        }

        public boolean isEmpty() {
            return childNodes.isEmpty();
        }

        public SegmentNode lastChild() {
            if (isEmpty())
                return null;
            return childNodes.get(childNodes.size() - 1);
        }

        public String getLabel() {
            return label;
        }

        public int getIndex() {
            return index;
        }

        public List<SegmentNode> getChildNodes() {
            return childNodes;
        }
        public int size() {
            return childNodes.size();
        }
    }
}
