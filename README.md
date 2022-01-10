# WebRTC Meeting web application

The basic web application allows multiple participants to share and see others' audio and video.
The web server acts as signaling server. Signaling happens using https requests. No websokets are used.
Each participant maintains n-1  [RTCPeerConnection](https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection) in their user agent. For n participants, the server maintains only n records.

![ScreenShot](https://i.stack.imgur.com/Fo2XW.png)


### To get started:

- Download and extract the project.
- [Generate Server Certificate](https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/) to run the site on HTTPs.
Run following commands:
```cmd
%JAVA_HOME%/bin/keytool -genkey -alias tomcat -keyalg RSA -keystore E:/keys.jks -storepass mypass
```
- Edit server settings in `pom.xml`
```html
<httpsPort>8443</httpsPort>
<keystoreFile>E:\keys.jks</keystoreFile>
<keystorePass>mypass</keystorePass>
```
- Start the server. Run following in project root:
```cmd
mvn
```
- Open webpages using url:
```
https://127.0.0.1:8443/vc
```
Ignore insecure page warnings in browser for testing.


```
