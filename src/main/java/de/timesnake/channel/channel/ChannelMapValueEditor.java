package de.timesnake.channel.channel;

@FunctionalInterface
public interface ChannelMapValueEditor<Entry> {

    void edit(Entry entry);
}
