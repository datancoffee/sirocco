
if [ $# != 4 ]
then
  echo "Invalid number of parameters. Need 4: IndexingType ParsingType ItemIDColumnIdx TextColumnIdx"
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

  ITEMIDCOLUMNIDX=$3
  TEXTCOLUMNIDX=$4

fi

MAVEN_OPTS="-Xmx2g -Xss4m"

INPUT_DIR='./src/test/resources/in'
OUTPUT_DIR='./src/test/resources/out'

echo "Processing CSV files ..."
for f in $(find $INPUT_DIR -name '[^.]*.csv'); do
    FILENAME=$(basename $f)
    UPPER_DIR=$(basename $(dirname $f))
    NEWNAME=$OUTPUT_DIR"/"$UPPER_DIR"/"$FILENAME
    #echo $FILENAME $UPPER_DIR $NEWNAME
    mvn exec:java \
        -Dexec.mainClass=sirocco.cmdline.CLI \
        -Dexec.args="Indexer -inputFile \"$f\" -outputFile \"$NEWNAME\" \
                    -indexingType \"$INDEXINGTYPE\" -parsingType \"$PARSINGTYPE\" \
                    -readAsCSV true -collectionItemIdIdx \"$ITEMIDCOLUMNIDX\" -textColumnIdx \"$TEXTCOLUMNIDX\""
done

