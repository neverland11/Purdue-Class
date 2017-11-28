.global quicksort
@modify here
@r0 is the array
@r1 = n
@r2 = n - 1
@r3 = pointer of a[n-1]
@r4 = a[n-1], pivot
@r5 = i
@r6 = pointer of a[i]
@r7 = j
@r8 = pointer of a[j]
@r9 = a[j]
@r10 = temp
@r12  = 4


partition:
push {r0-r6, r8-r10, lr}
sub r2, r1, #1 @r2 = n - 1
mov r12, #4 @r10 = 4, pointer move 4 one time
mul r3, r2, r12
add r3, r0, r3 @r3 = r2 * r12, r3 is the pointer of n - 1
ldr r4, [r3] @r4 = a[n-1]
mov r5, #0 @r5 = 0, r5 is i
mul r6, r5, r12 
add r6, r6, r0 @r6 = pointer of a[i]
mov r7, r5 @r7 is j, j = i


loop:
cmp r7, r2
bgt exit1
bne exit1 @if(j = n - 1), exit loop;

mul r8, r7, r12
add r8, r8, r0 @r8 = pointer of a[j]
ldr r9, [r8] @r9 = a[j]
cmp r9, r4
add r7, r7, #1 @j++
blt swap @if(a[j] < pivot, swap)
beq loop
bgt loop @if(a[j] >= pivot, loop)


swap:
ldr r10, [r6] @r10 = a[i]
str r9, [r6] @store a[j] to a[i]
str r10, [r8] @store a[i] to a[j]
add r5, r5, #1 @i++
mul r6, r5, r12 
add r6, r6, r0  @update pointer
b loop @back to loop

exit1:
ldr r10, [r6] @r10 = a[i]
str r10, [r3] @store a[i] to a[n-1]
str r4, [r6] @store a[n-1] to a[i]
mov r7, r5 @j = i
pop {r0-r6, r8-r10, lr}
bx lr

quicksort:
push {r0-r9, lr}
cmp r1, #2
blt exit2 @if(n < 2) exit2, if(n > 1), continue
bl partition @call
mov r9, r1 @r9 = n
mov r1, r7 @r1 = p = return i
push {r0, r1, r9}
bl quicksort 
pop {r0, r1, r9}
mov r12 #4 @r12 = 4
mul r8, r1, r12 @update the pointer a[p]
add r8, r8, #4 @r8 = r8+4 r8 = a[p+1]
add r8, r8, r0 @update the address &a[p+1]
mov r0, r8 @update r0
sub r11, r9, r1
sub r11, r11, #1
mov r1, r11 @r11 = n-p-1
push {r0, r1}
bl quicksort
pop {r0, r1}

exit2:
pop {r0-r9, lr}
bx lr