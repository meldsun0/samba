package samba.schema.content.ssz.blockheader.accumulator;

import samba.validation.HistoricalHashesAccumulator;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;

public class EpochRecordList {

    private static final SszListSchema<HeaderRecordContainer, SszList<HeaderRecordContainer>> schema =
            (SszListSchema<HeaderRecordContainer, SszList<HeaderRecordContainer>>) SszListSchema.create(HeaderRecordContainer.HeaderRecordContainerSchema.INSTANCE, HistoricalHashesAccumulator.EPOCH_SIZE);
    
}
