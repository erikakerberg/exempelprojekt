import io
import json
with io.open("plates.txt",'r',encoding='utf8') as f:
    dataAsText = f.read()

#Rad 5-104 är bilar

dataAsArray = dataAsText.split("\n")
carsAsArray = []

i = -1

while dataAsArray.__contains__("Märke / Modell	Regnr	Färg	Typ	Årtal"):
    i1 = dataAsArray.index("Märke / Modell	Regnr	Färg	Typ	Årtal") + 1
    i2 = dataAsArray.index("«")
    carsAsArray += dataAsArray[i1:i2]
    del dataAsArray[0:i2+1]

print(len(carsAsArray))

carsSeperated = list(map(lambda e: e.split("\t"), carsAsArray))

plates = []

for car in carsSeperated:
    plates.append({"number": car[1]})

jsonObject = {"plates": plates}

json_dump = json.dumps(jsonObject)

with io.open("data.json",'w',encoding='utf8') as f:
    f.truncate()
    f.write(json_dump)
    f.close()