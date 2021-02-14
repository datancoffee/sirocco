
if [ $# != 3 ]
then
  echo "Invalid number of parameters. Need 3: IndexingType ParsingType ContentType"
  exit 1
else
  if [ "$1" != "FULLINDEX" ] && [ "$1" != "TOPSENTIMENTS" ]
  then
    echo "Invalid 1st parameter value. Use one of {FULLINDEX | TOPSENTIMENTS}"
    exit 1
  else
    INDEXINGTYPE=$1
  fi

  if [ "$2" != "DEEP" ] && [ "$2" != "SHALLOW" ] && [ "$2" != "DEPENDENCY" ]
  then
    echo "Invalid 2nd parameter value. Use one of {DEEP | SHALLOW | DEPENDENCY}"
    exit 1
  else
    PARSINGTYPE=$2
  fi

  if [ "$3" != "ARTICLE" ] && [ "$3" != "SHORTTEXT" ]
  then
    echo "Invalid 3nd parameter value. Use one of {ARTICLE | SHORTTEXT}"
    exit 1
  else
    CONTENTTYPE=$3
  fi

fi

MAVEN_OPTS="-Xmx2g -Xss4m"

INPUT_DIR='./src/test/resources/in'
OUTPUT_DIR='./src/test/resources/out'

echo "Processing txt files in bag-of-properties format ..."
for f in $(find $INPUT_DIR -name '[^.]*.txt'); do
    FILENAME=$(basename $f)
    UPPER_DIR=$(basename $(dirname $f))
    NEWNAME=$OUTPUT_DIR"/"$UPPER_DIR"/"$FILENAME
    #echo $FILENAME $UPPER_DIR $NEWNAME
    mvn exec:java \
        -Dexec.mainClass=sirocco.cmdline.CLI \
        -Dexec.args="Indexer -inputFile \"$f\" -outputFile \"$NEWNAME\" -indexingType \"$INDEXINGTYPE\" -parsingType \"$PARSINGTYPE\" -contentType \"$CONTENTTYPE\" "
done

