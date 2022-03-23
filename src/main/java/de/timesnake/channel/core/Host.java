package de.timesnake.channel.core;

import java.util.Objects;

public class Host {

    private final String hostname;
    private final int port;

    public Host(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static Host parseHost(String identifier) {
        String[] s = identifier.split("\\:");
        return new Host(s[0], Integer.parseInt(s[1]));
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return this.hostname + ":" + this.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hostname, this.port);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Host)) return false;
        return this.hostname.equals(((Host) o).getHostname()) && this.port == ((Host) o).getPort();
    }
}
