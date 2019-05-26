.class public Test
.super java/lang/Object
.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method
.method public static main([Ljava/lang/String;)V
.limit locals 1
.limit stack 4
new Test
dup
invokespecial Test/<init>()V
invokevirtual Test/main()I
return
.end method
.method public main()I
.limit locals 1
.limit stack 16
Lmain2_start:
getstatic java/lang/System/out Ljava/io/PrintStream;
bipush 2
bipush 3
iadd
invokevirtual java/io/PrintStream/println(I)V
bipush 2
bipush 3
iadd
Lmain2_end:
ireturn
.end method
