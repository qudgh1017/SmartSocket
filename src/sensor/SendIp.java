package sensor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//smartsocket의 contentInstance(ip(순시전력)값) CREATE
public class SendIp {

	// 바다 URL
	String testServerURL = "http://203.254.173.111:7576/bada"; // 내부용(테스트용)
	String realServerURL = "http://203.253.128.166:7576/bada"; // 외부용(대쉬보드)

	//java.net.MalformedURLException: no protocol: "URL" => 앞에 http:// 붙여주기
	
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
			// (postman Mobius_API의 contentInstance CREATE 부분의 Header)
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("X-M2M-RI", "123sdfgd45");
			con.setRequestProperty("X-M2M-Origin", "S20170717074825768bp2l");
			con.setRequestProperty("Content-Type", "application/json; ty=4");

			// add request body
			// (postman Mobius_API의 contentInstance CREATE 부분의 body)
			// 보낼 contentInstnace 값 (ip 순시전력)
			String urlParams = "{    \"m2m:cin\": {        \"con\": \" " + contentInstance + "\"    }}";
			System.out.println(urlParams);
			
			// Send 'post' request
			// contentInstance 값 보내기
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();

			// 연결됬는지 안됬는지 확인
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
