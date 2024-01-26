push 1
push 5
add
push 1
lfp
push -2
add
lw
push 1
beq label0
lfp
push -1
add
lw
push 2
add
b label1
label0:
lfp
push -1
add
lw
push 1
add
label1:
print
halt