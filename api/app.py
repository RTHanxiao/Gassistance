import shutil
from fastapi import FastAPI, HTTPException
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import FileResponse, JSONResponse
import numpy as np
from pydantic import BaseModel
from pydub import AudioSegment
from IPython.display import Audio
import subprocess
import speech_recognition as sr
import soundfile
import torchaudio
import torch
import openai 
import requests
import json
import os
import logging
import time
import uvicorn
import ChatTTS

app = FastAPI()

# 配置你的API密钥
BING_API_KEY = 'f33434ac02394e9f8a0b90fe77e953a6'
OPENAI_API_KEY = 'sk-Ib89Gqp1lbuZsKRqCe229cA3942449FfA816D1Cd9aD77f16'
BING_API_ENDPOINT = "https://api.bing.microsoft.com/v7.0/search" 
OPENAI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions"
OPENAI_API_ENDPOINT_TRANSIT = "https://api.xty.app/v1/"
UPLOAD_DIRECTORY = "temp"
base_path="D:/Coding/Project/Android/GAssistance/api/ChatTTS"


if not os.path.exists(UPLOAD_DIRECTORY):
    os.makedirs(UPLOAD_DIRECTORY)


class Question(BaseModel):
    question: str

def search_bing(query):
    headers = {'Ocp-Apim-Subscription-Key': BING_API_KEY}
    params = {'q': query, 'mkt': 'en-US'}
    retry_interval_exp = 0

    while True:
        try:
            response = requests.get(BING_API_ENDPOINT, headers=headers, params=params)
            response.raise_for_status()
            return response.json()
        except Exception as ex:
            logging.warning(f"Exception: {ex}")
            if retry_interval_exp > 6:
                return {}
            time.sleep(max(4, 0.5 * (2 ** retry_interval_exp)))
            retry_interval_exp += 1
            return 

def extract_passages(response_json):
    passages = []
    hint = "Hint:"

    if 'entities' in response_json.keys() and 'value' in response_json['entities'].keys():
        for en in response_json['entities']['value']:
            if 'description' in en.keys():
                passages.append(en['description'])
            if 'name' in en.keys():
                hint += en['name'] + ' '
    
    if 'webPages' in response_json.keys() and 'value' in response_json['webPages'].keys() and len(passages) == 0:
        for en in response_json['webPages']['value']:
            if 'name' in en.keys() and 'snippet' in en.keys():
                passages.append(''.join([en['name'], en['snippet']]))
    
    if len(passages) == 0:
        logging.warning("No passages found in response.")
        passages = ['']

    ob = ''.join(passages)
    if hint != "Hint:":
        ob += hint[:-1]

    #print("ob:"+ob)
    return ob

# def bing_in_testdata():
    path ="../datasets guerytasks/data/hotpot_dev_v1_simplifed_query.json"#搜索query数据的位置
    out_path ="bing-hotpot-searchresult-pt.json" #结果输出的位置，保存为json数据
    with open(path,'r',encoding="utf-8") as f:
        data = json.load(f)

    querys =[]
    obs =[]

    for dict in data:
        query = dict['query']

        ob = ""
        response_json = ""
        try:
            response_json = search_bing(query)
        except:
            print("search error")
        res_text =[]
        hint ="Hint:"
        if 'entities' in response_json.keys() and 'value' in response_json['entities'].keys():
            for en in response_json['entities']['value']:
                if 'description'in en.keys():
                    res_text.append(en['description'])
                if 'name' in en.keys():
                    hint += en['name']+''
        if 'webPages' in response_json.keys() and 'value' in response_json['webPages'].keys() and len(res_text) == 0:
            for en in response_json['webPages']['value']:
                # do some filter for mmlu
                if 'name' in en.keys()and 'snippet' in en.keys():
                    res_text.append(''.join([en['name'],en['snippet']]))
        if len(res_text) == 0 :
            print(response_json)
            # os._exit()
            res_text = ['']
        ob = ''.join(res_text)
        if hint != "Hint:":
            ob += hint[:-1]

        di= {'id':dict['id'],'question':dict['question'],'query':query,'passage':ob}
        obs.append(di)
        if len(obs)%10== 0:
            with open(out_path,'w',encoding='utf-8') as f:
                json.dump(obs,f)
    with open(out_path,'w',encoding='utf-8') as f:
        json.dump(obs,f)


