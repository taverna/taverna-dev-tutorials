# Taverna language tutorial

## Prerequisites

* Java JDK 7 / OpenJDK 7
* [Maven 3](http://maven.apache.org/)

## RO bundle API

The first tutorial describes the use of the `robundle` API for creating a set of workflow input values to be used with Taverna 3 `executeworkflow.sh` instead of specifying each workflow input on the command line.


### Usage

Ensure this `pom.xml` contains:

```xml
<dependencies>
    <dependency>
       	<groupId>org.purl.wf4ever.robundle</groupId>
        <artifactId>robundle</artifactId>
        <version>0.5.0</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>mygrid-repository</id>
        <name>myGrid Repository</name>
        <url>http://www.mygrid.org.uk/maven/repository</url>
        <releases />
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

To find the latest `<version>` (in case the above has not been updated), see the
list of [robundle releases](https://github.com/wf4ever/robundle/releases).


### Research Object bundles

[robundle](https://github.com/wf4ever/robundle) is an API for building Research Object (RO) Bundles.  RO Bundles are structured ZIP files, described in a JSON manifest, including metadata, provenance and annotations for the constituent resources.

The [RO bundle specification](http://purl.org/wf4ever/ro-bundle) specifies the details of how to read and write the bundle metadata. These slides give a brief overview:

[![Slides](http://image.slidesharecdn.com/2014-04-24-robundles-140424044958-phpapp01/95/slide-1-638.jpg?cb=1398333951)](http://www.slideshare.net/soilandreyes/diving-into-research-objects)

In Taverna, RO Bundles are used for three types of packaging:

 * Workflow input values/lists (`.bundle.zip`)
 * Workflow outputs and details about the workflow run (`.bundle.zip`)
 * Workflow definition as [SCUFL2 Workflow Bundle](http://dev.mygrid.org.uk/wiki/display/developer/Taverna+Workflow+Bundle) (`.wfbundle`)

### robundle API

The [robundle API ](https://github.com/wf4ever/robundle)
implements this specifications, building on the Java 7
[java.nio.Files](http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html)
API and the [Java 7 ZIP file provider](http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html).

The robundle API is the basis for the [Taverna Data Bundles
API](https://github.com/myGrid/databundles), covered later in this
tutorial.

The RO bundle API can process these container formats:

* [RO bundle specification](https://w3id.org/bundle).
* [Adobe UFC](https://wikidocs.adobe.com/wiki/display/PDFNAV/UCF+overview)
* [ePub OCF](http://www.idpf.org/epub3/latest/ocf)
* [Open Document package (ODF)](http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os-part3.html#__RefHeading__752807_826425813)
* [COMBINE Archive (OMEX)](http://co.mbine.org/documents/archive)
* [ZIP](http://www.pkware.com/documents/casestudies/APPNOTE.TXT)



### Using robundle

Follow this tutorial by editing [src/main/java/com/example/tutorial/ROBundle.java](src/main/java/com/example/tutorial/ROBundle.java)

The
[Bundles](https://github.com/wf4ever/robundle/blob/master/src/main/java/org/purl/wf4ever/robundle/Bundles.java)
class is the main entry point for opening and saving bundles. To create a new,
temporary RO bundle, try:

```java
import org.purl.wf4ever.robundle.*;

    // Create a new (temporary) RO bundle
    Bundle bundle = Bundles.createBundle();
```

Using the `getRoot()` method we get a [java.nio.file.Path](http://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html) that represent `/` within the bundle. We can use the [java.nio.Files](http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html) methods to explore and manipulate the bundle.  For instance, to list the content of the root folder:

```java
import java.nio.file.Files;
import java.nio.file.Path;

		for (Path p : Files.newDirectoryStream(bundle.getRoot())) {
			System.out.println(p);
		}
```
The new bundle is empty, except for the file [`/mimetype`](https://w3id.org/bundle#ucf):



For out first example, we'll try to create a bundle with workflow inputs, in
order to use it with the Taverna 3 `executeworkflow.sh` command line.

Workflow inputs are read from the `inputs/` folder of the ZIP archive, with filenames
matching the input port names (no filename extension). For instance,
for the workflow input ports `port1` and `port2`:

 * inputs/port1
 * inputs/port2


```java
		// Get the inputs folder
		Path inputs = bundle.getPath("inputs");
```

The [getPath()](https://github.com/wf4ever/robundle/blob/0.5.0/src/main/java/org/purl/wf4ever/robundle/Bundle.java#L73) method can also take a relative path like `inputs/port1` - but we first need to create the directory.

		Files.createDirectory(inputs);

		Path inputs = bundle.getRoot().resolve("inputs");



		// Get an input port:
		Path in1 = inputs.resolve("in1");

		// Setting a string value for the input port:
		Bundles.setStringValue(in1, "Hello");

		// And retrieving it
		if (Bundles.isValue(in1)) {
			System.out.println(Bundles.getStringValue(in1));
		}

		// Or just use the regular Files methods:
		for (String line : Files.readAllLines(in1, Charset.forName("UTF-8"))) {
			System.out.println(line);
		}

		// Binaries and large files are done through the Files API
		try (OutputStream out = Files.newOutputStream(in1,
				StandardOpenOption.APPEND)) {
			out.write(32);
		}
		// Or Java 7 style
		Path localFile = Files.createTempFile("", ".txt");
		Files.copy(in1, localFile, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("Written to: " + localFile);

		Files.copy(localFile, bundle.getRoot().resolve("out1"));

		// Representing references
		URI ref = URI.create("http://example.com/external.txt");
		Path out3 = bundle.getRoot().resolve("out3");
		System.out.println(Bundles.setReference(out3, ref));
		if (Bundles.isReference(out3)) {
			URI resolved = Bundles.getReference(out3);
			System.out.println(resolved);
		}

		// Saving a bundle:
		Path zip = Files.createTempFile("bundle", ".zip");
		Bundles.closeAndSaveBundle(bundle, zip);
		// NOTE: From now "bundle" and its Path's are CLOSED
		// and can no longer be accessed

		System.out.println("Saved to " + zip);

		// Loading a bundle back from disk
		try (Bundle bundle2 = Bundles.openBundle(zip)) {
			assertEquals(zip, bundle2.getSource());
		}
 ```


Data bundles API
================

API for Taverna Data Bundles

[![Build Status](https://travis-ci.org/taverna/taverna-databundle.svg?branch=master)](https://travis-ci.org/taverna/taverna-databundle)

See [Data bundle requirements](http://dev.mygrid.org.uk/wiki/display/TAVOSGI/2013-02+Data+bundle+requirements)
and [TestDataBundles.java](src/test/java/uk/org/taverna/databundle/TestDataBundles.java)

This API is built on the Java 7 NIO Files and the [RO Bundle API](https://github.com/wf4ever/robundle), which
uses the [Java 7 ZIP file provider](http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html) to generate the Data Bundle.


The class [uk.org.taverna.databundle.DataBundles](src/main/java/uk/org/taverna/databundle/DataBundles.java)
complements the Java 7 [java.nio.Files](http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html)
API with more specific helper methods to work with Data Bundles.


Building
--------
```mvn clean install```

should normally work, given a recent version of [Maven 3](http://maven.apache.org/download.cgi) and
[Java 7 SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html).

[myGrid's Jenkins installation](http://build.mygrid.org.uk/ci/) has automated
builds of both [databundles](http://build.mygrid.org.uk/ci/job/databundles/)
and [robundle](http://build.mygrid.org.uk/ci/job/robundle/), which snapshots are deployed to
[myGrid's snapshot repository](http://build.mygrid.org.uk/maven/snapshot-repository/uk/org/taverna/databundle/databundle/).

Maven should download the latest snapshot of [robundle](https://github.com/wf4ever/robundle)
from [myGrid's snapshot repository](http://build.mygrid.org.uk/maven/snapshot-repository/org/purl/wf4ever/robundle/robundle/),
but in some cases you might have to build robundle locally before building this project.


Example of use
--------------

Example in full is at [uk.org.taverna.databundle.TestExample](src/test/java/uk/org/taverna/databundle/TestExample.java)


Create a new (temporary) data bundle:
```java
        Bundle dataBundle = DataBundles.createBundle();
```

Get the input ports and the port "in1":
```java
        Path inputs = DataBundles.getInputs(dataBundle);
        Path portIn1 = DataBundles.getPort(inputs, "in1");
```

Setting a string value for the input port:
```java
        DataBundles.setStringValue(portIn1, "Hello");
```

And retrieving it:
```java
        if (DataBundles.isValue(portIn1)) {
            System.out.println(DataBundles.getStringValue(portIn1));
        }
```
```
Hello
```


Alternatively, use the regular Files methods:
```java
        for (String line : Files
                .readAllLines(portIn1, Charset.forName("UTF-8"))) {
            System.out.println(line);
        }
```


Binaries and large files are done through the Files API
```java
        try (OutputStream out = Files.newOutputStream(portIn1,
                StandardOpenOption.APPEND)) {
            out.write(32);
        }
```

Or Java 7 style:
```java
        Path localFile = Files.createTempFile("", ".txt");
        Files.copy(portIn1, localFile, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Written to: " + localFile);
```
```
Written to: C:\Users\stain\AppData\Local\Temp\2009921746200670789.txt
```

Either direction of copy works, of course:
```java
        Files.copy(localFile,
                DataBundles.getPort(DataBundles.getOutputs(dataBundle), "out1"));
```


When you get a port, it can become either a value or a list:
```java
        Path port2 = DataBundles.getPort(inputs, "port2");
        DataBundles.createList(port2); // empty list
        List<Path> list = DataBundles.getList(port2);
        assertTrue(list.isEmpty());
```

Adding items sequentially:
```java
        Path item0 = DataBundles.newListItem(port2);
        DataBundles.setStringValue(item0, "item 0");
        DataBundles.setStringValue(DataBundles.newListItem(port2), "item 1");
        DataBundles.setStringValue(DataBundles.newListItem(port2), "item 2");
```

Set and get by explicit position:
```java
        DataBundles.setStringValue(DataBundles.getListItem(port2, 12), "item 12");
        System.out.println(DataBundles.getStringValue(DataBundles.getListItem(port2, 2)));
```
```
item 2
```

The list is sorted numerically (e.g. 2, 5, 10) and will contain nulls for empty slots:
```java
        System.out.println(DataBundles.getList(port2));
```
```
[/inputs/port2/0, /inputs/port2/1, /inputs/port2/2, null, null, null, null, null,
 null, null, null, null, /inputs/port2/12]
```

Ports can be browsed as a map by port name:
```java
        NavigableMap<String, Path> ports = DataBundles.getPorts(inputs);
        System.out.println(ports.keySet());
```
```
[in1, port2]
```

Saving a data bundle:
```java
        Path zip = Files.createTempFile("databundle", ".zip");
        DataBundles.closeAndSaveBundle(dataBundle, zip);
        // NOTE: From now dataBundle and its Path's are CLOSED
        // and can no longer be accessed
        System.out.println("Saved to " + zip);
```
```
Saved to C:\Users\stain\AppData\Local\Temp\databundle6905894602121718151.zip
```

Inspecting the zip:
```java
        if (Desktop.isDesktopSupported()) {
            // Open ZIP file for browsing
            Desktop.getDesktop().open(zip.toFile());
        }
```

Loading a data bundle back from disk:
```java
        try (Bundle dataBundle2 = DataBundles.openBundle(zip)) {
            // Any modifications here will be saved on (here automatic) close
        }
```