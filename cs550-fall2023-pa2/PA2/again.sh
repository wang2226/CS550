sshpass -p password parallel-ssh -i -h node.all -l haoran -A "cd /home/haoran/PA2/; make clean; rm -rf *.java"
sshpass -p password parallel-scp -h node.all -l haoran -A ./simba/*.java /home/haoran/PA2
sshpass -p password parallel-ssh -i -h node.all -l haoran -A "cd /home/haoran/PA2/; make"
