package com.example.Drive_system.connect;

public interface ProtocolHandler<T> {
    byte[] encodePackage(T data);

    T decodePackage(byte[] netData);

}
