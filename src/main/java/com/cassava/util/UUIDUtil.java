package com.cassava.util;

import java.util.Date;
import java.util.UUID;

/**
 * Created by yan.dai on 12/11/2015.
 */
public class UUIDUtil {

    public static synchronized UUID generateUUID() {
        return UUID.randomUUID();
    }

    /**
     * Gets a new time uuid.
     * @return the time uuid
     */
    public static synchronized UUID generateUUIDByTime() {
        return java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }


    /**
     * Returns an instance of uuid.
     *
     * @param uuid the uuid
     * @return the java.util. uuid
     */
    public static UUID toUUID( byte[] uuid )  {
        long msb = 0;
        long lsb = 0;
        assert uuid.length == 16;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (uuid[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (uuid[i] & 0xff);
        long mostSigBits = msb;
        long leastSigBits = lsb;

        com.eaio.uuid.UUID u = new com.eaio.uuid.UUID(msb,lsb);
        return UUID.fromString(u.toString());
    }


    /**
     * As byte array.
     *
     * @param uuid the uuid
     *
     * @return the byte[]
     */
    public static byte[] asByteArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;
    }

    /** create a TimeUUID based on a specific date, here is some code that will work
     *
     * @param d
     * @return the uuid
     */
    public static UUID uuidForDate(Date d) {
        final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
        /*
            Magic number obtained from #cassandra's thobbs, who
            claims to have stolen it from a Python library.
        */
        long origTime = d.getTime();
        long time = origTime * 10000 + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
        long timeLow = time &       0xffffffffL;
        long timeMid = time &   0xffff00000000L;
        long timeHi = time & 0xfff000000000000L;
        long upperLong = (timeLow << 32) | (timeMid >> 16) | (1 << 12) | (timeHi >> 48) ;
        return new UUID(upperLong, 0xC000000000000000L);
    }

}
