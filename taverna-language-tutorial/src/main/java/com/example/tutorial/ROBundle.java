package com.example.tutorial;

import java.io.IOException;
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

	}
}
