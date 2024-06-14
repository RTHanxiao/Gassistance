import json, time, logging
import os
from pprint import pprint
from fastapi import requests




def searchbing(query):
    # Add your Bing Search V7 subscription key and endpoint to your environment variables.
    subscription_key = "f33434ac02394e9f8a0b90fe77e953a6"
    endpoint = "https://api.bing.microsoft.com/" + "v7.0/search"
    mkt = 'en-US'
    params = { 'q': query, 'mkt': mkt }
    headers = { 'Ocp-Apim-Subscription-Key': subscription_key }

    # Call the API
    retry_interval_exp = 0
    while True:
        try:
            response = requests.get(endpoint, headers=headers, params=params)
            response.raise_for_status()
            return response.json()
        except Exception as ex:
            logging.warning("Exception...")
            if retry_interval_exp > 6:
                return {}
            time.sleep(max(4, 0.5 * (2 ** retry_interval_exp)))
            retry_interval_exp += 1


path = "../datasets_query/tasks/data/hotpot_dev_v1_simplified_query.json"    #搜索query数据的位置
out_path = "bing-hotpot-searchresult-pt.json"   #结果输出的位置，保存为json数据
with open(path,'r',encoding="utf-8") as f:
    data = json.load(f)

querys = []
obs = []

for dict in data:
    query = dict['query']

    ob = ""
    response_json = ''
    try:
        response_json = searchbing(query)

    except:
        print("search error")
    res_text = []
    hint = " Hint: "
    if 'entities' in response_json.keys() and 'value' in response_json['entities'].keys():
        for en in response_json['entities']['value']:
            if 'description' in en.keys():
                res_text.append(en['description'])
            if 'name' in en.keys():
                hint += en['name'] + ' '
    if 'webPages' in response_json.keys() and 'value' in response_json['webPages'].keys() and len(res_text) == 0:
        for en in response_json['webPages']['value']:
            # do some filter for mmlu
            if 'name' in en.keys() and 'snippet' in en.keys():
                res_text.append(' '.join([en['name'], en['snippet']]))
    if len(res_text) == 0:
        print(response_json)
        # os._exit()
        res_text = ['']
    ob = ' '.join(res_text)
    if hint != " Hint: ":
        ob += hint[:-1]

    di = {'id':dict['id'],'question':dict['question'],'query':query,'passage':ob}
    obs.append(di)
    if len(obs)%10 == 0:
        with open('bing-hotpot-searchresult-pt.json','w',encoding='utf-8') as f:
            json.dump(obs,f)
with open('bing-hotpot-searchresult-pt.json','w',encoding='utf-8') as f:
    json.dump(obs,f)





