
MAVEN_OPTS="-Xmx2g -Xss4m"

INPUT_DIR='./src/test/resources/in'
OUTPUT_DIR='./src/test/resources/out'

for f in $(find $INPUT_DIR -name '[^.]*.txt'); do
    FILENAME=$(basename $f)
    UPPER_DIR=$(basename $(dirname $f))
    NEWNAME=$OUTPUT_DIR"/"$UPPER_DIR"/"$FILENAME
    #echo $FILENAME $UPPER_DIR $NEWNAME
    mvn exec:java \
        -Dexec.mainClass=sirocco.cmdline.CLI \
        -Dexec.args="Indexer -inputFile \"$f\" -outputFile \"$NEWNAME\""

done


