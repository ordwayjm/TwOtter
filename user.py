'''
Created on Nov 21, 2012

@author: Kyle
'''

class User(object):
    '''
    classdocs
    '''


    def __init__(self,args):
        '''
        Constructor
        '''
        self.username = '@' + args[0]
        self.email = args[1]
        self.desc = args[2]
        self.picture = args[3]
        self.name = args[4]
        
    def toString(self):
        return self.name + "\n\t" + self.username + ", " + self.email + ", " + self.desc + ", " + self.picture
    
    def toHTML(self):
        file = open("user_template.html")
        page = ""
        for lines in file:
            page += lines.replace("$u",self.username)
        page = page.replace("$p",self.picture)
        page = page.replace("$n",self.name)
        page = page.replace("$d",self.desc)
        return page