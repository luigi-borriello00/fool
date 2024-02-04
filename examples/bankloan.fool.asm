push 0
lhp

push function0
lhp
sw
lhp
push 1
add
shp
lhp

push function0
lhp
sw
lhp
push 1
add
shp
push function1
lhp
sw
lhp
push 1
add
shp
lhp

push function2
lhp
sw
lhp
push 1
add
shp
push function3
lhp
sw
lhp
push 1
add
shp
lhp

push function2
lhp
sw
lhp
push 1
add
shp
push function4
lhp
sw
lhp
push 1
add
shp
push 50000
push 40000
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
push 9997
lw
lhp
sw
lhp
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
push 9995
lw
lhp
sw
lhp
lhp
push 1
add
shp
push 20000
push 5000
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
push 9997
lw
lhp
sw
lhp
lhp
push 1
add
shp
lfp
lfp
push -7
add
lw
lfp
push -6
add
lw
stm
ltm
ltm
lw
push 1
add
lw
js
lfp
push -8
add
lw
push -1
beq label14
push 0
b label15
label14:
push 1
label15:
push 1
beq label12
lfp
lfp
push -8
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
b label13
label12:
push 0
label13:
print
halt

function0:
cfp
lra
lfp
lw
push -1
add
lw
stm
sra
pop
sfp
ltm
lra
js

function1:
cfp
lra
lfp
lw
push -2
add
lw
stm
sra
pop
sfp
ltm
lra
js

function2:
cfp
lra
lfp
lw
push -1
add
lw
stm
sra
pop
sfp
ltm
lra
js

function3:
cfp
lra
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 1
add
lw
js
add
push 30000
bleq label2
push 1
b label3
label2:
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 1
add
lw
js
add
push 30000
beq label4
push 0
b label5
label4:
push 1
label5:
label3:
push 1
beq label0
push -1
b label1
label0:
lfp
lfp
lw
push -1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
lhp
sw
lhp
push 1
add
shp
push 9998
lw
lhp
sw
lhp
lhp
push 1
add
shp
label1:
stm
sra
pop
pop
sfp
ltm
lra
js

function4:
cfp
lra
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
push 20000
bleq label8
push 1
b label9
label8:
lfp
lfp
push 1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
push 20000
beq label10
push 0
b label11
label10:
push 1
label11:
label9:
push 1
beq label6
push -1
b label7
label6:
lfp
lfp
lw
push -1
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
lfp
lfp
lw
push -1
add
lw
stm
ltm
ltm
lw
push 1
add
lw
js
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
push 9997
lw
lhp
sw
lhp
lhp
push 1
add
shp
label7:
stm
sra
pop
pop
sfp
ltm
lra
js