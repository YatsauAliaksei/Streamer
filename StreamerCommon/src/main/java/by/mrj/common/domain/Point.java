package by.mrj.common.domain;

import lombok.Value;

/**
 * Represents point in time for Topic.
 * In simplest case sequence number
 */
@Value
public class Point {

    /**
     * Topic name
     */
    String topic;

    /**
     * Read from. Starting point
     */
    long seqNumber;

    /**
     * Max size of Data to be returned
     * 0 - means, size is up to Server
     */
    int maxDataSize;
}
