import numpy as np
from sympy import solve,solveset,Eq,roots,symbols,sympify,plot_implicit
from sympy.plotting import plot,plot3d

def main(lhs,rhs):
    equation = Eq(lhs,int(rhs))
    p=plot_implicit(equation, (x, -10, 10), (y, -10, 10), show=True)  
    buffer = io.BytesIO()
    plt.savefig(buffer, format='PNG')
    buffer.seek(0)
    byte_string = base64.b64encode(buffer.getvalue()).decode()
    return ""+str(byte_string,utf-8)