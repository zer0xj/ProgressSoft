#!/bin/bash

COUNT=100000
GARBAGE=0

declare -a CurrencyCodes=("AED" "AFN" "ALL" "AMD" "ANG" "AOA" "ARS" "AUD" "AWG" "AZN" "BAM" "BBD" "BDT" "BGN" "BHD" "BIF" "BMD" "BND" "BOB" "BRL" "BSD" "BTN" "BWP" "BYN" "BZD" "CAD" "CDF" "CHF" "CLP" "CNY" "COP" "CRC" "CUC" "CUP" "CVE" "CZK" "DJF" "DKK" "DOP" "DZD" "EGP" "ERN" "ETB" "EUR" "FJD" "FKP" "GBP" "GEL" "GGP" "GHS" "GIP" "GMD" "GNF" "GTQ" "GYD" "HKD" "HNL" "HRK" "HTG" "HUF" "IDR" "ILS" "IMP" "INR" "IQD" "IRR" "ISK" "JEP" "JMD" "JOD" "JPY" "KES" "KGS" "KHR" "KMF" "KPW" "KRW" "KWD" "KYD" "KZT" "LAK" "LBP" "LKR" "LRD" "LSL" "LYD" "MAD" "MDL" "MGA" "MKD" "MMK" "MNT" "MOP" "MRO" "MUR" "MVR" "MWK" "MXN" "MYR" "MZN" "NAD" "NGN" "NIO" "NOK" "NPR" "NZD" "OMR" "PAB" "PEN" "PGK" "PHP" "PKR" "PLN" "PYG" "QAR" "RON" "RSD" "RUB" "RWF" "SAR" "SBD" "SCR" "SDG" "SEK" "SGD" "SHP" "SLL" "SOS" "SRD" "STD" "SVC" "SYP" "SZL" "THB" "TJS" "TMT" "TND" "TOP" "TRY" "TTD" "TVD" "TWD" "TZS" "UAH" "UGX" "USD" "UYU" "UZS" "VEF" "VND" "VUV" "WST" "XAF" "XCD" "XDR" "XOF" "XPF" "YER" "ZAR" "ZMW" "ZWD")

function amtGen() {
	echo -n "$((RANDOM%1000000)).$((RANDOM%100))"
}

function currencyCodeGen() {
	echo -n "${CurrencyCodes[$((RANDOM%${#CurrencyCodes[@]}))]}"
}

function dateGen() {
	OUTPUT=$(date -d "$((RANDOM%3+2016))-$((RANDOM%12+1))-$((RANDOM%28+1)) $((RANDOM%23+1)):$((RANDOM%59+1)):$((RANDOM%59+1))" '+%d-%m-%Y %H:%M:%S' 2>/dev/null)
	while [ $? -ne "0" ]; do
		OUTPUT=$(date -d "$((RANDOM%3+2016))-$((RANDOM%12+1))-$((RANDOM%28+1)) $((RANDOM%23+1)):$((RANDOM%59+1)):$((RANDOM%59+1))" '+%d-%m-%Y %H:%M:%S' 2>/dev/null)
	done
	echo -n $OUTPUT
}

function show_help() {
	echo "USAGE: $(basename $0) <ARGUMENTS>"
	echo -e "\t-c\tNumber of rows to generate"
	echo -e "\t-f\tOutput file to write to (default = $COUNT)"
	echo -e "\t-g\tCreate (potentially) garbage data for testing"
	echo -e "\t-h\tShow help"
	echo
}

function useryesno () {
	[ ! -z "$VERBOSE" ] && [ $VERBOSE -gt "0" ] && echo "useryesno(\"$0\", \"$*\")"
	if [ $# -gt "0" ];then read -p "$* (y/N)? "
	else read -e -r -p "(y/N)? "
	fi
	ANSWER="$(echo $REPLY | cut -c1)"
	if [ "$ANSWER" == "y" ] || [[ "$ANSWER" == "Y" ]]; then return 0; fi
	return 1
}

while getopts "c:f:gh" OPTION
do
	case $OPTION in
		c)
			COUNT=$OPTARG
			;;
		f)
			OUTFILE="$OPTARG"
			;;
		g)
			GARBAGE=1
			;;
		h)
			SHOWHELP=1
			;;
	esac
done

[ ! -z "$SHOWHELP" ] && show_help $VERBOSE && exit 1

if [ ! -z "$OUTFILE" ]; then 
	if [ -e "$OUTFILE" ] && [ $(useryesno "Overwrite '$OUTFILE'?") ]; then exit 0
	else
		if [ $GARBAGE -gt "0" ]; then
			for i in $(seq $COUNT); do echo "$((RANDOM%$COUNT+$COUNT)),$(currencyCodeGen),$(currencyCodeGen),\"$(dateGen)\",$(amtGen)" >> "$OUTFILE"
			done
		else
			for i in $(seq $COUNT); do echo "$i,$(currencyCodeGen),$(currencyCodeGen),\"$(dateGen)\",$(amtGen)" >> "$OUTFILE"
			done
		fi
	fi
else
		if [ $GARBAGE -gt "0" ]; then
			for i in $(seq $COUNT); do echo "$((RANDOM%$COUNT)),$(currencyCodeGen),$(currencyCodeGen),\"$(dateGen)\",$(amtGen)"
			done
		else
			for i in $(seq $COUNT); do echo "$i,$(currencyCodeGen),$(currencyCodeGen),\"$(dateGen)\",$(amtGen)"
			done
		fi
fi

exit 0

