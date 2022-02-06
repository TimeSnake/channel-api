package de.timesnake.channel.api.message;

public interface ChannelSupportMessage extends ChannelMessage {

    static ChannelSupportMessage getTicketLockMessage(Integer port, Integer ticketId) {
        return new de.timesnake.channel.message.ChannelSupportMessage(port, MessageType.TICKET_LOCK, String.valueOf(ticketId));
    }

    static ChannelSupportMessage getTicketRejctMessage(Integer port, Integer ticketId) {
        return new de.timesnake.channel.message.ChannelSupportMessage(port, MessageType.REJECT, String.valueOf(ticketId));
    }

    static ChannelSupportMessage getTicketAcceptMessage(Integer port, Integer ticketId) {
        return new de.timesnake.channel.message.ChannelSupportMessage(port, MessageType.ACCEPT, String.valueOf(ticketId));
    }

    static ChannelSupportMessage getTicketSubmitMessage(Integer port, Integer ticketId) {
        return new de.timesnake.channel.message.ChannelSupportMessage(port, MessageType.SUBMIT, String.valueOf(ticketId));
    }

    static ChannelSupportMessage getTicketCreationMessage(Integer port, Integer ticketId) {
        return new de.timesnake.channel.message.ChannelSupportMessage(port, MessageType.CREATION, String.valueOf(ticketId));
    }

    Integer getPort();

    String getValue();

    enum MessageType implements de.timesnake.channel.MessageType {
        TICKET_LOCK, SUBMIT, REJECT, ACCEPT, CREATION
    }
}
