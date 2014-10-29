package com.example.tutorial;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

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
		
		


	}
}
