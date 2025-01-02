package samba.domain.content;

public enum ContentProofType {
    NONE(0),
    BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR(1),
    BLOCK_PROOF_HISTORICAL_ROOTS(2),
    BLOCK_PROOF_HISTORICAL_SUMMARIES(3);

    private final int value;

    ContentProofType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ContentProofType fromValue(int value) {
        for (ContentProofType proofType : ContentProofType.values()) {
            if (proofType.getValue() == value) {
                return proofType;
            }
        }
        throw new IllegalArgumentException("Unknown proof type: " + value);
    }
}