package samba.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import samba.domain.content.ContentBlockHeader;
import samba.schema.content.ssz.blockheader.BlockHeaderWithProofContainer;
import samba.schema.content.ssz.blockheader.BlockProofHistoricalRootsContainer;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalRootsAccumulatorList;

import java.io.InputStream;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class HistoricalRootsAccumulatorTest {

  ContentBlockHeader blockHeaderMidBellatrix;
  ContentBlockHeader blockHeaderEndBellatrix;

  @BeforeEach
  public void setUp() {
    this.blockHeaderMidBellatrix =
        ContentBlockHeader.decode(
            Bytes.fromHexString(
                "0x080000002c020000f90221a04fe8e9f0732bab1fd6fb908191d9e473e881ce961b5d94f5f46c6fe4a0f0845da01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794b64a30399f7f6b0c154c2e7af0a3ec7b0a5b131aa037d2317cf37060db51553e4ab11a55ba2ac05da66271925a4ffac03953f54ceea0f3f2a07d2c6725c4bb6efa1402071cc5d1d20e1074f7c670ae1a1274b4746a07a02807364b6cc74999ee0dc10800133cb610f54da5fa9948ef559817b88b01e017b90100bdbe3fc3d5f28deefeebf75bef8876739361bf41fff717892eb12cffb47313ceb8fedff3d43b9c7cfc9b5bf98de1adf973a9930dff037a31f64e8ef4a77466d7fe57c0bebf50cc6dfb1b627f7cff7cf3d0fe9c3bddeedf4f3a6f9f66e97539b6abcb1be16e238a8babe45c6ea605fa79a76bcffcfd1d4fd47e6ddedfcd2fbd766ebeffae3bfb9e5daffd5afa6773a6f66dd2dff10fdd9acfafbf11f67ad7797fdfcabf79db19f37fffffe7c30e0e88bbf7c233e9d7bb03dbcbff167bdd2db2bf65f2bb4a7bc1ff7fef7f5fe4cb9feedfbee5697dec5fbe5424cdabee9afa760fe77dbe5f9b54e71f9e3efdaceab1faf4b0bd8d397cdac96f66dc8dffcf1f7d778083ed5c918401c9c380840197ace184632630579f496c6c756d696e61746520446d6f63726174697a6520447374726962757465a02b6b814f80462191e3bb82741c920d412268a74fd8ba21a4128026c64620b7c78800000000000000008502465724f3314e9ca9396abb0898ce009c85990359a45c18a97f254f99c79b11e66ed1699be5f76565433dfdb5f2ef643ccf6b93efc00dfa6cc57610022534f4ae3b0f49fb7f1917c2db12556ab98cd41d5a7c5ed8aa64be1cb691fc7eca503006ca89c464de74802f27625c42a249879f9bfbdf8c189b56ac1a273d334d9e7979a7a186536d950fb57fde8a2e302a5709128608c071ddce2cd1b3d72b38f510d4ec438f3dd6b9fd163d53ab6518f2e46e6e5a2d79f1b2f5311f00a0639208dc496fc09762a2592811e61072d442c6874c0c9eb4796f18b9b6590e7da10ce5f580f8e05c0f7ef76e3386e5dd2c597f8777c407496e3eb9fa461fb5436aefef5c8a17e940a0d17dadb989c4d5eeb0f8144006355dee7e517589f3201794861f38f2a5a2cb3ad693dc8c431a95dac6f4b1ef27f2c9de0aa3543315665cca21a4dfa13136c071a5c35f8522ef3e816506ac326be3c1828ecdcbb93ef8d2cb668091f8dac5c49b28abd907b5e7cdc6e1d760b8fbe63d7e68550335f84e8119e7435f2e5948fe6e1bec32ad4488d3a4d17dacb3a5e2c670db161f1abcf61777aeb18ed5952fe597ff2fa8b781b49404b97cc07985c467c1208d96094e2494cbd787be6029590c3ae3040c0b5bbcb4f53d4a9d2cb2eeb1ba72eeaf54005e15e387af5829bf786032494e7fa99777791f708752d986c8f819afab429d533d0990862b039d74c16334f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b8c9e2376454618be2a49208834acbef8efad91fd63e120709836239cf3f3965eeada8ab88e38c0314453ba83a19d16cf9d7ac1c68995fd8b9b54a58f2384a9566e9a918b7435624d2dcf95503727499a3d17b5ecc522cf3ab017403b75461937f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4bdb56114e00fdd4c1f85c892bf35ac9a89289aaecb1ebd0a96cde606a748b5d71e40d513acb66b3698df13a593c6d317a9ff0ce1607d6d0c92e4a00ac71f3a5e50000000000000000000000000000000000000000000000000000000000000000f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4ba4427b1b8a34052b81657fb0c2e2602c34102da7434dd67728aabc9bdd142d160000480000000000"));
    this.blockHeaderEndBellatrix =
        ContentBlockHeader.decode(
            Bytes.fromHexString(
                "0x080000001e020000f90213a08514dc16265e910acc5d6d776f55c9cfbcec1320c816546415dc35b021801f63a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347941f9090aae28b8a3dceadf281b0f12828e676c326a08b8da1a447bed1a4019708ea1e4114383d42f40e1f6e85c21889b6fbcffdd295a0749c345c3827e94c7fcd112a367a723f872b01b66daf39e7acfbcaca0a69f0eca0ac8092ad7a39a1b0742cc306986283e68ee91c45ee470094e8bc2bd3d222494fb9010000a020c54d40c941102b81a7be3423b30060490380108f903083522294d883205996911ae1c0802852900b60d60e81a086cd11508843f80552f4538481767145080412004b04992a691b520802c488f8a34002b0354e180348085c4480604da0926020704a0342138ec0722d40f019e1a41c2c555813150042c1c2d0803c1902180083c00701045004408a510b0286720c60cc815169100884132651e21a1c288a08015880026400699041d0551615e48495e86460a38d0a052a1c843108765144f2c026c400da196d0d0d245603f0624008528301ca0a1ce123002b5020e034485ca3090a0640804c5495dc639528208f636032593a08cc724898138cb120b280840103ee758401c9c3808380f0ca846437304b917273796e632d6275696c6465722e78797aa0c5ceca9075886de0874fadb93a6d790cd4aff9f4becb2c69d354128da0d5efb288000000000000000085046d9a8492fb5385379666b7b1a0629fe63f86ba8b8ed83964a7d710677abb05584ce9056859618d45cc94b07100bfb18fffa7a8fc0821908d5a80b544c32ae10816dbc7a917bb66b93877825c61f5f8a342922a34eebe630f8c0871330c73b17e89a4ef93ef142b3b2ed22e156ffedb34bb2fc7f91f9174890278cbdd7207db65f6351f899e6d22edccc0262b69144bc395e6ae36b74ec27104c702db2d28a12a1c91d0b3b0d9c6798bd192c6dfc68f4d4986fd658ddf9110482150c1dddb1b20c00886bf452865d33b72f38caba8e79a554ffb9af8d33959151940095e0a50ebf585cf91f4c2a01ae75f6185f3f43e637f89ff5fb0fba8f8efa923b5b7b5392b0d43b9aaaa10aa33f39781dd846b90ee11b83820f0f4f17f8f388cd50d4ebdbf7c96877cbe30fe77a7b02ebdd71fab34f8c4c381c6df45fa78356bb9db8f830b3b70f4f35cfa39659f99b790f7c626795b50772b9404841158c156331aebc1b78e0129f8d173c90f92d25be91740e974e3b7585e363b7849d611100c6c304fe498e85ed029379612316a4dfd54b37a32c705951f45f344166e86cb5f8970242fbf5bf1c4ad45416a6d9cc416ca279836c8f6c86da69fe0556c58dc99bc97a761dd04494bd82611c764830c4865cdc90126d852d3a131a877fcb2811da5c70a78943e579dcca11f20bb361f89edab7314c24aba7b766b8fdbc7baf9404f08ff52d5e28437f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b9c16d5dc4ac3ddaf52af02e9bfb5582b98d3e6d7f69972a5478c3805649b8a6c8e043b4002895805adda2adb4be5668a347087567a3ebbaa8c732c5f318fe302eeacaba5cd991c70c813e866d269743ba6952dbb4e714f135dbf541fb3a85a9ef5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4bdb56114e00fdd4c1f85c892bf35ac9a89289aaecb1ebd0a96cde606a748b5d713d0ec8ce11308d6ac9cda6a6fcc5f351fc56b95650f76b858fb37a32a38fab070000000000000000000000000000000000000000000000000000000000000000f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4b8a41834d279b06fb294c30d4c6308e68c8e43da7540efa8a6dcbb798e1b137ceffbf5e0000000000"));
  }

  @Test
  public void testValidateValidBlockHeader() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("historicalroots.bin");

      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
          HistoricalRootsAccumulatorList.decodeBytes(accumulatorBytes);
      boolean validated =
          HistoricalRootsAccumulator.validate(
              blockHeaderMidBellatrix, historicalRootsAccumulatorList);
      assertTrue(validated);
    } catch (Exception e) {
      fail("Failed to read historical roots accumulator file: " + e.getMessage());
    }
  }

  @Test
  public void testValidateValidBlockHeaderEnd() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("historicalroots.bin");

      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
          HistoricalRootsAccumulatorList.decodeBytes(accumulatorBytes);
      boolean validated =
          HistoricalRootsAccumulator.validate(
              blockHeaderEndBellatrix, historicalRootsAccumulatorList);
      assertTrue(validated);
    } catch (Exception e) {
      fail("Failed to read historical roots accumulator file: " + e.getMessage());
    }
  }

  @Test
  public void testValidateInvalidBlockHeader() {
    ContentBlockHeader invalidBlockHeader =
        new ContentBlockHeader(
            new BlockHeaderWithProofContainer(
                blockHeaderMidBellatrix.getBlockHeader(),
                new BlockProofHistoricalRootsContainer(
                        blockHeaderEndBellatrix.getBeaconBlockProofHistoricalRoots(),
                        blockHeaderEndBellatrix.getBeaconBlockRoot(),
                        blockHeaderEndBellatrix.getExecutionBlockProof(),
                        UInt64.valueOf(blockHeaderEndBellatrix.getSlot()))
                    .sszSerialize()));
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("historicalroots.bin");

      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalRootsAccumulatorList historicalRootsAccumulatorList =
          HistoricalRootsAccumulatorList.decodeBytes(accumulatorBytes);
      boolean validated =
          HistoricalRootsAccumulator.validate(invalidBlockHeader, historicalRootsAccumulatorList);
      assertTrue(!validated);
    } catch (Exception e) {
      fail("Failed to read historical roots accumulator file: " + e.getMessage());
    }
  }
}
