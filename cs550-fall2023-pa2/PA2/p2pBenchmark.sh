#!/usr/bin/env bash
#This shell script is used for run P2P Benchmark.
#Author: Haoran Wang
#Date: Oct 2023

Central_Indexing_Server=10.108.74.213
Sleep_Time=2400

runCentral(){
      echo -e "\nNow is starting central indexing server, please be patient .... \n"
      sshpass -p password parallel-ssh -H 10.108.74.213 -l haoran --par 50 -A -i --timeout 0 "cd /home/haoran/PA2; java CentralIndexingServer"
      echo -e "\nCentral indexing server is up. \n"

      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_1.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java CISServer" 2> /dev/null
      sshpass -p password parallel-ssh -h node_1.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestCIS case1" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java CISServer" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestCIS case2" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java CISServer" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestCIS case3" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java CISServer" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestCIS case4" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.all -v -l haoran -A java
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"

}      

runStar(){
      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_1.server -l haoran -A -i --par 50 --timeout 0  "cd /home/haoran/PA2; java GnutellaServer Star" 2> /dev/null
      sshpass -p password parallel-ssh -h node_1.client -l haoran -A -i --par 50 --timeout 0  "cd /home/haoran/PA2; java TestStar case1" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Star" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestStar case2" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Star" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestStar case3" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Star" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestStar case4" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"
}

runMesh(){
      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_1.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Mesh" 2> /dev/null
      sshpass -p password parallel-ssh -h node_1.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestMesh case1" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Mesh" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestMesh case2" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Mesh" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestMesh case3" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      sshpass -p password parallel-ssh -h node_9.server -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java GnutellaServer Mesh" 2> /dev/null
      sshpass -p password parallel-ssh -h node_9.client -l haoran -A -i --par 50 --timeout 0 "cd /home/haoran/PA2; java TestMesh case4" 2> /dev/null
      sleep $Sleep_Time
      sshpass -p password parallel-nuke -h node.client -v -l haoran -A java
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"
}

if [ $# = 0 ]
    then
    	runCentral
    	runStar
    	runMesh
elif [ $1 = "Central" ]
    then
      	runCentral
elif [ $1 = "Star" ]
    then
      	runStar
elif [ $1 = "Mesh" ]
    then
      	runMesh
fi
