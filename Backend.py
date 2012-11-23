'''
Created on Nov 21, 2012

@author: Kyle
'''
import sqlite3
import post
import user


def getUserPosts(c,username):
    c.execute("SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture FROM USER JOIN POSTED ON USER.username=POST.username JOIN POST ON POST.postid=POSTED.postid WHERE POSTED.username=? ORDER BY timestamp DESC",[username])
    ans = []
    res = c.fetchall()
    for aRes in res:
        ans.append(post.Post(aRes))
    return ans
   
def getUserFeed(c,username):
    c.execute("SELECT DISTINCT(POSTED.postID),POSTED.username,POST.username,POST.timestamp,POST.message,USER.picture FROM POST JOIN POSTED ON POST.postid=POSTED.postid JOIN FOLLOWING ON FOLLOWING.followee=POSTED.username JOIN USER ON POST.username=USER.username WHERE FOLLOWING.follower=? ORDER BY timestamp DESC",[username])
    ans = []
    res = c.fetchall()
    for aRes in res:
        ans.append(post.Post(aRes))
    return ans

def getUserInfo(c,username):
    c.execute("SELECT username,email,description,picture,name FROM USER WHERE username=?",[username])
    return user.User(c.fetchone())
    