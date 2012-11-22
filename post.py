'''
Created on Nov 21, 2012

@author: Kyle
'''

class Post(object):
    def __init__(self,args):
        self.id = args[0]
        self.postedBy = args[1]
        self.postBy = args[2]
        self.time = args[3]
        self.message = args[4]
        self.picture = args[5]
    
    def toHTML(self):
        x = "<h2>" + self.postBy + "</h2>\n<p>" + self.message + "</p>\n<p>" + self.time + "</p>\n"
        if self.postedBy != self.postBy:
            x += "<p>Reposted by " + self.postedBy + "</p>\n"
        return x