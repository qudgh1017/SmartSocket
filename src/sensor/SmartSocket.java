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

		// serial 데이터 발생시 이벤트 실행
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
			// 라즈베리파이 시리얼 포트, 속도관련
			serial.open("/dev/ttyUSB0", 38400);
			
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
		ScheduledJob job = new ScheduledJob();
		Timer jobScheduler = new Timer();
		jobScheduler.scheduleAtFixedRate(job, 0, 1000); // delay-period 일정시간 간격으로 작업 실행
		while (true) {
			;
			Thread.sleep(1000);
		}
	}// main
}

//작업 스케줄링을 위한 양식
class ScheduledJob extends TimerTask {
	int btnCount = 0;

	ScheduledJob() {
		;
	}

	//jobScheduler.scheduleAtFixedRate(job, 0, 5000); // delay-period 일정시간 간격으로 이 부분을 실행
	public void run() {
		String[] st = null;
		String[] values = null;
		String ae = null;
		String container = "power_consumption"; // 항상 같은 이름
		String ip = null;
		boolean dataReady = false;

		SmartSocket.rxdata = SmartSocket.rxdata.replaceAll("\\s+", ""); // 공백, 특수문자 제거
		st = SmartSocket.rxdata.split("SS_NO:"); // st[0]="" st[1]=" 1003, 297129736, 283, 01, \n\r"
		SmartSocket.rxdata = "";
		
		System.out.println("111");
		for (int i = 0; i < st.length; i++) {
			/*if(st[i].equals("")){ // st[0]일때 ""이여서 불필요한 처리 없앰
				System.out.println("st[" + i + "]>>>>>>>>>>>>>>>>>>>continue넘어감");
				continue;
			}*/
			if (st[i].split(",").length == 4) { // 1004, 1055, 39, 01
				values = st[i].split(",");
				if (Integer.parseInt(values[0]) >= 1000) { // id가 1000보다 큼
					ae = "smartsocket_";
					ae += String.valueOf(Integer.parseInt(values[0])); // AE 이름
					ip = String.valueOf(Integer.parseInt(values[2])); // 순시전력
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
// 기존 코드
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

		SmartSocket.rxdata = SmartSocket.rxdata.replaceAll("\\s+", ""); // 공백,
																		// 특수문자
																		// 제거
		st = SmartSocket.rxdata.split("SS_NO:");
		SmartSocket.rxdata = "";

		long timestamp = System.currentTimeMillis();

		for (int i = 0; i < st.length; i++) {
			if (st[i].split(",").length == 4) { // 1004, 1055, 39, 01
				values = st[i].split(",");
				if (Integer.parseInt(values[0]) >= 1000) { // id가 1000보다 큼
					ip = String.valueOf(Integer.parseInt(values[2]) / 100); // 순시전력
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
