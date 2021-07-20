
set -eux

REGION="${1:-s3-us-west-2}"
BUCKET="${2:-artifacts}"
PLUGIN_NAME=${3:-aws-lambda-deployment-plugin-spinnaker}

TEMP_PLUGIN_FILE="plugins-temp.json"
PLUGIN_FILE_NAME="plugins.json"
PLUGIN_PATH="./build/distributions"
PLUGIN_FILE="$PLUGIN_PATH/$PLUGIN_FILE_NAME"
TEMP_FILE="temp.json"

increment_version() {
  local delimiter=.
  local array=($(echo "$1" | tr $delimiter '\n'))
  array[$2]=$((array[$2]+1))
  echo $(local IFS=$delimiter ; echo "${array[*]}")
}

tag=$(git describe --tags --abbrev=0)
version=$(increment_version "${tag}" 2)

# build the plugin

[ -d "$PLUGIN_PATH" ] || (echo "cannot find $PLUGIN_PATH. script needs to run in root plugin dir" && exit 1)

rm -rf $PLUGIN_PATH/*
rm -rf $TEMP_FILE,$TEMP_PLUGIN_FILE

./gradlew releaseBundle

# update plugins.json
cat $PLUGIN_PATH/plugin-info.json | jq -r '.releases |= map( . + '{\"url\":\"https://$BUCKET.s3.$REGION.amazonaws.com/plugins/${PLUGIN_NAME}/${PLUGIN_NAME}-${version}.zip\"}')' > $TEMP_PLUGIN_FILE
echo [] >> $PLUGIN_FILE;
jq 'reduce inputs as $i (.; .[0] = $i)' "$PLUGIN_FILE" "$TEMP_PLUGIN_FILE" > "$TEMP_FILE";
mv $TEMP_FILE $PLUGIN_FILE


# upload artifacts to s3
aws s3 cp $PLUGIN_FILE s3://$BUCKET/plugins/${PLUGIN_NAME}/$PLUGIN_FILE_NAME --acl public-read
aws s3 cp $PLUGIN_PATH/${PLUGIN_NAME}* s3://$BUCKET/plugins/${PLUGIN_NAME}/${PLUGIN_NAME}-${version}.zip --acl public-read

echo "This will be usable on... "
cat<<EOF

spinnaker:
  extensibility:
    plugins:
      aws.PluginNameCamelCase:
        id: aws.PluginNameCamelCase
        enabled: true
        version: ${version}
    repositories:
      PluginNameCamelCase:
        id: PluginNameCamelCase
        url: https://$BUCKET.s3.$REGION.amazonaws.com/plugins/${PLUGIN_NAME}/plugins.json
EOF


# nuke temp files
rm -rf $TEMP_FILE
rm -rf $TEMP_PLUGIN_FILE
