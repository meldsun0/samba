package samba.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import samba.domain.content.ContentBlockHeader;
import samba.schema.content.ssz.blockheader.BlockHeaderWithProofContainer;
import samba.schema.content.ssz.blockheader.SszBlockProofHistoricalHashesAccumulatorVector;
import samba.schema.content.ssz.blockheader.accumulator.HeaderRecordContainer;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalHashesAccumulatorContainer;

import java.io.InputStream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderFunctions;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoricalHashesAccumulatorTest {

  BlockHeader genesisBlockHeader;
  HeaderRecordContainer genesisHeaderRecordContainer;
  BlockHeader blockHeader1;
  HeaderRecordContainer blockHeaderRecordContainer1;
  BlockHeader blockHeader2;
  HeaderRecordContainer blockHeaderRecordContainer2;
  ContentBlockHeader contentBlockHeader;
  ContentBlockHeader contentBlockHeaderPartialEpoch;

  @BeforeEach
  public void setUp() {
    RLPInput inputGenesis =
        RLP.input(
            Bytes.fromHexString(
                "0xf90214a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0d7f8974fb5ac78d9ac099b9ad5018bedc2ce0a72dad1827a1709da30580f0544a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000850400000000808213888080a011bbe8db4e347b4e8c937c1c8370e4b5ed33adb3db69cbdb7a38e1e50b1b82faa00000000000000000000000000000000000000000000000000000000000000000880000000000000042"));
    genesisBlockHeader = BlockHeader.readFrom(inputGenesis, new MainnetBlockHeaderFunctions());
    genesisHeaderRecordContainer =
        new HeaderRecordContainer(
            genesisBlockHeader.getHash(),
            UInt256.valueOf(genesisBlockHeader.getDifficulty().getAsBigInteger()));
    RLPInput inputBlock1 =
        RLP.input(
            Bytes.fromHexString(
                "0xf90211a0d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479405a56e2d52c817161883f50c441c3228cfe54d9fa0d67e4d450343046425ae4271474353857ab860dbc0a1dde64b41b5cd3a532bf3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff80000001821388808455ba422499476574682f76312e302e302f6c696e75782f676f312e342e32a0969b900de27b6ac6a67742365dd65f55a0526c41fd18e1b16f1a1215c2e66f5988539bd4979fef1ec4"));
    blockHeader1 = BlockHeader.readFrom(inputBlock1, new MainnetBlockHeaderFunctions());
    blockHeaderRecordContainer1 =
        new HeaderRecordContainer(
            blockHeader1.getHash(),
            UInt256.valueOf(blockHeader1.getDifficulty().getAsBigInteger()));
    RLPInput inputBlock2 =
        RLP.input(
            Bytes.fromHexString(
                "0xf90218a088e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794dd2f1e6e498202e86d8f5442af596580a4f03c2ca04943d941637411107494da9ec8bc04359d731bfd08b72b4d0edcbd4cd2ecb341a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff00100002821388808455ba4241a0476574682f76312e302e302d30636463373634372f6c696e75782f676f312e34a02f0790c5aa31ab94195e1f6443d645af5b75c46c04fbf9911711198a0ce8fdda88b853fa261a86aa9e"));
    blockHeader2 = BlockHeader.readFrom(inputBlock2, new MainnetBlockHeaderFunctions());
    blockHeaderRecordContainer2 =
        new HeaderRecordContainer(
            blockHeader2.getHash(),
            UInt256.valueOf(blockHeader2.getDifficulty().getAsBigInteger()));

    this.contentBlockHeader =
        ContentBlockHeader.decode(
            Bytes.fromHexString(
                "0x080000002d020000f90222a02c58e3212c085178dbb1277e2f3c24b3f451267a75a234945c1581af639f4a7aa058a694212e0416353a4d3865ccf475496b55af3a3d3b002057000741af9731919400192fb10df37c9fb26829eb2cc623cd1bf599e8a067a9fb631f4579f9015ef3c6f1f3830dfa2dc08afe156f750e90022134b9ebf6a018a2978fc62cd1a23e90de920af68c0c3af3330327927cda4c005faccefb5ce7a0168a3827607627e781941dc777737fc4b6beb69a8b139240b881992b35b854eab9010000200000400000001000400080080000000000010004010001000008000000002000110000000000000090020001110402008000080208040010000000a8000000000000000000210822000900205020000000000160020020000400800040000000000042080000000400004008084020001000001004004000001000000000000001000000110000040000010200844040048101000008002000404810082002800000108020000200408008000100000000000000002020000b00010080600902000200000050000400000000000000400000002002101000000a00002000003420000800400000020100002000000000000000c00040000001000000100187327bd7ad3116ce83e147ed8401c9c36483140db184627d9afa9a457468657265756d50504c4e532f326d696e6572735f55534133a0f1a32e24eb62f01ec3f2b3b5893f7be9062fbf5482bc0d490a54352240350e26882087fbb243327696851aae1651b60cc53ffa2df1bae1550a0000000000000000000000000000000000000000000063d45d0a2242d35484f289108b3c80cccf943005db0db6c67ffea4c4a47fd529f64d74fa6068a3fd89a2c0d9938c3a751c4706d0b0e8f99dec6b517cf12809cb413795c8c678b3171303ddce2fa1a91af6a0961b9db72750d4d5ea7d5103d8d25f23f522d9af4c13fe8ac7a7d9d64bb08d980281eea5298b93cb1085fedc19d4c60afdd52d116cfad030cf4223e50afa8031154a2263c76eb08b96b5b8fdf5e5c30825d5c918eefb89daaf0e8573f20643614d9843a1817b6186074e4e53b22cf49046d977c901ec00aef1555fa89468adc2a51a081f186c995153d1cba0f2887d585212d68be4b958d309fbe611abe98a9bfc3f4b7a7b72bb881b888d89a04ecfe08b1c1a48554a48328646e4f864fe722f12d850f0be29e3829d1f94b34083032a9b6f43abd559785c996229f8e022d4cd6dcde4aafcce6445fe8743e1fcbe8672a99f9d9e3a5ca10c01f3751d69fbd22197f0680bc1529151130b22759bf185f4dbce357f46eb9cc8e21ea78f49b298eea2756d761fe23de8bea0d2e15aed136d689f6d252c54ebadc3e46b84a397b681edf7ec63522b9a298301084d019d0020000000000000000000000000000000000000000000000000000000000000"));

    this.contentBlockHeaderPartialEpoch =
        ContentBlockHeader.decode(
            Bytes.fromHexString(
                "0x0800000023020000f90218a02f1dc309c7cc0a5a2e3b3dd9315fea0ffbc53c56f9237f3ca11b20de0232f153a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794ea674fdde714fd979de3edf0f56aa9716b898ec8a0fee48a40a2765ab31fcd06ab6956341d13dc2c4b9762f2447aa425bb1c089b30a082864b3a65d1ac1917c426d48915dca0fc966fbf3f30fd051659f35dc3fd9be1a013c10513b52358022f800e2f9f1c50328798427b1b4a1ebbbd20b7417fb9719db90100ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff872741c5e4f6c39283ed14f08401c9c3808401c9a028846322c95c8f617369612d65617374322d31763932a02df332ffb74ecd15c9873d3f6153b878e1c514495dfb6e89ad88e574582b02a488232b0043952c93d98508fb17c6eeeb461cb6348eeed7700c00000000000000000000000000000000000000000000db21cba827f968eeadeee502025f01cbcfcf20e0fafee4ecb5a877bb7ae218f3f5a5fd42d16a20302798ef6ed309979b43003d2320d9f0e8ea9831a92759fb4bdb56114e00fdd4c1f85c892bf35ac9a89289aaecb1ebd0a96cde606a748b5d71c78009fdf07fc56a11f122370658a353aaa542ed63e44c4bc15ff4cd105ab33c50cb3e7542c17fa65faecc0fd911b9069cddb9e52114cfff2b06d3411267c78e774ec19c2ab8f61bac6da8835c6c00c74a7c07f3804b626d34563458952c589929776e586fafb3d3f001a11c9aaa3986752d147fa90faea0f463261f2ed9b8711529aacd1c137bffadc6bdf388b668118d469207288ca3681c895855c930f10b26846476fd5fc54a5d43385167c95144f2643f533cc85bb9d16b782f8d7db193506d86582d252405b840018792cad2bf1259f1ef5aa5f887e13cb2f0094f51e189cc2a1734369d3d379aa5cedb69c2709ec75233c63f3b84bf9aa3ae048bd8cd6cf04127db05441cd833107a52be852868890e4317e6a02ab47683aa75964220f1f11deb5763b3c38cb34c1344a6a02b6ccb1079b746545b7318057a8a5dbd2ff214000000000000000000000000000000000000000000000000000000000000"));
  }

  @Test
  public void testValidateValidBlockHeader() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("premergeacc.bin");

      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalHashesAccumulatorContainer accumulator =
          HistoricalHashesAccumulatorContainer.decodeBytes(accumulatorBytes);
      boolean validated = HistoricalHashesAccumulator.validate(contentBlockHeader, accumulator);
      assertTrue(validated);
    } catch (Exception e) {
      fail("Failed to read the premergeacc.bin file", e);
    }
  }

  @Test
  public void testValidateValidBlockHeaderWithPartialEpoch() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("premergeacc.bin");
      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalHashesAccumulatorContainer accumulator =
          HistoricalHashesAccumulatorContainer.decodeBytes(accumulatorBytes);
      boolean validated =
          HistoricalHashesAccumulator.validate(contentBlockHeaderPartialEpoch, accumulator);
      assertTrue(validated);
    } catch (Exception e) {
      fail("Failed to read the premergeacc.bin file", e);
    }
  }

  @Test
  public void testValidateInvalidBlockHeader() {
    ContentBlockHeader blockHeader =
        new ContentBlockHeader(
            new BlockHeaderWithProofContainer(
                genesisBlockHeader,
                new SszBlockProofHistoricalHashesAccumulatorVector(
                        contentBlockHeader.getBlockProofHistoricalHashesAccumulator())
                    .sszSerialize()));
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("premergeacc.bin");
      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      HistoricalHashesAccumulatorContainer accumulator =
          HistoricalHashesAccumulatorContainer.decodeBytes(accumulatorBytes);
      boolean validated = HistoricalHashesAccumulator.validate(blockHeader, accumulator);
      assertTrue(!validated);
    } catch (Exception e) {
      fail("Failed to read the premergeacc.bin file", e);
    }
  }
}
