from flask import Flask, request
from flask_cors import CORS
import requests
import json

app = Flask(__name__)
CORS(app)

@app.route("/test/", methods = ["POST"])
def helloWorld():

    try:

        img = request.get_data(as_text = True)

        url = "https://api.platerecognizer.com/v1/plate-reader/"
        resp = requests.post(url, data = {"upload": img}, headers = {"Authorization": "Token 4ff6214ef7aa8d699af257ac8009f2d2f7146e2f"})

        if not resp.json()["results"]:
            return "Ingen\nbil"

        plate = str(resp.json()["results"][0]["plate"]).upper()

        if len(plate) > 6: plate = plate[:6]

        if "0" in plate[:3]:
            plate0 = plate[:3].replace("0", "O")
            plate = plate0 + plate[3:6]

        if "O" in plate[3:6]:
            plate1 = plate[3:6].replace("O", "0")
            plate = plate[:3] + plate1


        with open("/home/haito/mysite/data.json", "r") as myfile: data = myfile.read()

        obj = json.loads(data)

        polis = False

        for reg in obj["plates"]:
            if str(reg["number"]) == str(plate):
                polis = True
                break

        if polis:
            return plate + "\npolis"
        else:
            return plate + "\ninte polis"

    except:

        return "Error"