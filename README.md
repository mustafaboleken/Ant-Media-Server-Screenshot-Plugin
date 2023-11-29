# Ant-Media-Server-Screenshot-Plugin
Take ScreenShot from a livestream using Rest Api

# Prerequests
- Install Ant Media Server
- Install Maven 

# Quick Start

- Clone the repository
  ```sh
  git clone https://github.com/mustafaboleken/Ant-Media-Server-Screenshot-Plugin.git
  ```
- Build the Screenshot Plugin
  ```sh
  mvn install  -Dgpg.skip=true
  ```
- Copy the generated jar file to your Ant Media Server's plugin directory
  ```sh
  cp target/ScreenshotPlugin.jar /usr/local/antmedia/plugins
  ```
- Restart the Ant Media Server
  ```
  sudo service antmedia restart
  ```
- Publish a Live Stream to Ant Media Server with WebRTC/RTMP/RTSP
- Before you take a screenshot, first you need to register livestreams once. Call the REST Method below to register a livestream. You should pass stream id as path parameter
```
curl -i -X POST -H "Accept: Application/json" -H "Content-Type: application/json" "https://<ant-media-server-domain>/<your-webapp-name>/rest/screenshot-plugin/register/{streamId}"
```
- Call the REST Method below to save screenshot from the livestream. You should pass stream id as query parameter
```
curl -i -X POST -H "Accept: Application/json" -H "Content-Type: application/json" "https://<ant-media-server-domain>/<your-webapp-name>/rest/screenshot-plugin/take-screenshot?streamId={streamId}"
```