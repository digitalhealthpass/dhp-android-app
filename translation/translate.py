# Instructions:
# 1. Place quickstart.py (this file), token.json and credentials.json files in the same directory.
# 2. Install Python 3.* and iPython3
# 3. From iPython3 interface execute the command 'run quickstart.py'
# 4. To create copy use the below commands (filename arguments are optional, default filenames can be seen in global filename objects below):
#
#     Each platform:
#     create_android_copy(content_android, filename, filename_fr)
#     create_ios_copy(content_ios, filename, filename_fr)
#
#     Each file:
#     transform_android_copy(content,filename)
#     transform_android_copy_fr(content,filename_fr)
#     transform_ios_copy(content, filename)
#     transform_ios_copy_fr(content, filename_fr)
# documentation reference can be found at
# https://developers.google.com/sheets/api/quickstart/python?authuser=2
# to run the file use "python3 translate.py"

from __future__ import print_function
from openpyxl import load_workbook

import os
import numpy as np
import pandas as pd
import re
import time
from MultiLang import MultiLang
from LangFile import LangFile

PATH_ANDROID_BASE = "../app/src/main/res/values-"

LANGUAGE_SUPPORTED = []
COLUMNS_TO_PARSE = []
COLUMN_KEY = 'Key' #todo read name from class

def main():
    return

def if_null(item):
    if item:
        return str(item)
    else:
        x = ''
        return str(x)

'''Replace all empty bracket [] with android variable reference: %[#]$s'''
def parse_var_android(df):
    for col in range(len(COLUMNS_TO_PARSE)):
        for item in range(len(df[COLUMNS_TO_PARSE[col]])):
            #if string has html tag, then add `CDATA` to it, to make sure there are no modification
            if "<html" in df[COLUMNS_TO_PARSE[col]][item] or "<br>" in df[COLUMNS_TO_PARSE[col]][item]:
                df[COLUMNS_TO_PARSE[col]][item] = "<![CDATA[\n" + df[COLUMNS_TO_PARSE[col]][item] + "\n]]>"
                continue

            i = 1
            while re.search(r"\[\]", if_null(df[COLUMNS_TO_PARSE[col]][item])):
                df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"\[\]", '%' + str(i) + '$s', if_null(df[COLUMNS_TO_PARSE[col]][item]), 1)
                i=i+1
        for item in range(len(df[COLUMNS_TO_PARSE[col]])):
            df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"&", "&amp;", if_null(df[COLUMNS_TO_PARSE[col]][item]))
            df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"\'", "\\'", if_null(df[COLUMNS_TO_PARSE[col]][item]))
            df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"-'", "–'", if_null(df[COLUMNS_TO_PARSE[col]][item]))
            df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"%@", "%s", if_null(df[COLUMNS_TO_PARSE[col]][item]))

    return df

'''Replace all empty bracket [] with ios variable reference: %[#]$@'''
def parse_var_ios(df):
    for col in range(len(COLUMNS_TO_PARSE)):
        for item in range(len(df[COLUMNS_TO_PARSE[col]])):
            i=1
            while re.search(r"\[\]", if_null(df[COLUMNS_TO_PARSE[col]][item])):
                df[COLUMNS_TO_PARSE[col]][item]=re.sub(r"\[\]", '%' + str(i) + '$@', if_null(df[COLUMNS_TO_PARSE[col]][item]), 1)
                i=i+1
    return df

def file_header(filename, timestamp):
    app_name = 'Wallet'
    company_name = 'Canada'
    header = '/* \n  %s \n  %s \n  Created by Python Script on %s. \n Copyright 2019 %s. All rights reserved. \n*/' % (filename, app_name, timestamp, company_name)
    return header

timestamp = time.asctime(time.localtime(time.time()))

def write_ios_lines(df, i, lang_code):
    key = df[COLUMN_KEY][i]
    if "//" in key: #allow comments
        copy_string = key + "\n"
    else :
        copy_string = '"'+str(df[COLUMN_KEY][i])+'" = "'+str(df[lang_code][i])+'";\n'

    return (copy_string)

#TODO clean this
PATH_IOS_BASE = "../Holder/Common/Resources/%s.lproj"

def create_ios_copy_file (df, filename, lang_code):
    #create directory if needed
    path = PATH_IOS_BASE % lang_code
    os.makedirs(path, exist_ok=True)

    with open(filename, mode="w", encoding="utf8", newline='') as f:
        f.write(file_header(filename, timestamp) + '\n\n\n')
        for i in range(len(df)):
            f.write(write_ios_lines(df, i, lang_code))
        print('Created iOS Copy file', filename)

def write_android_lines(df, i, lang_code):
    key = df[COLUMN_KEY][i]
    if "//" in key: #allow comments
        copy_string = '    <!--    ' + key + "-->\n"
    else :
        copy_string = '    <string name="'+str(df[COLUMN_KEY][i])+'">"'+str(df[lang_code][i])+'"</string>\n'

    return (copy_string)

def create_android_copy_file (df, filename, lang_code):
    #create directory if needed
    path = "../app/src/main/res/values-" + lang_code
    os.makedirs(path, exist_ok=True)

    with open(filename, mode="w", encoding="utf8", newline='') as f:
        f.write('<!--\n' + file_header(filename, timestamp) + '\n-->\n\n' + '<resources>\n\n')
        for i in range(len(df)):
            f.write(write_android_lines(df, i, lang_code))
        f.write('</resources>')
        print("Created  Android Copy file", filename)

'''Parses and creates Android Copy files'''
def create_android_copy():
    lang_class = MultiLang()
    # lang_class = LangFile("de")
    content = lang_class.read_file_content()

    COLUMNS_TO_PARSE.extend(lang_class.COLUMNS_TO_PARSE)
    LANGUAGE_SUPPORTED.extend(lang_class.LANGUAGE_SUPPORTED)

    parsed_content = parse_var_android(content)

    # create files for each language parsed
    for lang_code in LANGUAGE_SUPPORTED:
        if lang_code == "en":
            # use default strings for English
            default_file = "../app/src/main/res/values/strings.xml"
            create_android_copy_file(parsed_content, default_file, lang_code)

        full_file_name = PATH_ANDROID_BASE + lang_code + "/strings.xml"
        create_android_copy_file(parsed_content, full_file_name, lang_code)

'''Parses and creates iOS Copy files'''
def create_ios_copy():
    lang_class = MultiLang()
    # lang_class = LangFile("de")
    content = lang_class.read_file_content()

    COLUMNS_TO_PARSE.extend(lang_class.COLUMNS_TO_PARSE)
    LANGUAGE_SUPPORTED.extend(lang_class.LANGUAGE_SUPPORTED)

    parsed_content = parse_var_ios(content)

    # create files for each language parsed
    for lang_code in LANGUAGE_SUPPORTED:
        full_file_name = PATH_IOS_BASE % (lang_code) + "/Localizable.strings"
        create_ios_copy_file(parsed_content, full_file_name, lang_code)

create_android_copy()
# create_ios_copy()

if __name__ == '__main__':
    main()
