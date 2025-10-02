# Run this command in this folder
# Update the XJC_PATH variable to reflect the location on YOUR machine
# where the JAXB Libs are deployed
XJC_PATH="/Users/jonathan/Development/EAS/JavaLibs/jaxb-ri-20110601/bin"
ECLIPSE_PATH="/Users/jonathan/Development/EAS/New Eclipse Workspace/EssentialImportUtility"
cp "$ECLIPSE_PATH"/WebContent/schema/*.* /Users/jonathan/Temp
#cd WebContent/schema
cd /Users/jonathan/Temp
$XJC_PATH/xjc.sh essential_import_utility.xsd -d generated-xml-src
cd generated-xml-src
cp -R * "$ECLIPSE_PATH"/generated-xml-src 
rm -r org
cd "$ECLIPSE_PATH"
echo "Java classes generated in folder ./generated-xml-src. Copy/move these into the src directory once you are happy with them"
