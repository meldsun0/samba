package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5NodeInfo;
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

public class Discv5NodeInfoTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_NODE_INFO = "discv5_nodeInfo";
  private final String ENR =
      "enr:-LS4QOhHz1hd6Sg6dAtYL1XDsMN-8Quk0dmH_RhY50nVAGApdpEcK15YxNZhhDFIqNAACi8E3H1GIbtKgQsaM2TDVkyEZ1t3LGOqdCBjMjhjMzFmODViNTU4NDM0MWE0ZDQ0NTliODg2M2VjYThkOTRhY2Q2gmlkgnY0gmlwhKwRAAWJc2VjcDI1NmsxoQLv0EURHW2Rbcuk5hmsN7ZjorMOktgSBDB6n_kYOo-wc4N1ZHCCIzE";
  private Discv5NodeInfo method;
  private Discv5Client discv5Client;
  private final NodeRecordFactory nodeRecordFactory = NodeRecordFactory.DEFAULT;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5NodeInfo(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_NODE_INFO);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_NODE_INFO, new Object[] {}));
    final NodeRecord homeNodeRecord = nodeRecordFactory.fromEnr(ENR);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), buildNodeInfo(homeNodeRecord));
    final JsonRpcResponse actual = method.response(request);

    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultDespiteParameters() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_NODE_INFO, new Object[] {"0x68656c6c6f20776f726c6"}));
    final NodeRecord homeNodeRecord = nodeRecordFactory.fromEnr(ENR);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), buildNodeInfo(homeNodeRecord));
    final JsonRpcResponse actual = method.response(request);

    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void testResponseWithNullNodeRecord() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_NODE_INFO, new Object[] {}));
    when(discv5Client.getHomeNodeRecord()).thenReturn(null);

    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INTERNAL_ERROR);
    final JsonRpcResponse actual = method.response(request);

    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  private NodeInfo buildNodeInfo(NodeRecord nodeRecord) {
    return new NodeInfo(nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
  }
}
