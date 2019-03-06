# SmartSocket

<스마트 소켓>
전력량을 체크하는 센서(스마트 소켓)를 Zigbee통신을 이용하여 Raspberry pi 3와 연동하여 Bada에 센서값 보내주기.



IoT  서버 플랫폼(Bada 안에 Mobius 사용)
Bada는 oneM2M 기반
oneM2M은 리소스 구조이다
AE >> Container >> ContentInstance
- AE(Application Entity)(ex)자동차) 리소스 존재
  - Container(ex) 속도, 엔진온도, 냉각수 온도)
     - ContentInstance(ex) 각 Container안에 속하는 값)
로 구성되어 있다.
