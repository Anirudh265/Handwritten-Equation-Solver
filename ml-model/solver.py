#Mapping labels to their digits and operations
import numpy as np
dic={0:0,1:1,2:2,3:3,4:4,5:5,6:6,7:7,8:8,9:9,10:'+',11:'-',12:'=',13:'x'}
#print equation 
def printEq(x):
    flag=0
    for i in range(len(x)):
        if flag==1:
            flag=0
            continue
        if i!=len(x)-1 and dic[x[i]]=='x'and x[i+1]!=10 and x[i+1]!=11 and x[i+1]!=12:
            print('x^'+str(dic[x[i+1]]), end=" ")
            flag=1
        else :
            print(dic[x[i]], end=" ")
# Find degree of polynomial
def highdegree(x):
    Max=1
    for i in range(len(x)):
        if i!=len(x)-1 and x[i]==13 and x[i+1]!=10 and x[i+1]!=11 and x[i+1]!=12:
            Max=max(Max,x[i+1])
    return Max        
#Solve the equation
def solution(x):
    printEq(x)
    print("")
    degree=highdegree(x)
    eq=np.ones(degree+1)
    #equality index
    ind=0
    for i in x:
        if i==12:
            break
        ind=ind+1   
    x=x[0:ind]+x[ind+1:len(x)]
    def ret(i):
        if i>ind:
            if x[i-1]==10:
                return -1*x[i]
        if i==ind:
            return -1*x[i]
        if i<ind and i>0:
            if x[i-1]==11:
                return -1*x[i]
        else :
            return x[i]
    for k in range(degree+1):
        #find constant term
        if k==0 :
            for i in range(len(x)):
                #x not infront , x not in back
                if i==0:
                    if x[i+1]!=13 and x[i]<10:
                        eq[degree-k]=ret(i)
                        break
                if i==len(x)-1:
                    eq[degree-k]=ret(i)
                    break
                if x[i+1]!=13 and x[i-1]!=13 and x[i]<10:
                    eq[degree-k]=ret(i)
                    break
        if k==1:
            for i in range(len(x)):
                if i==len(x)-2:
                    eq[degree-k]=ret(i) 
                if(i<len(x)-2):    
                    if x[i+1]==13 and x[i+2]>9 and x[i]<9:
                        eq[degree-k]=ret(i) 
                        break
                else:
                    eq[degree-k]=ret(i-1)        
        if k>1:
            for i in range(2,len(x)):
                if x[i]==k and x[i-1]==13:
                    eq[degree-k]=ret(i-2)
                    break
    #Print and return ans
    print(np.roots(eq))
    return np.roots(eq)