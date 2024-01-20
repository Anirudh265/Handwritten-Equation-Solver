import numpy as np
from sympy import solve,solveset,Eq,roots,symbols,sympify,plot_implicit
from sympy.plotting import plot,plot3d
import io
import base64
import matplotlib.pyplot as plt

def main(lhs,flag):

    eq1=lhs[1:]
    eq=eq1.split('=')
    x, y = symbols('x y')
    if(len(eq)==1):
        eq2=0
    else:
        eq2=int(eq[1])
    eq1=sympify(eq[0])
    s=eq1-eq2
    equation = Eq(eq1,eq2)
    if(flag==1):
        p=plot3d(s, (x, -10, 10), (y, -10, 10), show=True)
    else:
        p=plot(s, (x, -10, 10), (y, -10, 10), show=True)
    buffer = io.BytesIO()
    plt.savefig(buffer, format='PNG')
    buffer.seek(0)
    byte_string = base64.b64encode(buffer.getvalue())
    return ""+str(byte_string,'utf-8')

