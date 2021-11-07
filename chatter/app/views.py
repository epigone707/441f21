from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import os, time
from django.conf import settings
from django.core.files.storage import FileSystemStorage
import json
from google.oauth2 import id_token
from google.auth.transport import requests

import hashlib


@csrf_exempt
def adduser(request):
    if request.method != 'POST':
        return HttpResponse(status=404)

    json_data = json.loads(request.body)
    clientID = json_data['clientID']   # the front end app's OAuth 2.0 Client ID
    idToken = json_data['idToken']     # user's OpenID ID Token, a JSon Web Token (JWT)

    now = time.time()                  # secs since epoch (1/1/70, 00:00:00 UTC)

    try:
        # Collect user info from the Google idToken, verify_oauth2_token checks
        # the integrity of idToken and throws a "ValueError" if idToken or
        # clientID is corrupted or if user has been disconnected from Google
        # OAuth (requiring user to log back in to Google).
        # idToken has a lifetime of about 1 hour
        idinfo = id_token.verify_oauth2_token(idToken, requests.Request(), clientID)
    except ValueError:
        # Invalid or expired token
        return HttpResponse(status=511)  # 511 Network Authentication Required

    # get username
    try:
        username = idinfo['name']
    except:
        username = "Profile NA"

    # Compute chatterID and add to database
    backendSecret = "giveamouseacookie"   # or server's private key
    nonce = str(now)
    hashable = idToken + backendSecret + nonce
    chatterID = hashlib.sha256(hashable.strip().encode('utf-8')).hexdigest()

    # Lifetime of chatterID is min of time to idToken expiration
    # (int()+1 is just ceil()) and target lifetime, which should
    # be less than idToken lifetime (~1 hour).
    lifetime = min(int(idinfo['exp']-now)+1, 60) # secs, up to idToken's lifetime

    cursor = connection.cursor()
    # clean up db table of expired chatterIDs
    cursor.execute('DELETE FROM chatters WHERE %s > expiration;', (now, ))

    # insert new chatterID
    # Ok for chatterID to expire about 1 sec beyond idToken expiration
    cursor.execute('INSERT INTO chatters (chatterid, username, expiration) VALUES '
                   '(%s, %s, %s);', (chatterID, username, now+lifetime))

    # Return chatterID and its lifetime
    return JsonResponse({'chatterID': chatterID, 'lifetime': lifetime})


@csrf_exempt
def postauth(request):
    if request.method != 'POST':
        return HttpResponse(status=404)
    json_data = json.loads(request.body)

    chatterID = json_data['chatterID']
    message = json_data['message']

    cursor = connection.cursor()
    cursor.execute('SELECT username, expiration FROM chatters WHERE chatterID = %s;', (chatterID,))

    row = cursor.fetchone()
    now = time.time()
    if row is None or now > row[1]:
        # return an error if there is no chatter with that ID
        return HttpResponse(status=401) # 401 Unauthorized

    # Else, insert into the chatts table
    cursor.execute('INSERT INTO chatts (username, message) VALUES (%s, %s);', (row[0], message))
    return JsonResponse({})



@csrf_exempt
def postimages(request):
    if request.method != 'POST':
        return HttpResponse(status=400)

    # loading form-encoded data
    username = request.POST.get("username")
    message = request.POST.get("message")

    if request.FILES.get("image"):
        content = request.FILES['image']
        filename = username+str(time.time())+".jpeg"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        imageurl = fs.url(filename)
    else:
        imageurl = None

    if request.FILES.get("video"):
        content = request.FILES['video']
        filename = username+str(time.time())+".mp4"
        fs = FileSystemStorage()
        filename = fs.save(filename, content)
        videourl = fs.url(filename)
    else:
        videourl = None
        
    cursor = connection.cursor()
    cursor.execute('INSERT INTO images (username, message, imageurl, videourl) VALUES '
                   '(%s, %s, %s, %s);', (username, message, imageurl, videourl))

    return JsonResponse({})

def getchatts(request):
        if request.method != 'GET':
                return HttpResponse(status=404)
        cursor = connection.cursor()
        cursor.execute('SELECT * FROM chatts ORDER BY time DESC;')
        rows = cursor.fetchall()
        
        response = {}
        response['chatts'] = rows
        return JsonResponse(response)

def getimages(request):
        if request.method != 'GET':
                return HttpResponse(status=404)
        cursor = connection.cursor()
        cursor.execute('SELECT * FROM images ORDER BY time DESC;')
        rows = cursor.fetchall()

        response = {}
        response['chatts'] = rows
        return JsonResponse(response)

# Create your views here#
@csrf_exempt
def postchatt(request):
    if request.method != 'POST':
        return HttpResponse(status=404)
    json_data = json.loads(request.body)
    username = json_data['username']
    message = json_data['message']
    cursor = connection.cursor()
    cursor.execute('INSERT INTO chatts (username, message) VALUES '
                   '(%s, %s);', (username, message))
    return JsonResponse({})

@csrf_exempt
def getaudio(request):
        if request.method != 'GET':
                return HttpResponse(status=404)
        cursor = connection.cursor()
        cursor.execute('SELECT * FROM audio ORDER BY time DESC;')
        rows = cursor.fetchall()

        response = {}
        response['chatts'] = rows
        return JsonResponse(response)


@csrf_exempt
def postaudio(request):
    if request.method != 'POST':
        return HttpResponse(status=404)
    json_data = json.loads(request.body)
    username = json_data['username']
    message = json_data['message']
    audio = json_data['audio']
    cursor = connection.cursor()
    cursor.execute('INSERT INTO audio (username, message, audio) VALUES '
                   '(%s, %s, %s);', (username, message, audio))
    return JsonResponse({})

@csrf_exempt
def postmaps(request):
    if request.method != 'POST':
        return HttpResponse(status=404)
    json_data = json.loads(request.body)
    username = json_data['username']
    message = json_data['message']
    geodata = json_data['geodata']
    cursor = connection.cursor()
    cursor.execute('INSERT INTO maps (username, message, geodata) VALUES '
                   '(%s, %s, %s);', (username, message, geodata))
    return JsonResponse({})

@csrf_exempt
def getmaps(request):
    if request.method != 'GET':
    	return HttpResponse(status=404)
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM maps ORDER BY time DESC;')
    rows = cursor.fetchall()
       
    response = {}
    response['chatts'] = rows
    return JsonResponse(response)


