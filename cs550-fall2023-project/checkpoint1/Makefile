JFLAGS = -g -Xdiags:verbose -cp .:bitcoinj-core-0.16.jar:commons-codec-1.16.0.jar:snakeyaml-2.2.jar
JC = javac
JVM= java
MAIN = dsc


.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = ./Transaction.java \
       	  ./Block.java \
          ./BlockChain.java \
          ./Crypt.java \
          ./dsc.java \
          ./Metronome.java \
	      ./Monitor.java \
	      ./Pool.java \
	      ./Validator.java \
	      ./Wallet.java


default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
