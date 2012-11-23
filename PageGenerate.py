import Backend
import sqlite3

def postsToHTML(username, getFeed):
    
    conn = sqlite3.connect("twotter.db")
    c = conn.cursor()
    if (getFeed):
        posts = Backend.getUserFeed(c, username)
    else:
        posts = Backend.getUserPosts(c, username)
    userInfo = Backend.getUserInfo(c, username)
    conn.close()
    postHTML = ""
    for p in posts:
        postHTML += p.toHTML()
    userHTML = userInfo.toHTML()
    
    
    template = open("template.html","r")
    page = ""
    for line in template:
        page += line.replace('@?@?@',postHTML)
    page = page.replace('#@#@#', userHTML)
    return page

print(postsToHTML("KyleRogers",False))