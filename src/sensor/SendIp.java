package sensor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//smartsocket�� contentInstance(ip(��������)��) CREATE
public class SendIp {

	// �ٴ� URL
	String testServerURL = "http://203.254.173.111:7576/bada"; // ���ο�(�׽�Ʈ��)
	String realServerURL = "http://203.253.128.166:7576/bada"; // �ܺο�(�뽬����)

	//java.net.MalformedURLException: no protocol: "URL" => �տ� http:// �ٿ��ֱ�
	
	public void SendToBada(String ae, String container, String contentInstance) {

		
		int responseCode = 0;
		StringBuffer response = null;
		String responseMessage = null;
		Object serverResponse = null;

		try {
			// String url = testServerURL + "/" + ae + "/" + container;
			String url = realServerURL + "/" + ae + "/" + container;
			URL obj = null;

			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// add request header
			// (postman Mobius_API�� contentInstance CREATE �κ��� Header)
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("X-M2M-RI", "123sdfgd45");
			con.setRequestProperty("X-M2M-Origin", "S20170717074825768bp2l");
			con.setRequestProperty("Content-Type", "application/json; ty=4");

			// add request body
			// (postman Mobius_API�� contentInstance CREATE �κ��� body)
			// ���� contentInstnace �� (ip ��������)
			String urlParams = "{    \"m2m:cin\": {        \"con\": \" " + contentInstance + "\"    }}";
			System.out.println(urlParams);
			
			// Send 'post' request
			// contentInstance �� ������
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();

			// �������� �ȉ���� Ȯ��
			responseCode = con.getResponseCode();
			responseMessage = con.getResponseMessage();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println(">>> ip : " + response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	

	}
}
