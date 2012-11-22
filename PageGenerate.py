import Backend
import sqlite3

username = 'JohnSmith'
conn = sqlite3.connect("twotter.db")
c = conn.cursor()

posts = Backend.getUserFeed(c, username)
postHTML = ""
for p in posts:
    postHTML += p.toHTML()
user = Backend.getUserInfo(c, username)
userHTML = user.toHTML()


template = open("template.html","r")
page = ""
for line in template:
    page += line.replace('@?@?@',postHTML)
page = page.replace('#@#@#', userHTML)
print(page)