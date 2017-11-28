.global bubble

bubble:
push {lr} @push the return address
@r0 is the array pointer
@r1 is the total number of the array, r1 = n
@r2 = i outer loop
@r3 = j inner loop

mov r2, #0 @r2 = 0, i = 0

iloop:
add r2, r2, #1 @i++, i = 1; i < n
cmp r2, r1
beq exit @if i = n, exit
mov r3, #0 @j = 0
sub r5, r1, r2 @r5 = n - i

jloop:
cmp r3, r5
beq iloop @if j = n - i, back to the i-loop

mov r10, #4 @r10 = 4, pointer moves 4 every time
mul r6, r3, r10 @j*4 is the pointer of the number we will use
add r6, r6, r0 @make r6 be the pointer
ldr r7, [r6] @r7  = the number at j*4
add r9, r6, #4 @r9 is the pointer of the next number
ldr r8, [r9] @r8 = the number after
cmp r7, r8
bgt swap @if r7 > r8, swap
add r3, r3, #1 @j++
b jloop @continue j loop


swap: 	@do the swap
str r8, [r6] @store r8 in the before pointer
str r7, [r9] @store r7 in the later pointer
add r3, r3, #1 @j++
b jloop @back to j loop 

exit:
pop {lr}
bx lr