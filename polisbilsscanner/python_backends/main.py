import requests
import base64
import json
import time
import cv2

cam = cv2.VideoCapture(0)

cv2.namedWindow("Polisscanner")

text = "Ingen bil"

while True:

    ret, frame = cam.read()
    
    if not ret:
        print("failed to grab frame")
        break

    font = cv2.FONT_HERSHEY_SIMPLEX 
    org = (0, 50) 
    fontScale = 1
    color = (0, 255, 0) 
    thickness = 2

    k = cv2.waitKey(1)

    if k % 256 == 27:
        break

    elif k % 256 == 32:
        img_name = "opencv_frame_1.png"

        cv2.imwrite(img_name, frame)

        with open(img_name, 'rb') as image_file:
            img_base64 = base64.b64encode(image_file.read())

        url = 'https://api.platerecognizer.com/v1/plate-reader/'
        response = requests.post(url, data = {"upload": img_base64}, headers = {"Authorization": "Token 4ff6214ef7aa8d699af257ac8009f2d2f7146e2f"})

        if not response.json()["results"]:
            text = "Ingen bil"
            continue
        
        plate = str(response.json()["results"][0]["plate"]).upper()

        with open('data.json', 'r') as myfile: data = myfile.read()

        obj = json.loads(data)

        polis = False

        for reg in obj["plates"]:
            if str(reg["number"]) == str(plate):
                polis = True
                break
        
        if polis:
            text = plate + " - POLIS"
        else:
            text = plate + " - Inte polis"
    
    frame = cv2.putText(frame, text, org, font, fontScale, color, thickness, cv2.LINE_AA)
    cv2.imshow("test", frame)


cam.release()

cv2.destroyAllWindows()