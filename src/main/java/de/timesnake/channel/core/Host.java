/*
 * channel-api.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

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
