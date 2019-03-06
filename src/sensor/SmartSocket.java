package sensor;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class SmartSocket {

	
	static String rxdata = new String();

	public static void main(String args[]) throws Exception {
	
		System.out.println(" Serial: 38400, N, 8, 1.");
		// INIT SERIAL
		final Serial serial = SerialFactory.createInstance();

		// serial ������ �߻��� �̺�Ʈ ����
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {

					//System.out.printf("event.getAsciiString() : %s ", event.getAsciiString());
					rxdata = event.getAsciiString(); // "SS_NO: 1003, 297129736, 283, 01, \n\r"
					//System.out.println("rxdata>>>>>>>>>>>>" + rxdata);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// INIT
		try {
			// ��������� �ø��� ��Ʈ, �ӵ�����
			serial.open("/dev/ttyUSB0", 38400);
			
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
		ScheduledJob job = new ScheduledJob();
		Timer jobScheduler = new Timer();
		jobScheduler.scheduleAtFixedRate(job, 0, 1000); // delay-period �����ð� �������� �۾� ����
		while (true) {
			;
			Thread.sleep(1000);
		}
	}// main
}

//�۾� �����ٸ��� ���� ���
class ScheduledJob extends TimerTask {
	int btnCount = 0;

	ScheduledJob() {
		;
	}

	//jobScheduler.scheduleAtFixedRate(job, 0, 5000); // delay-period �����ð� �������� �� �κ��� ����
	public void run() {
		String[] st = null;
		String[] values = null;
		String ae = null;
		String container = "power_consumption"; // �׻� ���� �̸�
		String ip = null;
		boolean dataReady = false;

		SmartSocket.rxdata = SmartSocket.rxdata.replaceAll("\\s+", ""); // ����, Ư������ ����
		st = SmartSocket.rxdata.split("SS_NO:"); // st[0]="" st[1]=" 1003, 297129736, 283, 01, \n\r"
		SmartSocket.rxdata = "";
		
		System.out.println("111");
		for (int i = 0; i < st.length; i++) {
			/*if(st[i].equals("")){ // st[0]�϶� ""�̿��� ���ʿ��� ó�� ����
				System.out.println("st[" + i + "]>>>>>>>>>>>>>>>>>>>continue�Ѿ");
				continue;
			}*/
			if (st[i].split(",").length == 4) { // 1004, 1055, 39, 01
				values = st[i].split(",");
				if (Integer.parseInt(values[0]) >= 1000) { // id�� 1000���� ŭ
					ae = "smartsocket_";
					ae += String.valueOf(Integer.parseInt(values[0])); // AE �̸�
					ip = String.valueOf(Integer.parseInt(values[2])); // ��������
					try {
						System.out.println("ae : " + ae);
						System.out.println("container : " + container);
						System.out.println("ip : " + ip);
						dataReady = true;
					} catch (Exception e) {
						System.out.println(">>> Exception(make sensordata)" + e.toString());
						e.printStackTrace();
					}
				}
			}
		}

		if (dataReady == true) {
			dataReady = false;
			try {
				SendIp sendIp = new SendIp();
				sendIp.SendToBada(ae, container, ip);				
			} catch (Exception e) {
				System.out.println(">>> Exception, upload sensor" + e.toString());
			}
		}
	}
}
// ���� �ڵ�
/*public class SmartSocket {
	String deviceUID = new String();
	String gatewayIP = new String();
	String deviceID = new String();
	// static CoTSensorAPI sapi = null;

	static String rxdata = new String();

	static GpioPinDigitalOutput gLed1;
	static GpioPinDigitalOutput rLed1;
	static GpioPinDigitalOutput gLed2;
	static GpioPinDigitalOutput rLed2;
	static GpioPinDigitalInput pirPin;

	public static void main(String args[]) throws Exception {
		// sapi = new CoTSensorAPI();
		// sapi.registerSensorControl();

		GpioController gpio = GpioFactory.getInstance();
		gLed1 = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_25, PinMode.DIGITAL_OUTPUT); // O
																								// gnd
																								// O
																								// gpio25
		rLed1 = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_29, PinMode.DIGITAL_OUTPUT); // O
																								// gpio29
																								// O
																								// gpio28
		gLed2 = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_24, PinMode.DIGITAL_OUTPUT); // O
																								// gpio29
																								// O
																								// gpio28
		rLed2 = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_28, PinMode.DIGITAL_OUTPUT); // O
																								// gpio29
																								// O
																								// gpio28
		pirPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_15, PinPullResistance.PULL_DOWN);
		gLed1.low();
		rLed1.low();
		rLed2.high();

		System.out.println(" Serial: 38400, N, 8, 1.");
		// INIT SERIAL
		final Serial serial = SerialFactory.createInstance();

		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {

					System.out.printf("dataReceived : %s ", event.getAsciiString());
					rxdata += event.getAsciiString(); // "SS_NO: 1001, 233783,
														// 192, 01, \n\r"
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// INIT
		try {
			serial.open("/dev/ttyUSB0", 38400);
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
		ScheduledJob job = new ScheduledJob();
		Timer jobScheduler = new Timer();
		jobScheduler.scheduleAtFixedRate(job, 0, 5000); // delay-period
		while (true) {
			;
			Thread.sleep(1000);
		}
	}// main
}

class ScheduledJob extends TimerTask {
	int btnCount = 0;

	ScheduledJob() {
		;
	}

	public void run() {
		String[] st = null;
		String[] values = null;
		String ssid = null;
		String ip = null;
		boolean dataReady = false;

		SmartSocket.rxdata = SmartSocket.rxdata.replaceAll("\\s+", ""); // ����,
																		// Ư������
																		// ����
		st = SmartSocket.rxdata.split("SS_NO:");
		SmartSocket.rxdata = "";

		long timestamp = System.currentTimeMillis();

		for (int i = 0; i < st.length; i++) {
			if (st[i].split(",").length == 4) { // 1004, 1055, 39, 01
				values = st[i].split(",");
				if (Integer.parseInt(values[0]) >= 1000) { // id�� 1000���� ŭ
					ip = String.valueOf(Integer.parseInt(values[2]) / 100); // ��������
					try {
						System.out.println("ssid : " + (Integer.parseInt(values[0]) - 1000 + 1));
						System.out.println("sensordata : " + ip + " <> " + String.valueOf(timestamp));
						// ssid =
						// SmartSocket.sapi.getSensorID(Integer.parseInt(values[0])
						// - 1000 + 1);
						// SmartSocket.sapi.appendSensorData(ssid, "wr123", ip,
						// String.valueOf(timestamp) );
						dataReady = true;
					} catch (Exception e) {
						System.out.println(">>> Exception(make sensordata)" + e.toString());
						e.printStackTrace();
					}
				}
			}
		}

		if (dataReady == true) {
			dataReady = false;
			try {
				// SmartSocket.sapi.uploadSensors();
				// SmartSocket.sapi.initSensorData();
			} catch (Exception e) {
				System.out.println(">>> Exception, upload sensor" + e.toString());
			}
		}
	}
}*/
