import redis
import time
import json
import sys
import os

from flask import Flask, request
from flask_cors import CORS


def getRedisConn():
    rHost = "127.0.0.1"
    rPort = 6379
    rDb = 0
    rPwd = "your_password"
    redisConn = redis.Redis(host=rHost, port=rPort,
                            db=rDb, password=rPwd, decode_responses=True)
    return redisConn


# 创建 3 个对象
app = Flask(__name__)
app.debug = True
CORS(app, supports_credentials=True)


@app.route('/api/userRegister', methods=["GET", "POST"])
def userRegister():
    uid = request.args.get("uid")
    hid = request.args.get("hid")
    pwd = request.args.get("pwd")
    tel = request.args.get("tel")

    key = "hhuc/user/{}".format(uid)
    val = {
        "uid": uid,
        "hid": hid,
        "pwd": pwd,
        "tel": tel
    }
    conn = getRedisConn()
    if conn.exists(key):
        resInfo = {
            "status": "2"
        }
    else:
        conn.hmset(key, val)
        resInfo = {
            "status": "1",
            "userInfo": val
        }
    return json.dumps(resInfo)


@app.route('/api/userLogin', methods=["GET", "POST"])
def userLogin():
    res = {"status": 0}
    uid = request.args.get("uid")
    pwd = request.args.get("pwd")

    key = "hhuc/user/{}".format(uid)
    conn = getRedisConn()
    if conn.exists(key):
        info = conn.hgetall(key)
        if info['pwd'] == pwd:
            res = info
            res['status'] = 1
        else:
            res['status'] = 2
    return json.dumps(res)


@app.route('/api/userChangePwd', methods=["GET", "POST"])
def userChangePwd():
    uid = request.args.get("uid")
    pwd = request.args.get("pwd")
    newpwd = request.args.get("newpwd")

    key = "hhuc/user/{}".format(uid)
    conn = getRedisConn()
    if conn.exists(key):
        info = conn.hgetall(key)
        if info['pwd'] == pwd:
            conn.hset(key, "pwd", newpwd)
            resInfo = {
                "status": "1"
            }
        else:
            resInfo = {
                "status": "2"
            }
    else:
        resInfo = {
            "status": "3"
        }
    return json.dumps(resInfo)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=20082, threaded=True, debug=True)
