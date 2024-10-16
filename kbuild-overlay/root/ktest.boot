#!/bin/bash -e

printf "Basic tests run in QEMU ..."
# Download dependent test tools
ls -l
cd /dev
wget https://github.com/lz4/lz4/archive/refs/tags/v1.9.4.tar.gz -O lz4-1.9.4.tar.gz 
printf "Download lz4ok ..."
tar -zxvf lz4-1.9.4.tar.gz
make BUILD_SHARED=no -C lz4-1.9.4 && lz4libdir=$(pwd)/lz4-1.9.4/lib
git clone git://git.kernel.org/pub/scm/linux/kernel/git/xiang/erofs-utils.git -b experimental-tests

# Build experimental-test branch of erofs-utils and run basic-test
cd erofs-utils
./autogen.sh && ./configure
make check

# Prepare test data benchmark
cd ../ && mkdir silesia out
wget -O silesia.zip https://mattmahoney.net/dc/silesia.zip
unzip silesia.zip -d silesia
erofs-utils/mkfs/mkfs.erofs -C4096 silesia.erofs.img silesia 
erofs-utils/fuse/erofsfuse silesia.erofs.img out

# Run stress test
git clone https://github.com/erofs/erofsstress.git
gcc erofsstress/stress.c -o stress
./stress -l100 -p3 ./out
# ./erofs-utils/tests/erofsstress/stress -l100 -p3 ./out
