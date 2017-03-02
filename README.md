iCloud4j is a Java port of the [PyiCloud](https://github.com/picklepete/pyicloud) library. It provides a means for Java applications to use iCloud web services.
It is licenced under the Apache 2.0 licence.

#Authentication

When the account isn't protected using two-factor authentication, authentication can be down by supplying a simple
username/password pair:

    ICloudService iCloud = new ICloudService("client-id");
    iCloud.authenticate("user@example.com", "password".toCharArray());

#Persistent sessions

An authenticated session can be serialized, stored and re-used. Authenticate requesting an
extendedLogin session and serialize/store the ICloudSession:

    ICloudService iCloud = new ICloudService("client-id");
    iCloud.authenticate("user@example.com", "password".toCharArray(), true);
	 ICloudSession session = iCloud.getSession();

Later the same session can be loaded and re-used without password authentication:

    ICloudService iCloud = new ICloudService(session);

#Devices

A list of devices linked to the iCloud account can be retrieved via the 'FindMyIPhoneService':

    FindMyIPhoneService findMyIPhoneService = new FindMyIPhoneService(iCloudService);
    List<AppleDevice> devices = findMyIPhoneService.getDevices();

#Ubiquity (File Storage)

The iCloud Ubiquity file storage can be browsed by creating a new instance of the 'UbiquityService', and then calling
methods on each node from there:

    UbiquityService ubiquityService = new UbiquityService(iCloudService);
    UbiquityNode rootNode = ubiquityService.getRoot();
    List<UbiquityNode> childrenNode = root.getChildren();

File data can be streamed into an output stream:

    OutputStream outputStream;
    UbiquityNode child = folder.getChildren().get(0);
    if ("file".equals(child.getType()) {
        child.downloadFileData(outputStream);
    }

#Drive (iCloud Drive)

The iCloud Drive contents can be browsed like so:

	DriveService driveService = new DriveService(iCloud);
	DriveNode root = driveService.getRoot();
	for(DriveNode node : root.getChildren()) {
	    ...
	}

#CKDatabase (CloudKit database)

The CloudKit databases can be queried like so:

	String query = "{\"query\":{\"recordType\":\"CPLAssetAndMasterByAssetDateWithoutHiddenOrDeleted\"},\"zoneID\":{\"zoneName\":\"PrimarySync\"},\"desiredKeys\":[\"filenameEnc\"],\"resultsLimit\":20}";

	CKDatabaseService photosDatabase = new CKDatabaseService(iCloud, CKDatabaseService.ENDPOINT_PHOTOS);
	CKResponse response = photosDatabase.query(query);
	for(CKRecord record : response.getRecords()) {
		String fileName = record.getString("filenameEnc");
		...
	}
