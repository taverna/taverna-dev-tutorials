package com.example.tutorial;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.purl.wf4ever.robundle.Bundle;
import org.purl.wf4ever.robundle.Bundles;

public class ROBundle {

	public static void main(String[] args) throws IOException {
		// Create a new (temporary) RO bundle
		Bundle bundle = Bundles.createBundle();

		for (Path p : Files.newDirectoryStream(bundle.getRoot())) {
			System.out.println(p);
		}

		// Get the inputs folder
		Path inputs = bundle.getPath("inputs");
		Files.createDirectory(inputs);

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
		Path in2 = inputs.resolve("in2");
		try (OutputStream out = Files.newOutputStream(in2)) {
			out.write(32);
		}

		// Copy out to the local file system
		Path localFile = Paths.get("target/in1.txt");
		Files.copy(in1, localFile, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("Written to: " + localFile.toAbsolutePath());

		// or copy into the bundle from the file system
		Files.copy(localFile, bundle.getRoot().resolve("inputs/in1"));
		
		// convert to java.io.File and back to Path
		File file = localFile.toFile();
		Path path = file.toPath();
		// Open in operating system's default application, e.g. Notepad
		Desktop.getDesktop().open(file);


	}
}
