package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5UpdateNodeInfo;
import samba.api.jsonrpc.results.NodeInfo;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.services.discovery.Discv5Client;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5UpdateNodeInfoTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_UPDATE_NODE_INFO = "discv5_updateNodeInfo";

  private final String ENR =
      "enr:-LS4QOhHz1hd6Sg6dAtYL1XDsMN-8Quk0dmH_RhY50nVAGApdpEcK15YxNZhhDFIqNAACi8E3H1GIbtKgQsaM2TDVkyEZ1t3LGOqdCBjMjhjMzFmODViNTU4NDM0MWE0ZDQ0NTliODg2M2VjYThkOTRhY2Q2gmlkgnY0gmlwhKwRAAWJc2VjcDI1NmsxoQLv0EURHW2Rbcuk5hmsN7ZjorMOktgSBDB6n_kYOo-wc4N1ZHCCIzE";
  private Discv5UpdateNodeInfo method;
  private Discv5Client discv5Client;
  private final NodeRecord homeNodeRecord = NodeRecordFactory.DEFAULT.fromEnr(ENR);

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5UpdateNodeInfo(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_UPDATE_NODE_INFO);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_UPDATE_NODE_INFO, new Object[] {"127.0.0.1:9000", true}));

    when(discv5Client.updateEnrSocket(any(), anyBoolean())).thenReturn(true);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

    JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), buildNodeInfo(homeNodeRecord));
    JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    when(discv5Client.updateEnrSocket(any(), anyBoolean())).thenReturn(false);
    expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void testResponseWithNullNodeRecord() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_UPDATE_NODE_INFO, new Object[] {}));

    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);

    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  private NodeInfo buildNodeInfo(NodeRecord nodeRecord) {
    return new NodeInfo(nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
  }
}
