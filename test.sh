#!/usr/bin/env bash
#~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar encode wlong /dev/stdout | ~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar decode /dev/stdin /dev/stdout
if [ $# -eq 0 ]; then
	echo No arguments passed, doing the default test
	f="testfiles/3.original";
else
	f="$1";
fi;
codedlen=$(~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar encode $f /dev/stdout | wc -c);
if [ $# -eq 2 ]; then
	echo Hex output of Huffman coded version:
	~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar encode $f /dev/stdout | hd;
	echo Decoded output:
	~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar encode $f /dev/stdout | ~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar decode /dev/stdin /dev/stdout;
else
	diff -s $f <(~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar encode $f /dev/stdout | ~/jdk1.8.0_20/bin/java -jar HuffmanCodingDemo/dist/HuffmanCodingDemo.jar decode /dev/stdin /dev/stdout);
fi;
echo Coded size is $(php -r "echo round($codedlen/filesize('$f')*1000)/10;")% of original

