JFLAGS = -g
JC = javac
JVM= java
MAIN = Peer Central_Indexing_Server
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Central_Indexing_Server.java \
        Peer.java \
        getParameter.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
