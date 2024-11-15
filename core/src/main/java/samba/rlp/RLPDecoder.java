package samba.rlp;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.rlp.RLP;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class RLPDecoder {

    //enr is in base64
    public static String decodeRlpEnr(String enr) {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(enr);
            Bytes decodedENR = RLP.decodeValue(Bytes.wrap(decodedBytes));
            return  Base64.getUrlEncoder().encodeToString(decodedENR.toArray());
    }
}

