package com.leandoer;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipService {
	static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static final byte[] BUFFER = new byte[1024];
	static final Path CURRENT_RELATIVE_PATH = Paths.get("");
	static final Path[] EMPTY = new Path[0];

	public void pack(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] paths = normalizePaths(filenames);
		if (Files.exists(path)) {
			logger.warning("You are about to override existing archive: " + path);
			logger.warning("Please, confirm operation: Y/[N]");
			Scanner sc = new Scanner(System.in);
			if (!sc.nextLine().matches("[Yy]")) {
				return;
			}
		}
		logger.fine("Creating new archive... " + path.toAbsolutePath());
		try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(path.toString()))) {
			addAllFilesToZip(z, path, paths);
			logger.fine("Archive created: " + path.toAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void addAllFilesToZip(ZipOutputStream z, Path name, Path... paths) {
		try {
			byte[] array;
			for (Path root : paths) {
				List<Path> val = Files.walk(root)
						.filter(path -> !Files.isDirectory(path))
						.filter(path -> !path.equals(name))
						.collect(Collectors.toList());
				for (Path child : val) {
					logger.fine("Adding file to archive: " + child.toAbsolutePath());
					z.putNextEntry(new ZipEntry(root.getParent().relativize(child).toString()));
					array = Files.readAllBytes(child.toAbsolutePath());
					z.write(array);
					z.closeEntry();
				}
			}
		} catch (IOException ex) {
			logger.warning("Error: " + ex);
			logger.log(Level.FINER, ex.getMessage(), ex);
			name.toFile().delete();
		}
	}

	protected void writeAllData(InputStream inputStream, OutputStream outputStream) throws IOException{
		int length;
		while ((length = inputStream.read(BUFFER))!=-1){
			outputStream.write(BUFFER, 0, length);
		}
	}


	public void update(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] paths = normalizePaths(filenames);
		logger.fine("Updating archive... " + path.toAbsolutePath());
		modifyEntryList(path, paths, EMPTY);
	}


	public void unpack(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] destination = normalizePaths(filenames);
		Path root;
		if (destination.length == 0) {
			root = Paths.get(Paths.get("").toAbsolutePath().toString(),
					path.getFileName().toString().split("\\.")[0]);
		} else if (destination.length == 1) {
			root = destination[0].resolve(path.getFileName().toString().split("\\.")[0]);
		} else {
			throw new RuntimeException("Ambiguous destination for -n (unpack), choose one: " +
					Arrays.stream(destination)
							.map(Path::toString)
							.collect(Collectors.joining(", "))
			);
		}
		logger.fine("Unpacking archive... " + path.toAbsolutePath());
		try (ZipInputStream z = new ZipInputStream(new FileInputStream(name))) {
			ZipEntry next;
			FileOutputStream fos;
			while ((next = z.getNextEntry()) != null) {
				Path current = Paths.get(root.toString(), next.toString());
				logger.fine("Unpacking file: " + current);
				if (!Files.exists(current)) {
					if (current.getParent() != null) {
						Files.createDirectories(current.getParent());
					}
					Files.createFile(current);
				}
				fos = new FileOutputStream(current.toString());
				writeAllData(z, fos);
				z.closeEntry();
				fos.close();
			}
			logger.fine("Unpacked archive: " + root);
		} catch (IOException ex) {
			logger.warning("Error: " + ex);
			logger.log(Level.FINER, ex.getMessage(), ex);
		}
	}


	protected boolean isRemoved(ZipEntry e, Path... filenames) {
		for (Path filename : filenames) {
			if (e.toString().startsWith(filename.toString())) {
				return true;
			}
		}
		return false;
	}



	protected String getTempFile(String suffix) throws IOException {
		return Files.createTempFile("file", ".zip").toAbsolutePath().toString();
	}

	protected void modifyEntryList(Path path, Path[] pathsToAdd, Path[] pathsToRemove){
		try {
			File f = new File(getTempFile(path.getFileName().toString()));
			f.deleteOnExit();
			logger.fine("Created temporary file: "+ f.getAbsolutePath());
			try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))){
				ZipFile zipFile = new ZipFile(path.toString());
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry e = entries.nextElement();
					if (!isRemoved(e, pathsToRemove)) {
						zos.putNextEntry(e);
						InputStream is = zipFile.getInputStream(e);
						writeAllData(is, zos);
						zos.closeEntry();
						is.close();
					} else {
						logger.fine("Deleting file: "+e);
					}
				}
				addAllFilesToZip(zos, path, pathsToAdd);
				zipFile.close();
				zos.close();
				logger.fine("Updating archive...");
				Files.copy(f.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
			} catch(IOException ex) {
				throw ex;
			}
		} catch (IOException ex) {
			logger.warning("Error: " + ex);
			logger.log(Level.FINER, ex.getMessage(), ex);
		}



	}
	public void remove(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] paths = Arrays.stream(filenames).map(filename -> Paths.get(filename)).toArray(Path[]::new);
		logger.fine("Removing files from archive... " + path.toAbsolutePath());
		modifyEntryList(path, EMPTY, paths);
	}



	protected Path normalizePath(String path) {
		return CURRENT_RELATIVE_PATH.toAbsolutePath().resolve(path).normalize();
	}

	protected Path[] normalizePaths(String[] paths) {
		return Arrays.stream(paths)
				.map(this::normalizePath)
				.toArray(Path[]::new);
	}


	public boolean containsFile(String filename, String another){
		boolean exists = false;
		try {
			ZipFile zipFile = new ZipFile(normalizePath(filename).toString());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()){
				if (entries.nextElement().toString().startsWith(another)){
					exists = true;
					break;
				}
			}
			zipFile.close();
		} catch(IOException ex) {
		    ex.printStackTrace();
		}
		return exists;
	}

}
