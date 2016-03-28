#########################################################################
# File Name: mk.sh
# Author: regan
# mail: regan@thtfit.com
# Created Time: Mon 28 Mar 2016 05:20:43 PM CST
#########################################################################
#!/bin/bash
[ -z $1 ] || ([ $1 = "all" ] && { echo compiles all !!!; touch *; })
source $ANDROID_BUILD_TOP/build/envsetup.sh;rm bin -rf;rm gen -rf;mm -B
