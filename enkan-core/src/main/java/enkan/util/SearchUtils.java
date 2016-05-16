package enkan.util;

import java.nio.ByteBuffer;

/**
 * @author kawasima
 */
public class SearchUtils {
    public static int kmp(ByteBuffer buf, byte[] sought) {
        if (sought == null || sought.length == 0) {
            return -1;
        }

        if (sought.length == 1) {
            int i = buf.position();
            while (buf.hasRemaining()) {
                if (buf.get(i) == sought[0]) {
                    return i;
                }
                i += 1;
            }
            return -1;
        }

        int i = 0;
        int[] t = kmpTable(sought);

        int m = buf.position();
        while (m + i < buf.limit()) {
            if (sought[i] == buf.get(m + i)) {
                i++;
                if (i == sought.length) return m;
            } else {
                m = m + i - t[i];
                if (i > 0)
                    i = t[i];
            }
        }
        return -1;
    }

    protected static int[] kmpTable(byte[] w) {
        int i = 2;
        int j = 0;

        int[] t = new int[w.length];

        t[0] = -1;
        t[1] = 0;

        while (i < w.length) {
            if (w[i - 1] == w[j]) {
                t[i] = j + 1;
                i += 1;
                j += 1;
            } else if (j > 0) {
                j = t[j];
            } else {
                t[i] = 0;
                i += 1;
            }

        }
        return t;
    }

    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = Math.min(Math.min(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }
}
