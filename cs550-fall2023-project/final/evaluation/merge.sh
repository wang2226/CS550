sub_dir=$1
for dir in "$PWD"/"$sub_dir"/submission/*/;
do
  arr=( "$dir"* )
  cat "$dir${arr[@]##*/}" >> "$PWD"/"$sub_dir"/Wallet_Send.log
done

cat "$PWD"/"$sub_dir"/Wallet_Send.log | awk -F ' ' '{print $7 ','  $8 $11}'|awk -F ',' '{print $2,$1}' > "$PWD"/"$sub_dir"/submit
cat "$PWD"/"$sub_dir"/Tx_Confirmed.log | awk -F ' ' '{print $7  ',' $8 $11}'|awk -F ',' '{print $2,$1}' > "$PWD"/"$sub_dir"/confirmed

cd "$PWD"/"$sub_dir"
sort  submit > submit.sort
sort  confirmed > confirmed.sort
join -t" " -1 1 -2 1 -o 1.1 1.3 2.3 submit.sort confirmed.sort > result_"$sub_dir".$$
sort -k2 result_"$sub_dir".$$ > result_"$sub_dir".txt

rm -f Wallet_Send.log submit submit.sort confirmed confirmed.sort result_"$sub_dir".$$
cd "$PWD"

