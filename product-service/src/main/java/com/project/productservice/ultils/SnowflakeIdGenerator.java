package com.project.productservice.ultils;

import lombok.Builder;


public class SnowflakeIdGenerator {
    private final long startEpoch;

    private final long workerId;
    private final long datacenterId;
    private final long sequenceMask;
    private final long workerIdShift;
    private final long datacenterIdShift;
    private final long timestampLeftShift;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    @Builder
    public SnowflakeIdGenerator(
            long workerIdBits,
            long datacenterIdBits,
            long sequenceBits,
            long workerId,
            long datacenterId,
            long startEpoch
    ) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.startEpoch = startEpoch;

        long maxDatacenterId = (1L << datacenterIdBits) - 1;
        long maxWorkerId = (1L << workerIdBits) - 1;
        this.sequenceMask = (1L << sequenceBits) - 1;

        this.workerIdShift = sequenceBits;
        this.datacenterIdShift = sequenceBits + workerIdBits;
        this.timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

        if (workerId > maxWorkerId || workerId < 0)
            throw new IllegalArgumentException("workerId out of range (0-" + maxWorkerId + ")");
        if (datacenterId > maxDatacenterId || datacenterId < 0)
            throw new IllegalArgumentException("datacenterId out of range (0-" + maxDatacenterId + ")");
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;

            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - startEpoch) << timestampLeftShift)
                | datacenterId << datacenterIdShift
                | workerId << workerIdShift
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}