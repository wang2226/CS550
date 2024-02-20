#!/usr/bin/env bash
#This shell script is used for run P2P Benchmark.
#Author: Haoran Wang
#Date: Oct 2023

Node_File_Dir=/home/brucework/PA2
Host_File=host.selected
Central_Indexing_Server=10.108.74.213

choose_node(){
    node=$(shuf -n $1 $Node_File_Dir/node.client)
    >$Host_File
    printf "%s\n" "${node[@]}" > $Host_File
}

HORIZAONTALLINE="============================================================"

clear
echo -e "\n$HORIZAONTALLINE"
echo "                        Run P2P Benchmark"
echo $HORIZAONTALLINE

CENTRAL="Central Indexing Scenario"
STAR="Star Topology Scenario"
MESH="2D-Mesh Topology Scenario"
GAMEOVER="Exit P2P Benchmark"

PS3="Please enter your choice:"
select choice in "${CENTRAL}" "${STAR}" "${MESH}" "${GAMEOVER}"
do
  case $choice in
    $CENTRAL)
      echo -e "\nNow is starting central indexing server, please be patient .... \n"
      ssh haoran@simba17 "java CentralIndexingServer" 2> /dev/null
      sshpass -p password parallel-ssh -h Node.client -l haoran -A -i "cd /home/haoran/PA2;java batchRegister" 2> /dev/null
      echo -e "\nCentral indexing server is up. \n"

      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      choose_node 1
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testCIS case1" 2> /dev/null
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testCIS case2" 2> /dev/null
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testCIS case3" 2> /dev/null
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testCIS case4" 2> /dev/null
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"

      ;;
    $STAR)
      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      choose_node 1
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testStar case1" 2> /dev/null
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testStar case2" 2> /dev/null
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testStar case3" 2> /dev/null
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testStar case4" 2> /dev/null
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"
      ;;
    $MESH)
      echo -e "\nNow is running Query latency experiments, please be patient .... \n"
      choose_node 1
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testMesh case1" 2> /dev/null
      echo -e "\nQuery latency experiments complete. \n"

      echo -e "\nNow is running Query throughput experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testMesh case2" 2> /dev/null
      echo -e "\nQuery throughput experiments complete. \n"

      echo -e "\nNow is running Transfer throughput Small experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testMesh case3" 2> /dev/null
      echo -e "\nTransfer throughput Small experiments complete.\n"

      echo -e "\nNow is running Transfer throughput Large experiments, please be patient .... \n"
      choose_node 9
      sshpass -p password parallel-ssh -h $Host_File -l haoran -A -i "cd /home/haoran/PA2;java testMesh case4" 2> /dev/null
      echo -e "\nTransfer throughput Large experiments, please be patient .... \n"
      ;;
    $GAMEOVER)
      echo -e "\nSee you again!"
      exit 0
      ;;
    *)
      echo -e "\n==> Enter a number between 1 and 4"
      ;;
  esac
done
