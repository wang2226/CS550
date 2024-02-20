#!/usr/bin/env bash

# Scratch
sshpass -p password parallel-ssh -i -h node.all -l haoran -A "cd /home/haoran/PA2/; rm -rf *" 2> /dev/null

echo "Copy source code to each node..."
sshpass -p password parallel-scp -h node.all -l haoran -A ./simba/* /home/haoran/PA2 2> /dev/null

echo "Compile the program for each node..."
sshpass -p password parallel-ssh -i -h node.all -l haoran -A "cd /home/haoran/PA2/; make" 2> /dev/null

echo "Create datasets on each node..."
sshpass -p password parallel-ssh -i -h node.client  --timeout 0 -l haoran -A "cd /home/haoran/PA2; ./Create_DataSet.sh" 2> /dev/null

echo "Combine data sets from individual nodes and form a P2P system dataset, and distribute to each node"
echo "Now is for small dataset..."
sshpass -p password parallel-ssh -i -h node.client -l haoran -A "cd /home/haoran/PA2/; ./getfile.sh 10K* DataSet_Small" 2> /dev/null
mkdir ./small_dir

echo "Now is copy small dataset filename from individual nodes..."
sshpass -p password parallel-slurp -h node.client -l haoran -A -L ./small_dir /home/haoran/PA2/DataSet_Small data_set

for dir in "$PWD"/small_dir/*/;
do
  arr=( "$dir"* )
  cat "$dir${arr[@]##*/}" >> All_Node.small
done

sshpass -p password parallel-scp -h node.client -l haoran -A ./All_Node.small /home/haoran/PA2 2> /dev/null

echo "Now is for large dataset..."
sshpass -p password parallel-ssh -i -h node.client -l haoran -A "cd /home/haoran/PA2/; ./getfile.sh 100MB* DataSet_Large" 2> /dev/null
mkdir ./large_dir

echo "Now is copy large dataset filename from individual nodes..."
sshpass -p password parallel-slurp -h node.client -l haoran -A -L ./large_dir /home/haoran/PA2/DataSet_Large data_set

for dir in "$PWD"/large_dir/*/;
do
  arr=( "$dir"* )
  cat "$dir${arr[@]##*/}" >> All_Node.large
done

sshpass -p password parallel-scp -h node.client -l haoran -A ./All_Node.large /home/haoran/PA2 2> /dev/null

# Creating a dataset on a backup node
sshpass -p password parallel-ssh -i -h node.client  --timeout 0 -l haoran -A "cd /home/haoran/PA2; ./HighAvailability.sh" 2> /dev/null

# Clean temporary file
rm -rf All_Node.large All_Node.small small_dir large_dir
sshpass -p password parallel-ssh -i -h node.all -l haoran -A "cd /home/haoran/PA2/; rm -rf DataSet_Small DataSet_Large" 2> /dev/null
