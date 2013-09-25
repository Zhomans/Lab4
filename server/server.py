from flask import Flask
from flask import request
from flask import json
global datalist

app = Flask(__name__)
datalist = dict()

@app.route('/get', methods = ['GET'])
def get():
	data = request.args.get('phone','default')
	total = "|".join(["&".join(otherData) for phone,otherData in datalist.items() if data != phone])
	return total

@app.route('/post')
def post():
	print "Here"
	if request.method == 'POST':
		data = json.loads(request.data)
		datalist[data['phone']] = [data['lat'],data['lon'],data['vel']] 
		return "Success"
	else:
		print "It's okay android."
		return "You sent nothing, fool."

if __name__ == "__main__":
	app.run(host='0.0.0.0')