package samba.domain.node;

import org.apache.tuweni.bytes.Bytes;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

public class NodeDistanceCalculator {


    /**
     * Calculates the XOR distance between two values.
     *
     * @param origin the first value
     * @param destination the second value
     * @return the distance
     */

    public static int calculateDistance(final Bytes origin, Bytes destination) {
        if(origin.size() != destination.size()) throw  new AssertionError();
        final byte[] v1b = origin.toArray();
        final byte[] v2b = destination.toArray();
        if (Arrays.equals(v1b, v2b)) {
            return 0;
        }
        int distance = v1b.length * 8;
        for (int i = 0; i < v1b.length; i++) {
            final byte xor = (byte) (0xff & (v1b[i] ^ v2b[i]));
            if (xor == 0) {
                distance -= 8;
            } else {
                int p = 7;
                while (((xor >> p--) & 0x01) == 0) {
                    distance--;
                }
                break;
            }
        }
        return distance;
    }
    }
}


