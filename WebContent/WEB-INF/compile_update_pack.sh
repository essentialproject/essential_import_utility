# Copyright ©2012 Enterprise Architecture Solutions Ltd.
# Script to compile an Essential Update Pack
#
# Usage: ./compile_update_pack.sh <OUTPUT_PACK_NAME_WITHOUT_SUFFIX>
#
# Ensure that you have written the update.info file and place your update
# scripts in a folder called 'Scripts'
zip -r $1.eup update.info updatepack.xsd Scripts
