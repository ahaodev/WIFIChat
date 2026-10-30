package com.ahao.wifichat.service.filter;


import com.ahao.wifichat.protocol.codec.Pack;

public interface PacketFilter {

    /**
     * Tests whether or not the specified packet should pass the filter.
     *
     * @param packet the packet to test.
     * @return true if and only if <tt>packet</tt> passes the filter.
     */
    public boolean accept(Pack packet);
}
