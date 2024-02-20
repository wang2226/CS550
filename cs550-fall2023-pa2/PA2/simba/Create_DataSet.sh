#!/usr/bin/env bash

# Get peer id
peer_id=`hostname`

folder="./shared"
count=0


if [ ! -d "$folder" ];
then
	mkdir $folder
fi

echo "Now is processing 10K file"
# Create 100K: 10KB text files
for serial in {1..100000};
do

    (base64 /dev/urandom | head -c 10240 > $folder/10KB_${peer_id}_$serial.txt) > /dev/null 2>&1 ;
	  ((count=count+1))

	if [ "$count" -eq 10000 ]; then
        echo "The 10K size file is now being generated, please be patient..."
        count=0
    fi
    
done

echo "Now is processing 100MB file"
count=0
# Create 10: 100MB binary files
for serial in {1..10};
do
    (dd if=/dev/zero of=$folder/100MB_${peer_id}_$serial.bin bs=1024 count=0 seek=$[1024*100]) > /dev/null 2>&1 ;
	  ((count=count+1))

	  if [ "$count" -eq 1 ]; then
        echo "The 100MB size file is now being generated, please be patient..."
        count=0
    fi
done

echo "Now is processing 4GB file"
count=0
# Create 1: 4GB binary files
    (dd if=/dev/zero of=$folder/4GB_${peer_id}_1.bin bs=1024 count=0 seek=$[1024*4096]) > /dev/null 2>&1 ;
	  ((count=count+1))

	  if [ "$count" -eq 1 ]; then
        echo "The 4GB size file is now being generated, please be patient..."
        count=0
    fi
    
echo "Create dataset completed. "