def call_openai_api(prompt):
    # headers = {
    #     'Authorization': f'Bearer {OPENAI_API_KEY}',
    #     'Content-Type': 'application/json'
    # }
    # data = {
    #     'model': 'gpt-3.5-turbo',
    #     'messages': [
    #         {'role': 'system', 'content': 'You are a helpful assistant.'},
    #         {'role': 'user', 'content': prompt}
    #     ]
    # }
    # response = requests.post(OPENAI_API_ENDPOINT_TRANSIT, headers=headers, json=data)
    # try:
    #     response.raise_for_status()
    #     return response.json()['choices'][0]['message']['content']
    try:
            client = openai.OpenAI(
                base_url=OPENAI_API_ENDPOINT_TRANSIT,  #添加openai url
                api_key=OPENAI_API_KEY# 添加你的openai key
            )
            completion = client.chat.completions.create(
                model="gpt-3.5-turbo",

                messages=[
                    {"role": "system", "content": prompt},
                    {"role": "user", "content": prompt}
                ]
            )
            #print(completion.choices[0].message.content)
            text = completion.choices[0].message.content
           
            if '**' in text:
                text = text.replace("**", "")
            if '\n' in text:
                text = text.replace("\n", '')
            return text

    except openai.error.OpenAIError as e:
        logging.error(f"OpenAI API error: {e}")
        raise HTTPException(status_code=500, detail="OpenAI API request failed")


def complete(
    prompt, dict,max_tokens=100, temperature=0, logprobs=None, n=1,
    frequency_penalty=0, presence_penalty=0, stop=None, rstrip=False,
    partition_id=None, **kwargs
):
    i = 0
    out = []
    outpath = 'hot_answer_result.json' #结果输出路径
    for p in prompt:
        try:
            client = openai.OpenAI(
                base_url=OPENAI_API_ENDPOINT_TRANSIT,  #添加openai url
                api_key=OPENAI_API_KEY# 添加你的openai key
            )
            completion = client.chat.completions.create(
                model="gpt-3.5-turbo",

                messages=[
                    {"role": "system", "content": p},
                    {"role": "user", "content": p}
                ]
            )
            print(completion.choices[0].message.content)
            text = completion.choices[0].message.content
            question = dict[i]['question']
            if '**' in text:
                text = text.replace("**", "")
            if '\n' in text:
                text = text.replace("\n", '')
        except:
            print("error")

        j = {"id": i, "question":question,"query": dict[i]['query'],'answer':text}
        i += 1
        print(j)
        out.append(j)
        if(i%10 == 0):
            with open (outpath,'w',encoding='utf-8') as f:
                json.dump(out,f)
    with open(outpath, 'w', encoding='utf-8') as f:
        json.dump(out, f)
    return out

# def openai_res_out():
    inpath = 'bing-hotpot-searchresult-pt.json' #输入搜索结果文件的路径
    if inpath.endswith('json'):
        data = json.load(open(inpath, 'r', encoding='utf8'))
    elif inpath.endswith('jsonl'):
        data = open(inpath, 'r', encoding='utf8').readlines()
        data = [json.loads(l, strict=False) for l in data]

    inlines = []
    dict = []
    for line in data:
        dict.append(line)

        p = "Answer the question with one entity in the following format, end the answer with '**'. \n\n Question: World heavyweight champion. Charles returned to boxing after the war as a light heavyweight, picking up many notable wins over leading light heavyweights, as well as heavyweight contenders Archie Moore, Jimmy Bivins, Lloyd Marshall and Elmer Ray. Ezzard Charles was a world champion in which sport? \n\n Answer: Prize fight.** \n\n Question: Nitrous oxide (dinitrogen oxide or dinitrogen monoxide), commonly known as laughing gas, nitrous, or nos, is a chemical compound, an oxide of nitrogen with the formula N2O. At room temperature, it is a colourless non-flammable gas, and has a slightly sweet scent and taste.[5] At elevated temperatures, nitrous oxide is a powerful oxidiser similar to molecular oxygen. What is the correct name of laughing gas? \n\n Answer: Nitrous oxide.** \n\n Question: {background} {query} \n\n Answer: "

        p = p.replace("{background}",line['passage']).replace('{query}',line['question'])
        inlines.append(p)


    out = complete(inlines,dict=dict,max_tokens=300)
    with open("hot_answer_result.json",'a+',encoding='utf-8') as f:
        json.dump(out,f)

