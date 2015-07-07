#!/usr/bin/python

import sys
import re

old_title = ""
total = 0
dict = {'01' : 0, '02' : 0, '03' : 0, '04' : 0, '05' : 0, '06' : 0, '07' : 0, '08' : 0, '09' : 0, 
        '10' : 0, '11' : 0, '12' : 0, '13' : 0, '14' : 0, '15' : 0, '16' : 0, '17' : 0, '18' : 0, 
        '19' : 0, '20' : 0, '21' : 0, '22' : 0, '23' : 0, '24' : 0, '25' : 0, '26' : 0, '27' : 0, 
        '28' : 0, '29' : 0, '30' : 0, '31' : 0}

for line in sys.stdin:
#f = open("test", "r")
#test2 = open("test2", "w")
#while True:
#line = f.readline()

    if line:
        a = line.split("\t")
        name = a[0]
        second_part = a[1]
        second_part_split = second_part.split(":")
        date = second_part_split[0]
        view = second_part_split[1]
        if(old_title == ""):
            old_title = a[0]
            total = total + int(view)
            dict[date] = dict[date] + int(view)
        elif(old_title == name):
            total = int(total) + int(view)
            dict[date] = dict[date] + int(view)
        else:
            if(total > 0):
                print(str(total) + "\t" + str(old_title)
                       + "\t" + "01"+ ":" + str(dict["01"]) + "\t" + "02"+ ":" + str(dict["02"])
                       + "\t" + "03"+ ":" + str(dict["03"]) + "\t" + "04"+ ":" + str(dict["04"])
                       + "\t" + "05"+ ":" + str(dict["05"]) + "\t" + "06"+ ":" + str(dict["06"])
                       + "\t" + "07"+ ":" + str(dict["07"]) + "\t" + "08"+ ":" + str(dict["08"])
                       + "\t" + "09"+ ":" + str(dict["09"]) + "\t" + "10"+ ":" + str(dict["10"])
                       + "\t" + "11"+ ":" + str(dict["11"]) + "\t" + "12"+ ":" + str(dict["12"])
                       + "\t" + "13"+ ":" + str(dict["13"]) + "\t" + "14"+ ":" + str(dict["14"])
                       + "\t" + "15"+ ":" + str(dict["15"]) + "\t" + "16"+ ":" + str(dict["16"])
                       + "\t" + "17"+ ":" + str(dict["17"]) + "\t" + "18"+ ":" + str(dict["18"])
                       + "\t" + "19"+ ":" + str(dict["19"]) + "\t" + "20"+ ":" + str(dict["20"])
                       + "\t" + "21"+ ":" + str(dict["21"]) + "\t" + "22"+ ":" + str(dict["22"])
                       + "\t" + "23"+ ":" + str(dict["23"]) + "\t" + "24"+ ":" + str(dict["24"])
                       + "\t" + "25"+ ":" + str(dict["25"]) + "\t" + "26"+ ":" + str(dict["26"])
                       + "\t" + "27"+ ":" + str(dict["27"]) + "\t" + "28"+ ":" + str(dict["28"])
                       + "\t" + "29"+ ":" + str(dict["29"]) + "\t" + "30"+ ":" + str(dict["30"])
                       + "\t" + "31"+ ":" + str(dict["31"]) + "\n")
            old_title = name
            for key in dict.keys():
                dict[key] = 0
            dict[date] = dict[date] + int(view)
            total = view
    else:
        break
if(total > 0):
                print(str(total) + "\t" + str(old_title)
                       + "\t" + "01"+ ":" + str(dict["01"]) + "\t" + "02"+ ":" + str(dict["02"])
                       + "\t" + "03"+ ":" + str(dict["03"]) + "\t" + "04"+ ":" + str(dict["04"])
                       + "\t" + "05"+ ":" + str(dict["05"]) + "\t" + "06"+ ":" + str(dict["06"])
                       + "\t" + "07"+ ":" + str(dict["07"]) + "\t" + "08"+ ":" + str(dict["08"])
                       + "\t" + "09"+ ":" + str(dict["09"]) + "\t" + "10"+ ":" + str(dict["10"])
                       + "\t" + "11"+ ":" + str(dict["11"]) + "\t" + "12"+ ":" + str(dict["12"])
                       + "\t" + "13"+ ":" + str(dict["13"]) + "\t" + "14"+ ":" + str(dict["14"])
                       + "\t" + "15"+ ":" + str(dict["15"]) + "\t" + "16"+ ":" + str(dict["16"])
                       + "\t" + "17"+ ":" + str(dict["17"]) + "\t" + "18"+ ":" + str(dict["18"])
                       + "\t" + "19"+ ":" + str(dict["19"]) + "\t" + "20"+ ":" + str(dict["20"])
                       + "\t" + "21"+ ":" + str(dict["21"]) + "\t" + "22"+ ":" + str(dict["22"])
                       + "\t" + "23"+ ":" + str(dict["23"]) + "\t" + "24"+ ":" + str(dict["24"])
                       + "\t" + "25"+ ":" + str(dict["25"]) + "\t" + "26"+ ":" + str(dict["26"])
                       + "\t" + "27"+ ":" + str(dict["27"]) + "\t" + "28"+ ":" + str(dict["28"])
                       + "\t" + "29"+ ":" + str(dict["29"]) + "\t" + "30"+ ":" + str(dict["30"])
                       + "\t" + "31"+ ":" + str(dict["31"]) + "\n")

            
            
        