package de.bischinger.tinkerforge;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alexander Bischof on 27.01.15.
 */
public class BrickletReader {
  public static void main(String[] args) throws Exception {

    //Find all Subclasses of Device
    Reflections reflections = new Reflections("com.tinkerforge");
    Set<Class<? extends Device>> subTypesOf = reflections.getSubTypesOf(Device.class);

	//Read all DeviceIdentifier into Map<Identifier, Class>
	final Map<Integer, Class<? extends Device>> allDeviceIdentifierMapping = new HashMap<>();
	for (Class<? extends Device> deviceClass : subTypesOf) {
	  try {
		Field deviceIdentifier = deviceClass.getDeclaredField("DEVICE_IDENTIFIER");
		allDeviceIdentifierMapping.put(deviceIdentifier.getInt(null), deviceClass);
	  } catch (IllegalAccessException | NoSuchFieldException e) {}  //Ignore
	}

	//Lookup connected bricklets with an EnumerateListener
	IPConnection ipConnection = new IPConnection();
	ipConnection.connect("localhost", 4223);
	ipConnection.addEnumerateListener(
			(uid, connectedUid, position, hardwareVersion, firmwareVersion, deviceIdentifier, enumerationType) -> {
			  if (enumerationType == IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
				return;
			  }

			  Class<? extends Device> aClass = allDeviceIdentifierMapping
					  .get(deviceIdentifier);
			  System.out.println(aClass.getSimpleName() + " UID: " + uid);
			}
	);

	ipConnection.enumerate();
  }
}
