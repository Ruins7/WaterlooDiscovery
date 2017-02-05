#!/usr/bin/python
from geopy.distance import vincenty
from flask import Flask
from flask import request, jsonify
from flask_sqlalchemy import SQLAlchemy
from models import *

import googlemaps

gmaps = googlemaps.Client(key='')

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "mysql://user:pass@localhost/enghack"
db = SQLAlchemy(app)
num_of_rounds = 5

def check_task_by_type(task, body):
    if task.taskType == 'place':
        _lat, _lng = map(float, task.answer.split(','))
        return vincenty((_lat, _lng), (body['lat'], body['lng'])).meters < 50
    elif task.taskType == 'type':
        _ret = gmaps.places_nearby(radius=50, type=task.answer,
                location=(body['lat'], body['lng']))
        print _ret
        return _ret and _ret['status'] == 'OK' and len(_ret['results']) > 0
    elif task.taskType == 'multiple':
        _locs = [ map(float, _pair_string.split(',')) for _pair_string in task.answer.split(';') ]
        _ret = False
        _lat, _lng = map(float, task.answer.split(','))
        for _loc in _locs:
            if vincenty((_loc[0], _loc[1]), (body['lat'], body['lng'])).meters < 50:
                _ret = True
                break
    elif task.taskType == 'any':
        return True
    else:
        return False

@app.route('/start', methods=['POST'])
def startTask():
    body = request.get_json()
    email = body.get('email')
    taskid = int(body.get('taskid', 1))
    _user = db.session.query(User).filter(User.email==email).scalar()
    _user.currentTask = taskid
    _task = db.session.query(Task).filter(Task.id==taskid).scalar()
    _usertask = db.session.query(UserTask).filter(UserTask.email==email,
            UserTask.taskid==_task.id).scalar()
    if _usertask:
        _usertask.punish = 0
        _usertask.startat = datetime.datetime.now()
    else:
        _usertask = UserTask(
            email=_user.email,
            taskid=taskid,
            punish=0,
            startat=datetime.datetime.now())
        db.session.merge(_usertask)
    _user.progress = True
    _ret = _task.serialize()
    db.session.commit()
    return jsonify( _ret if _task else {} )

@app.route('/checkin', methods=['POST'])
def checkTask():
    body = request.get_json()
    email = body.get('email')
    lat = float(body.get('lat', 0))
    lng = float(body.get('lng', 0))

    _user = db.session.query(User).filter(User.email==email).scalar()
    _task = db.session.query(Task).filter(Task.id==_user.currentTask).scalar()
    _ret = check_task_by_type(_task, body)
    _usertask = db.session.query(UserTask).filter(UserTask.email==email,
            UserTask.taskid==_task.id).scalar()

    _json = {'ret': _ret}
    if _ret:
        _usertask.finishat=datetime.datetime.now()
        _delta = _usertask.finishat - _usertask.startat
        _json['time'] = "%s days %s H %s M %s S" % (str(_delta.days),
                str(_delta.seconds/3600), str(_delta.seconds%3600/60),
                str(_delta.seconds%3600%60))
        _user.progress = False
        if _user.currentTask < num_of_rounds:
            _user.currentTask = _user.currentTask + 1
        db.session.merge(_user)
        db.session.merge(_usertask)
    else:
        if _usertask:
            _usertask.punish = (_usertask.punish if _usertask.punish else 0) + 1
            db.session.merge(_usertask)
    db.session.commit()
    return jsonify(_json)

@app.route('/login', methods=['POST'])
def login():
    body = request.get_json()
    email = body.get('email')
    password = body.get('password')
    _user = db.session.query(User).filter(User.email==email).options(
            joinedload(User.task)
        ).scalar()
    if _user and password == '123':
        return jsonify(_user.serialize())
    else:
        return jsonify({})

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)
