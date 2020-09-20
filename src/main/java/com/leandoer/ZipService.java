package com.leandoer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipService {
	static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


	public void pack(Path name, Path... paths) {
		deleteFileIfExists(name);
		logger.finer("Creating new archive... " + name.toAbsolutePath());
		createOrUpdate(name, paths);

	}

	public void deleteFileIfExists(Path name) {
		if (Files.exists(name)) {
			try {
				logger.finer("Deleting old archive... " + name.toAbsolutePath());
				Files.delete(name);

			} catch (IOException ex) {
				logger.warning("Error: " + ex.getMessage());
				logger.log(Level.FINEST, ex.getMessage(), ex);
			}
		}
	}


	public void createOrUpdate(Path name, Path... paths) {
		if (paths.length == 0) {
			paths = new Path[]{Paths.get("").toAbsolutePath()};
		}
		try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(name.toString()))) {
			byte[] array;
			for (Path root : paths) {
				List<Path> val = Files.walk(root)
						.filter(path -> !Files.isDirectory(path))
						.filter(path -> !path.equals(name))
						.collect(Collectors.toList());
				for (Path child : val) {
					logger.finer("Adding file to archive: " + child.toAbsolutePath());
					z.putNextEntry(new ZipEntry(root.getParent().relativize(child).toString()));
					array = Files.readAllBytes(child.toAbsolutePath());
					z.write(array);
					z.closeEntry();
				}
			}
			logger.finer("Archive created: " + name.toAbsolutePath());
		} catch (IOException ex) {
			logger.warning("Error: " + ex.getMessage());
			logger.log(Level.FINEST, ex.getMessage(), ex);
			name.toFile().delete();
		}
	}


	public void unpack(Path name, Path... destination) {
		Path root;
		if (destination.length == 0) {
			root = Paths.get(Paths.get("").toAbsolutePath().toString(),
					name.getFileName().toString().split("\\.")[0]);
		} else if (destination.length == 1) {
			root = destination[0];
		} else {
			throw new RuntimeException("Ambiguous destination for -n (unpack), choose one: " +
					Arrays.stream(destination)
							.map(Path::toString)
							.collect(Collectors.joining(", "))
			);
		}
		logger.finer("Unpacking archive... " + name.toAbsolutePath());
		try (ZipInputStream z = new ZipInputStream(new FileInputStream(name.toString()))) {
			byte[] buffer = new byte[1024];
			ZipEntry next;
			FileOutputStream fos;
			while ((next = z.getNextEntry()) != null) {
				int length;

				Path current = Paths.get(root.toString(), next.toString());
				logger.finer("Unpacking file: " + current);
				if (!Files.exists(current)) {
					if (current.getParent() != null) {
						Files.createDirectories(current.getParent());
					}
					Files.createFile(current);
				}
				fos = new FileOutputStream(current.toString());
				while ((length = z.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}

				z.closeEntry();
				fos.close();
			}
			logger.finer("Unpacked archive: " + root);
		} catch (IOException ex) {
			logger.warning("Error: " + ex.getMessage());
			logger.log(Level.FINEST, ex.getMessage(), ex);
		}
	}


	public void remove(Path name, Path... filenames) {
		Path currentRoot = Paths.get(Paths.get("").toAbsolutePath().toString(),
				name.getFileName().toString().split("\\.")[0]);
		logger.finer("Temporarily unpacking archive...");
		unpack(name);

		for (Path filename : filenames) {
			Path currentFile = currentRoot.toAbsolutePath().resolve(filename);
			if (Files.exists(currentFile)) {
				try {
					logger.finer("Deleting file: " + currentFile);
					deleteFolderRecursively(currentFile);
				} catch (IOException ex) {
					logger.warning("Error: " + ex.getMessage());
					logger.log(Level.FINEST, ex.getMessage(), ex);
				}
			}
		}
		try {
			Path[] filesToZip = Files.find(currentRoot, 1, (file, attributes) -> true).skip(1).toArray(Path[]::new);
			pack(name, filesToZip);
			logger.finer("Removing temporary folder: " + currentRoot);
			deleteFolderRecursively(currentRoot);
		} catch (IOException ex) {
			logger.warning("Error: " + ex.getMessage());
			logger.log(Level.FINEST, ex.getMessage(), ex);
		}

	}

	public void deleteFolderRecursively(Path folder) throws IOException {
		Files.walk(folder)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

}
// -d -verbose file.zip /parserchain/CLIFIlter.java
// -n -verbose file.zip
// -a -verbose file.zip src/main
// -n -verbose file.zip src