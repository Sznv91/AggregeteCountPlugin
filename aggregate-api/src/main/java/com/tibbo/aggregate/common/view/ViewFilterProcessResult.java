package com.tibbo.aggregate.common.view;

public enum ViewFilterProcessResult {
    /**
     * No actions required. Filters are valid.
     */
    OK,
    /**
     * Invalid filters provided.
     */
    ERROR,
    /**
     * Filters require full table processing.
     */
    HAS_TO_PROCESS_FULLY
}
