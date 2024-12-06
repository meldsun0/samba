package samba.rlp;

import java.util.Base64;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.rlp.RLP;

public class RLPDecoder {

  // enr is in base64
  public static String decodeRlpEnr(String enr) {
    byte[] decodedBytes = Base64.getUrlDecoder().decode(enr);
    Bytes decodedENR = RLP.decodeValue(Bytes.wrap(decodedBytes));
    return Base64.getUrlEncoder().encodeToString(decodedENR.toArray());
  }
}
