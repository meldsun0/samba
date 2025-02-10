package samba.services.utp;

//    // IP address + port + Discovery v5 NodeId + connection_id
public record UTPClientId(int connectionIdReceiving, int connectionIdSending, String enr) {}
