
ALL: s

# 直接编译,运行服务器
Server.class:Server.java Common.java
	javac $^

s:Server.class
	java Server


# 直接编译,运行客户端
Client.class:Client.java Common.java
	javac $^

c:Client.class
	java Client

clean:
	rm -rf *.class 
