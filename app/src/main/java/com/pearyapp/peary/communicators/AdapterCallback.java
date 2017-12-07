package com.pearyapp.peary.communicators;

import java.util.List;

/**
 * Created by Alexa on 28.04.2016.
 */
public interface AdapterCallback {
    void itemClicked(int count, List<Integer> selectedItemId);
}
