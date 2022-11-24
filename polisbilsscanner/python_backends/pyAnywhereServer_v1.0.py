from flask import Flask, request
from flask_cors import CORS
import requests
import json

app = Flask(__name__)
CORS(app)

@app.route("/test/", methods = ['POST'])
def helloWorld():

    img = request.get_data(as_text = True)

    url = 'https://api.platerecognizer.com/v1/plate-reader/'
    resp = requests.post(url, data = {"upload": img}, headers = {"Authorization": "Token 4ff6214ef7aa8d699af257ac8009f2d2f7146e2f"})

    if not resp.json()["results"]:
        return "Ingen bil"

    plate = str(resp.json()["results"][0]["plate"]).upper()

    with open('/home/haito/mysite/data.json', 'r') as myfile: data = myfile.read()

    obj = json.loads(data)

    polis = False

    for reg in obj["plates"]:
        if str(reg["number"]) == str(plate):
            polis = True
            break

    if polis:
        return plate + ", polis"
    else:
        return plate + ", inte polis"