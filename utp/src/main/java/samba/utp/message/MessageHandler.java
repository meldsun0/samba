package samba.utp.message;

public interface MessageHandler<Message> {

  void handle(Message message);
}
