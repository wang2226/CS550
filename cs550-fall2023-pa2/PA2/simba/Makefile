JFLAGS = -g
JC = javac
JVM= java
MAIN = CentralIndexingServer \
       CISClient \
       CISServer \
       GnutellaClient \
       GnutellaServer \
       TestCIS \
       TestStar \
       TestMesh


.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        ./ReadProperties.java \
        ./CentralIndexingServer.java \
        ./CISClient.java \
        ./CISServer.java \
        ./FileDownload.java \
	./SendNeighbor.java \
	./Gnutella.java \
	./GnutellaClient.java \
	./GnutellaServer.java \
	./Test.java \
	./TestCIS.java \
	./TestStar.java \
	./TestMesh.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
