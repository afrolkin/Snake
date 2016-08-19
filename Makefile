JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  Board.java \
		  Snake.java 

default: classes

classes: $(CLASSES:.java=.class)

run:
	java Snake 30 2
clean:
	  $(RM) *.class
