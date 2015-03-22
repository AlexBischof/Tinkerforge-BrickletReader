package de.bischinger.tinkerforge;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Alexander Bischof on 27.01.15.
 */
public class BrickletReader {
  private final BrickletUidMap brickletUidMap;

  public BrickletReader() {
	this.brickletUidMap = new BrickletUidMap();
  }

  public void init(IPConnection ipConnection) throws Exception {

	Objects.requireNonNull(ipConnection);

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
	ipConnection.connect("localhost", 4223);
	ipConnection.addEnumerateListener(
			(uid, connectedUid, position, hardwareVersion, firmwareVersion, deviceIdentifier, enumerationType) -> {
			  if (enumerationType == IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
				return;
			  }

			  //Add class uid combination to map
			  Class<? extends Device> aClass = allDeviceIdentifierMapping
					  .get(deviceIdentifier);
			  brickletUidMap.put(aClass, uid);
			}
	);

	ipConnection.enumerate();

	//Wait to get result
	Thread.sleep(1000l);
  }

  public String getBrickletUid(Class<? extends Device> aClass) {
	return brickletUidMap.getBrickletUid(aClass);
  }
}
