package de.bischinger.tinkerforge;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletTemperature;
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

	//Application bricklet class to uid map
	final BrickletUidMap brickletUidMap = new BrickletUidMap();

	//Lookup connected bricklets with an EnumerateListener
	IPConnection ipConnection = new IPConnection();
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

	//Example: 2 temperature and one ambientlight bricklets
	BrickletTemperature temperature1 = new BrickletTemperature(brickletUidMap.getBrickletUid(BrickletTemperature.class),
	                                                    ipConnection);
	BrickletTemperature temperature2 = new BrickletTemperature(brickletUidMap.getBrickletUid(BrickletTemperature.class),
	                                                    ipConnection);
	BrickletAmbientLight ambientLight = new BrickletAmbientLight(
			brickletUidMap.getBrickletUid(BrickletAmbientLight.class), ipConnection);
  }
}
