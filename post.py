'''
Created on Nov 21, 2012

@author: Kyle
'''

class Post(object):
    def __init__(self,args):
        self.id = args[0]
        self.postedBy = '@' + args[1]
        self.postBy = '@' + args[2]
        self.time = args[3]
        self.message = args[4]
        self.picture = args[5]
    
    def toHTML(self):
        file = open("post_template.html")
        ret = ""
        for lines in file:
            ret += lines.replace('$up',str(self.postBy))
        ret = ret.replace('$p',self.picture)
        ret = ret.replace('$t',str(self.time))
        ret = ret.replace('$upd','' if self.postBy == "Reposted by " + self.postedBy else self.postedBy)
        ret = ret.replace('$m',self.message)
        return ret