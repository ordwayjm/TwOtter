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
        self.username = args[0]
        self.email = args[1]
        self.desc = args[2]
        self.picture = args[3]
        self.name = args[4]
        
    def toString(self):
        return self.name + "\n\t" + self.username + ", " + self.email + ", " + self.desc + ", " + self.picture
    
    def toHTML(self):
        return "<h2>" + self.name + "</h2>\n<h3>" + self.username + "</h3>\n<p>" + self.desc + "</p>\n"