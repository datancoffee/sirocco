

if [ $# != 2 ]
then
  echo "Invalid number of parameters. Need 2: dir1 dir2"
  exit 1
else
  DIR1=$1
  DIR2=$2
fi

for f in $(find $DIR1 -name '[^.]*.txt'); do
    FILENAME=$(basename $f)
    UPPER_DIR=$(basename $(dirname $f))
    NEWNAME=$DIR2"/"$UPPER_DIR"/"$FILENAME
    #echo $FILENAME $UPPER_DIR $NEWNAME

    opendiff $f $NEWNAME

done


