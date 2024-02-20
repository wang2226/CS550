#!/usr/bin/env bash
#This shell script is used for run DSC Blockchain Benchmark.
#Author: Haoran Wang
#Date: Dec 2023

loop=$1
for ((i=1; i <= $loop; i++));
do
	./dsc.sh wallet send 1.0 HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF
done

