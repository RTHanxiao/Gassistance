from openai import OpenAI
from transformers import AutoTokenizer
import os
os.environ["TOKENIZERS_PARALLELISM"] = "false"

import time
import threading
import json
import _thread
from tqdm import tqdm
from datetime import datetime, timedelta
import logging
from contextlib import contextmanager
from collections import defaultdict
import pandas as pd
# Load model directly
from transformers import GPT2Tokenizer



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
            client = OpenAI(
                base_url="https://api.xty.app/v1",  #添加openai url
                api_key="sk-Ib89Gqp1lbuZsKRqCe229cA3942449FfA816D1Cd9aD77f16"# 添加你的openai key
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

def openai_res_out():
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
