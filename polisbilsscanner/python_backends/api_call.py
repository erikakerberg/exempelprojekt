import requests
import base64
import json

IMAGE_PATH = 'download.jpg'
SECRET_KEY = 'sk_a04893b6c1d4c78244fa1640'

with open(IMAGE_PATH, 'rb') as image_file:
    img_base64 = base64.b64encode(image_file.read())

url = 'https://api.platerecognizer.com/v1/plate-reader/'
response = requests.post(url, data = {"upload": img_base64}, headers = {"Authorization": "Token 4ff6214ef7aa8d699af257ac8009f2d2f7146e2f"})

plate = response.json()["results"][0]["plate"]

print(plate)