package samba.besu.plugin;

import api.HistoryService;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;

public class SambaHistoryAPI implements HistoryService {


    @Override
    public BlockHeader getBlockHeaderByBlockHash(Hash blockHash) {
        return null;
    }

    @Override
    public BlockBody getBlockBodyByBlockHash(Hash blockHash) {
        return null;
    }

    @Override
    public TransactionReceipt getReceiptByBlockHash(Hash blockHash) {
        return null;
    }

    @Override
    public BlockHeader getBlockHeaderByBlockNumber(long blockNumber) {
        return null;
    }
}
