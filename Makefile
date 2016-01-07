CC = javac
FLAGS =  

default: all

Part1:
	$(CC) $(FLAGS) TestFairSem.java

Part2:
	$(CC) $(FLAGS) TestSynchPort.java

Part3:
	$(CC) $(FLAGS) TestPortArray.java

Part4:
	$(CC) $(FLAGS) TestPortWithMailboxA.java TestPortWithMailboxB.java

all: Part1 Part2 Part3 Part4

clean: 
	rm *.class 
	
