package de.bischinger.tinkerforge;

import com.tinkerforge.Device;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alexander Bischof on 28.01.15.
 *
 */
public class BrickletUidMap extends HashMap<Class<? extends Device>, ArrayDeque<String>> {

  public String getBrickletUid(Class<? extends Device> aClass) {
	return get(aClass).pop();
  }

  public ArrayDeque<String> put(Class<? extends Device> key, String uid) {
	ArrayDeque<String> uids = super.get(key);

    //lazy initialize
    if (uids == null) {
	  uids = new ArrayDeque<>();
	  super.put(key, uids);
	}

	uids.add(uid);
	return uids;
  }
}
