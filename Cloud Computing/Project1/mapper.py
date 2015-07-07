#!/usr/bin/python
     
import sys
import os

for line in sys.stdin:
#f = open("pagecounts-20130601-000000", "r")
#while True:
#line = f.readline()
    if line:
        a=line.split()
        if (a[0] != "en"):
            continue
        if(a[1].startswith("Media") or a[1].startswith("Special") or a[1].startswith("Talk")
           or a[1].startswith("User") or a[1].startswith("User_talk") or a[1].startswith("Project")
           or a[1].startswith("Project_talk") or a[1].startswith("File") or a[1].startswith("File_talk")
           or a[1].startswith("MediaWiki") or a[1].startswith("MediaWiki_talk") or a[1].startswith("Template") 
           or a[1].startswith("Template_talk") or a[1].startswith("Help") or a[1].startswith("Help_talk") 
           or a[1].startswith("Category") or a[1].startswith("Category_talk") or a[1].startswith("Portal") 
           or a[1].startswith("Wikipedia") or a[1].startswith("Wikipedia_talk")):
            continue
        if(a[1].startswith("a") or a[1].startswith("b") or a[1].startswith("c") or a[1].startswith("d")
           or a[1].startswith("e") or a[1].startswith("f") or a[1].startswith("g") or a[1].startswith("h")
           or a[1].startswith("i") or a[1].startswith("j") or a[1].startswith("k") or a[1].startswith("l")
           or a[1].startswith("m") or a[1].startswith("n") or a[1].startswith("o") or a[1].startswith("p")
           or a[1].startswith("q") or a[1].startswith("r") or a[1].startswith("s") or a[1].startswith("t")
           or a[1].startswith("u") or a[1].startswith("v") or a[1].startswith("w") or a[1].startswith("x")
           or a[1].startswith("y") or a[1].startswith("z")):
            continue
        if(a[1].endswith(".jpg") or a[1].endswith(".gif") or a[1].endswith(".png") or a[1].endswith(".JPG")
           or a[1].endswith(".GIF") or a[1].endswith(".PNG") or a[1].endswith(".txt") or a[1].endswith(".ico")):
            continue
        if(a[1]=="404_error/" or a[1]=="Main_Page" or a[1]=="Hypertext_Transfer_Protocol" 
           or a[1]=="Favicon.ico" or a[1]=="Search"):
            continue
        else:
            new_line = line
            date = 0
            #file_name = os.environ["map.input.file"]
            file_name = "pagecounts-20130601-000000"
            file_name_split = file_name.split("-")
            date = file_name_split[1][-2:]
            process_line = new_line.split()
            first = process_line[1]
            second = process_line[2]
            result = first + "\t" + date + ":" + second + '\n'
            print(result)
    else:
        break