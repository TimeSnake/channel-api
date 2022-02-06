package de.timesnake.channel.channel;

@FunctionalInterface
public interface ChannelMapEntryEditor<Key, Value> {

    void edit(Key key, Value value);
}
