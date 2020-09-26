package com.leandoer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class ZIpServiceTest {
	public static final String TEST_DIR = "src\\main\\resources\\test-data";
	public static final String ZIP_FILE = TEST_DIR + "\\file.zip";

	public static final List<String> testFiles = Arrays.asList(
			TEST_DIR,
			TEST_DIR + "\\one",
			TEST_DIR + "\\one\\five.java",
			TEST_DIR + "\\one\\four.java",
			TEST_DIR + "\\three.java",
			TEST_DIR + "\\two",
			TEST_DIR + "\\two\\seven.txt",
			TEST_DIR + "\\two\\six.txt"
	);

	@AfterClass
	public static void afterClass() throws Exception {
		Files.walk(Paths.get(TEST_DIR))
				.filter(path -> !testFiles.contains(path.toString()))
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);

	}

	@Before
	public void setUp() throws Exception {
		Files.walk(Paths.get(TEST_DIR))
				.filter(path -> !testFiles.contains(path.toString()))
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	@Test
	public void testPack() {
		ZipService zipService = new ZipService();
		zipService.pack(ZIP_FILE,
				TEST_DIR + "\\one\\five.java",
				TEST_DIR + "\\two",
				TEST_DIR + "\\three.java"
		);

		assertTrue(zipService.containsFile(ZIP_FILE, "five.java"));
		assertTrue(zipService.containsFile(ZIP_FILE, "two"));
		assertTrue(zipService.containsFile(ZIP_FILE, "three.java"));
	}

	@Test
	public void normalizePath() {
		ZipService zipService = new ZipService();
		assertEquals("Change \"System.getProperty(\"user.home\")+\"IdeaProjects\\\\ to the location of zip-cli-tool on your drive\"",
				System.getProperty("user.home") + "\\IdeaProjects\\zip-cli-tool\\src\\main\\resources\\test-data",
				zipService.normalizePath("..\\zip-cli-tool\\src\\main\\resources\\test-data").toString());
	}

	@Test
	public void testRemove() {
		ZipService zipService = new ZipService();
		zipService.pack(ZIP_FILE,
				TEST_DIR + "\\two",
				TEST_DIR + "\\one"
		);
		assertTrue(zipService.containsFile(ZIP_FILE, "one\\five.java"));
		assertTrue(zipService.containsFile(ZIP_FILE, "two\\seven.txt"));
		assertTrue(zipService.containsFile(ZIP_FILE, "two\\six.txt"));

		zipService.remove(ZIP_FILE,
				"one\\five.java",
				"two\\seven.txt",
				"two\\six.txt"
		);

		assertFalse(zipService.containsFile(ZIP_FILE, "one\\five.java"));
		assertFalse(zipService.containsFile(ZIP_FILE, "two\\seven.txt"));
		assertFalse(zipService.containsFile(ZIP_FILE, "two\\six.txt"));
		assertTrue(zipService.containsFile(ZIP_FILE, "one\\four.java"));
	}

	@Test
	public void testUpdate() {
		ZipService zipService = new ZipService();
		zipService.pack(ZIP_FILE,
				TEST_DIR + "\\two\\seven.txt",
				TEST_DIR + "\\one\\five.java"
		);
		assertFalse(zipService.containsFile(ZIP_FILE, "four.java"));
		assertFalse(zipService.containsFile(ZIP_FILE, "six.txt"));
		assertFalse(zipService.containsFile(ZIP_FILE, "three.java"));

		zipService.update(ZIP_FILE,
				TEST_DIR + "\\one\\four.java",
				TEST_DIR + "\\two\\six.txt",
				TEST_DIR + "\\three.java"
		);
		assertTrue(zipService.containsFile(ZIP_FILE, "four.java"));
		assertTrue(zipService.containsFile(ZIP_FILE, "six.txt"));
		assertTrue(zipService.containsFile(ZIP_FILE, "three.java"));
	}

	@Test
	public void testUnpack() throws Exception {
		ZipService zipService = new ZipService();
		zipService.pack(ZIP_FILE,
				TEST_DIR + "\\two",
				TEST_DIR + "\\one",
				TEST_DIR + "\\three.java"
		);

		zipService.unpack(ZIP_FILE, TEST_DIR);
		final List<String> unpackedFiles = Arrays.asList(
				"file",
				"file\\one",
				"file\\one\\five.java",
				"file\\one\\four.java",
				"file\\three.java",
				"file\\two",
				"file\\two\\seven.txt",
				"file\\two\\six.txt"
		);

		long size = Files.walk(Paths.get(TEST_DIR + "\\file"))
				.map(path -> Paths.get(TEST_DIR).relativize(path))
				.filter(unpackedFiles::contains).count();

		assertEquals(0L, size);

	}

}
