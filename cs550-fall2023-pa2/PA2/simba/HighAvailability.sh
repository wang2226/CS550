#!/usr/bin/env bash

# Get peer id
my_id=`hostname`

case "$my_id" in
   "simba01")
    backup_id="simba02"
   ;;
   "simba02")
    backup_id="simba03"
   ;;
   "simba03")
    backup_id="simba04"
   ;;
   "simba04")
    backup_id="simba05"
   ;;
   "simba05")
    backup_id="simba06"
   ;;
   "simba06")
    backup_id="simba07"
   ;;
   "simba07")
    backup_id="simba08"
   ;;
   "simba08")
    backup_id="simba09"
   ;;
   "simba09")
    backup_id="simba10"
   ;;
   "simba10")
    backup_id="simba11"
   ;;
   "simba11")
    backup_id="simba12"
   ;;
   "simba12")
    backup_id="simba13"
   ;;
   "simba13")
    backup_id="simba14"
   ;;
   "simba14")
    backup_id="simba15"
   ;;
   "simba15")
    backup_id="simba16"
   ;;
   "simba16")
    backup_id="simba01"
   ;;
esac


folder="./shared"
count=0

echo "Now is processing 10K file"
# Create 100K: 10KB text files
for serial in {1..100000};
do

    (base64 /dev/urandom | head -c 10240 > $folder/10KB_${backup_id}_$serial.txt) > /dev/null 2>&1 ;
	  ((count=count+1))

	  # if [ "$count" -eq 10000 ]; then
	  if [ "$count" -eq 10 ]; then
        echo "The 10K size file is now being generated, please be patient..."
        count=0
    fi
    
done

echo "Now is processing 100MB file"
count=0
# Create 10: 100MB binary files
for serial in {1..10};
do
    (dd if=/dev/zero of=$folder/100MB_${backup_id}_$serial.bin bs=1MB count=0 seek=100) > /dev/null 2>&1;
	  ((count=count+1))

	  if [ "$count" -eq 1 ]; then
        echo "The 100MB size file is now being generated, please be patient..."
        count=0
    fi
done
    
echo "Create dataset completed. "
