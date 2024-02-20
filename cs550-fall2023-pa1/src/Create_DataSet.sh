#!/bin/bash

# Check if the peer id  is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 Peerid"
    exit 1
fi

peer_id=$1
folder="./shared"
count=0

if [ ! -d "$folder" ];
then
	mkdir $folder
fi

echo "Now is processing 1K file"
# Create 1M: 1KB text files
for serial in {1..10000};
do
# echo "Hello, world!" | dd of=$folder/1KB_${peer_id}_$serial.txt bs=1K count=1;
    dd if=/dev/zero of=$folder/1KB_${peer_id}_$serial.txt bs=1K count=1 > /dev/null 2>&1;
	((count++))

	if [ $count -eq 1000 ]; then
        echo "The 1K size file is now being generated, please be patient..."
        count=0
    fi
    
done

echo "Now is processing 1M file"

count=0
# Create 1K: 1MB text files
for serial in {1..1000};
do
# echo "Hello, world!" | dd of=$folder/1MB_${peer_id}_$serial.txt bs=1M count=1;
    dd if=/dev/zero of=$folder/1MB_${peer_id}_$serial.txt bs=1M count=1 > /dev/null 2>&1;
	((count++))

	if [ $count -eq 100 ]; then
        echo "The 1M size file is now being generated, please be patient..."
        count=0
    fi
done
    
echo "Now is processing 1G file"
count=0
# Create 8: 1GB binary files
for serial in {1..8};
do
    dd if=/dev/zero of=$folder/1GB_${peer_id}_$serial.bin bs=1G count=1 > /dev/null 2>&1;
	((count++))

	if [ $count -eq 1 ]; then
        echo "The 1G size file is now being generated, please be patient..."
        count=0
    fi
done
    
echo "Create dataset completed. "