def TTS(text, output_path): 
    chat = ChatTTS.Chat()
    chat.load_models(source='local',local_path=base_path)
    print("TTS A")
    wavs = chat.infer(text, use_decoder=True)
    audio_data = np.array(wavs[0])
    if audio_data.ndim == 1:
        audio_data = np.expand_dims(audio_data, axis=0)

    torchaudio.save(output_path, torch.from_numpy(wavs[0]), 24000)
    
    #soundfile.write(output_path, wavs[0], 24000, format='WAV')
    soundfile.write(output_path, audio_data.T, 24000)


@app.post("/audio")
async def upload_audio(file: UploadFile = File(...)): 
    try:
        # 保存上传的文件
        print("uploaded")
        file_location = os.path.join(UPLOAD_DIRECTORY, file.filename)
        with open(file_location, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 转换音频格式为wav
        audio = AudioSegment.from_file(file_location)
        wav_file_location = os.path.join(UPLOAD_DIRECTORY, "converted.wav")
        audio.export(wav_file_location, format="wav")
        print("converted")

        # 进行语音识别
        recognizer = sr.Recognizer()
        print("start recognize")
        with sr.AudioFile(wav_file_location) as source:
            audio_data = recognizer.record(source)
            text = recognizer.recognize_google(audio_data, language="zh-CN")
            print(text)

        prompt = f"passages\n\nQ: {text}\nA:"
        answer=call_openai_api(prompt)
        #answer="我是一个由OpenAI开发的语言模型，被称为ChatGPT。我可以回答你的问题，提供信息，或者与你进行对话。"
        print(answer)

        #答案转语音
        response_audio_path = os.path.join(UPLOAD_DIRECTORY, "response.wav")
        TTS(answer, response_audio_path)

        audio_url = f"http://192.168.99.108:8000/{response_audio_path}"
        
        return JSONResponse(content={"message": "File uploaded successfully.", "transcription": text,"answer":answer}, status_code=200)
    except Exception as e:
        return JSONResponse(content={"message": str(e)}, status_code=500)
    
@app.get("/download_audio")
async def download_audio():
    file_path = "D:/Coding/Project/Android/GAssistance/temp/response.wav"
    if os.path.exists(file_path):
        return FileResponse(path=file_path, filename="audio_file.wav", media_type='audio/wav')
    else:
        return {"error": "File not found"}

@app.post("/ask")
async def ask(question: Question):
    if not question.question:
        raise HTTPException(status_code=400, detail="Question is required")
    
    try:
        # 调用Bing API
        bing_results = search_bing(question.question)
        #print(bing_results)
        if not bing_results:
            raise HTTPException(status_code=500, detail="Bing API search failed")

        # 提取搜索结果中的摘要
        passages = extract_passages(bing_results)
        print(passages)

        # 构造OpenAI API的prompt
        prompt = f"{passages}\n\nQ: {question.question}\nA:"
        
        # 调用OpenAI API
        answer = call_openai_api(prompt)
        
        print(answer)

        return {"answer": answer}
    except:
        prompt = f"passages\n\nQ: {question.question}\nA:"

        answer = call_openai_api(prompt)

        print(answer)

        return {"answer": answer}

if __name__ == "__main__":

    uvicorn.run("app:app", host="0.0.0.0", port=8000,reload=True)
    
