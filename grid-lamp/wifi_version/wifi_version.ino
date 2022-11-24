#include <SPI.h>
#include <Ethernet.h>
#include <Adafruit_NeoPixel.h>
#include <ESP8266WiFi.h>

String ssid = "F&Es_WIFI";
String password = "himym123";

WiFiServer server(80);
Adafruit_NeoPixel grid = Adafruit_NeoPixel(40, 15);

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);

  pinMode(2, OUTPUT);
  digitalWrite(2, LOW);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();

  

  grid.begin();

  grid.setBrightness(100);
  
  for (int i = 0; i < 40; i++) {
    grid.setPixelColor(i, 255, 255, 255);
  }
  grid.show();
}

void loop() {
  WiFiClient client = server.available();
  
  if (client) {
    Serial.println("new client");

    String request;
    
    while (client.connected()) {
      if (client.available()) {
        
        char c = client.read();

        request += c;
        
        Serial.write(c);

        if (c == '\n') {
          request = request.substring(6, request.length() - 10);
          
          client.println("HTTP/1.1 200 OK");
          client.println("Access-Control-Allow-Origin: *");
          client.println("Content-Type: application/json;charset=utf-8");
          client.println("Server: Arduino");
          client.println("Connection: close");
          client.println();

          int i = request.substring(0, 2).toInt();
          int r = request.substring(2, 5).toInt();
          int g = request.substring(5, 8).toInt();
          int b = request.substring(8, 11).toInt();

          if (i == 40) {
            for (int j = 0; j < 40; j++) {
              grid.setPixelColor(j, r, g, b);
            }
          } else {
            grid.setPixelColor(i, r, g, b);
          }
            
          grid.show();
          
          break;
        }
      }
    }
    client.stop();
    Serial.println("client disconnected");
  }
}
