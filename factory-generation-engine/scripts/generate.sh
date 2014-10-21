#!/bin/bash

CODENVY_HOME=`pwd`
CODENVY_CLI=~/codenvy/cli/codenvy
CODENVY_FACTORY=$CODENVY_HOME/java/factories.sh
[ -z "${FACTORIES_CONFIG}" ]  && FACTORIES_CONFIG=~/.codenvy_factorygen

if [ -s $FACTORIES_CONFIG ]; then
    echo "Found config file"
  else
    echo "need config file $FACTORIES_CONFIG with content like:"
    echo ""
    echo "FACTORYMAIL=factories@codenvy.com"
    echo "FACTORYPASS=some_pass"
    echo "S3CONFIG=~/.s3cfg-stg-factories"
    echo "S3BUCKET=s3://factories.codenvy-stg.com"
    exit 1
fi


. $FACTORIES_CONFIG 

# Generate an updated Codenvy token
$CODENVY_CLI  auth --user $FACTORYMAIL --password $FACTORYPASS --newToken --save

mkdir -p $CODENVY_HOME/generated
rm -rf $CODENVY_HOME/jsons
mkdir -p $CODENVY_HOME/jsons

# sync already uploaded json files to lacal folder
#s3cmd -c $S3CONFIG sync $S3BUCKET/* $CODENVY_HOME/jsons
cd $CODENVY_HOME/jsons
s3cmd -c $S3CONFIG ls $S3BUCKET | grep ".json" | awk '{ print $4 }' | xargs -n 1 s3cmd -c $S3CONFIG get
agit=( $CODENVY_HOME/*.json )

# Each JSON file, generate a .endpoint file using 'codenvy remote factory:create' command.
# Take the new .endpoint file and execute 'factories.bat' command.
# Do not pass in full file name to factories.bat.  Only the initial half, ie. 'spring' if 'spring.endpoint'
# TODO: Only run this loop on .JSON files that are newer than any generated HTML or .factory files.
cd $CODENVY_HOME

# sync images to S3 BEFORE generating
s3cmd -c $S3CONFIG sync  $CODENVY_HOME/images $S3BUCKET

for A in ${agit[@]}
do 
# compare sha1 sum of s3 and git *.json
echo $A
SHA1G=`openssl sha1 $A | awk '{print $2}'`
echo $SHA1G
# basename
BN=${A##*/}
SHA1S3=`openssl sha1 $CODENVY_HOME/jsons/$BN | awk '{print $2}'`
echo $SHA1S3
# baseneme suffixless
BNSL=${BN%.json}

# generate factory and endpoint, html files if SHA1 sum differ
if [[ $SHA1G != $SHA1S3 ]]; then
    $CODENVY_CLI remote factory:create --in $BNSL.json --encoded --rel self > $BNSL.endpoint
    # check if factory created OK
    if grep -q http "$BNSL.endpoint" ; then
	$CODENVY_FACTORY $BNSL
	mv $BNSL.endpoint $CODENVY_HOME/generated/
	mv $BNSL.factory $CODENVY_HOME/generated/
	mv $BNSL.html $CODENVY_HOME/generated/
    else 
	echo "$BNSL.json NOT OK, exiting"
	exit 2
    fi
fi

done

# sync to S3 json and generated content
s3cmd -c $S3CONFIG sync  $CODENVY_HOME/generated $S3BUCKET
s3cmd -c $S3CONFIG sync  $CODENVY_HOME/*.json $S3BUCKET/
