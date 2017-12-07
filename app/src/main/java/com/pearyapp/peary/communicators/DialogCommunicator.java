package com.pearyapp.peary.communicators;

/**
 * Created by Alexa on 17.03.2016.
 */
public interface DialogCommunicator {
    void valueFromDialog(String data, int number, boolean statement, String key);
}
