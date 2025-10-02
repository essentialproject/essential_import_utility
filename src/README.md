# Import Utility

## Description
Generates import specifications that can then be imported in to Protege

## Frameworks
* ZK framework - used for UI

## Dependencies

## Build and packaging
Rquires license key for ZK to download ZK artefacts. The key is stored in the root of your `.m2` folder in `settings.xml`. The settings.xml can be downloaded from Sharepoint. Once the key is saved to `.m2/settings.xml`, you can run:

`mvn clean package`