from django.shortcuts import render
from django.http import JsonResponse, HttpResponse
from django.db import connection
from django.views.decorators.csrf import csrf_exempt
import os, time
from django.conf import settings
from django.core.files.storage import FileSystemStorage
import json

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


