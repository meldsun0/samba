package samba.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderFunctions;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import samba.schema.content.ssz.blockheader.accumulator.EpochRecordList;
import samba.schema.content.ssz.blockheader.accumulator.HeaderRecordContainer;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalHashesAccumulatorContainer;

public class HistoricalHashesAccumulatorTest {

    BlockHeader genesisBlockHeader;
    HeaderRecordContainer genesisHeaderRecordContainer;
    BlockHeader blockHeader1;
    HeaderRecordContainer blockHeaderRecordContainer1;
    BlockHeader blockHeader2;
    HeaderRecordContainer blockHeaderRecordContainer2;
    
    @BeforeEach
    public void setUp() {
        RLPInput inputGenesis = RLP.input(Bytes.fromHexString("0xf90214a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a0d7f8974fb5ac78d9ac099b9ad5018bedc2ce0a72dad1827a1709da30580f0544a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000850400000000808213888080a011bbe8db4e347b4e8c937c1c8370e4b5ed33adb3db69cbdb7a38e1e50b1b82faa00000000000000000000000000000000000000000000000000000000000000000880000000000000042"));
        genesisBlockHeader = BlockHeader.readFrom(inputGenesis, new MainnetBlockHeaderFunctions());
        genesisHeaderRecordContainer = new HeaderRecordContainer(genesisBlockHeader.getHash(), UInt256.valueOf(genesisBlockHeader.getDifficulty().getAsBigInteger()));
        RLPInput inputBlock1 = RLP.input(Bytes.fromHexString("0xf90211a0d4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d493479405a56e2d52c817161883f50c441c3228cfe54d9fa0d67e4d450343046425ae4271474353857ab860dbc0a1dde64b41b5cd3a532bf3a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff80000001821388808455ba422499476574682f76312e302e302f6c696e75782f676f312e342e32a0969b900de27b6ac6a67742365dd65f55a0526c41fd18e1b16f1a1215c2e66f5988539bd4979fef1ec4"));
        blockHeader1 = BlockHeader.readFrom(inputBlock1, new MainnetBlockHeaderFunctions());
        blockHeaderRecordContainer1 = new HeaderRecordContainer(blockHeader1.getHash(), UInt256.valueOf(blockHeader1.getDifficulty().getAsBigInteger()));
        RLPInput inputBlock2 = RLP.input(Bytes.fromHexString("0xf90218a088e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794dd2f1e6e498202e86d8f5442af596580a4f03c2ca04943d941637411107494da9ec8bc04359d731bfd08b72b4d0edcbd4cd2ecb341a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008503ff00100002821388808455ba4241a0476574682f76312e302e302d30636463373634372f6c696e75782f676f312e34a02f0790c5aa31ab94195e1f6443d645af5b75c46c04fbf9911711198a0ce8fdda88b853fa261a86aa9e"));
        blockHeader2 = BlockHeader.readFrom(inputBlock2, new MainnetBlockHeaderFunctions());
        blockHeaderRecordContainer2 = new HeaderRecordContainer(blockHeader2.getHash(), UInt256.valueOf(blockHeader2.getDifficulty().getAsBigInteger()));
    }

    @Test
    public void testUpdateAccumulator() {
        HistoricalHashesAccumulatorContainer accumulator = new HistoricalHashesAccumulatorContainer();
        accumulator = HistoricalHashesAccumulator.updateAccumulator(accumulator, genesisBlockHeader);
        Bytes genesisHash = Hash.sha256(accumulator.sszSerialize());
        System.out.println("Genesis Hash: " + genesisHash.toHexString());
        System.out.println("After updating with Genesis Block Header: " + accumulator.hashTreeRoot().toHexString());
        HistoricalHashesAccumulatorContainer genesisAccumulator = new HistoricalHashesAccumulatorContainer(List.of(), new EpochRecordList(List.of(genesisHeaderRecordContainer)));
        System.out.println("Genesis Accumulator: " + genesisAccumulator.hashTreeRoot().toHexString());

        System.out.println("Genesis Block Header: " + genesisBlockHeader.getHash().toString());
        System.out.println("Genesis Block Header: " + genesisBlockHeader.getDifficulty().toString());
        /* 
        
        System.out.println("Genesis Header Record Container: " + genesisHeaderRecordContainer.toString());
        accumulator = HistoricalHashesAccumulator.updateAccumulator(accumulator, genesisBlockHeader);
        System.out.println("After updating with Genesis Block Header: " + accumulator.toString());
        System.out.println(accumulator.hashTreeRoot().toHexString());
        accumulator = HistoricalHashesAccumulator.updateAccumulator(accumulator, blockHeader1);
        System.out.println("After updating with Block Header 1: " + accumulator.toString());
        System.out.println(accumulator.hashTreeRoot().toHexString());
        */
        //assertEquals(Bytes32.fromHexString("0xb629833240bb2f5eabfb5245be63d730ca4ed30d6a418340ca476e7c1f1d98c0"), accumulator.hashTreeRoot());
    }
}
