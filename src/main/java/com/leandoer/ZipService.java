package com.leandoer;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
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

	public void pack(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] paths = normalizePaths(filenames);
		logger.finer("Creating new archive... " + path.toAbsolutePath());
		try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(name.toString()));){
			addAllFilesToZip(z, path, paths);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void addAllFilesToZip(ZipOutputStream z, Path name, Path... paths) {
		try {
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

	public void writeAllData(InputStream inputStream, OutputStream outputStream) throws IOException{
		int length;
		while ((length = inputStream.read(BUFFER))!=-1){
			outputStream.write(BUFFER, 0, length);
		}
	}


	public void update(String name, String... filenames){
		Path path = normalizePath(name);
		Path[] paths = normalizePaths(filenames);
		File f = new File("~~temp.zip");
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))){
			ZipFile zipFile = new ZipFile(name.toString());
			Enumeration<? extends ZipEntry> entries= zipFile.entries();
			while (entries.hasMoreElements()){
				ZipEntry e = entries.nextElement();
				zos.putNextEntry(e);
				InputStream is = zipFile.getInputStream(e);
				writeAllData(is, zos);
				zos.closeEntry();
				is.close();
			}
			addAllFilesToZip(zos, path, paths);
			zipFile.close();
			zos.close();
			Files.copy(f.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
			f.delete();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}


	public void unpack(String name, String... filenames) {
		Path path = normalizePath(name);
		Path[] destination = normalizePaths(filenames);
		Path root;
		if (destination.length == 0) {
			root = Paths.get(Paths.get("").toAbsolutePath().toString(),
					path.getFileName().toString().split("\\.")[0]);
		} else if (destination.length == 1) {
			root = destination[0];
		} else {
			throw new RuntimeException("Ambiguous destination for -n (unpack), choose one: " +
					Arrays.stream(destination)
							.map(Path::toString)
							.collect(Collectors.joining(", "))
			);
		}
		logger.finer("Unpacking archive... " + path.toAbsolutePath());
		try (ZipInputStream z = new ZipInputStream(new FileInputStream(name.toString()))) {
			byte[] buffer = new byte[1024];
			ZipEntry next;
			FileOutputStream fos;
			while ((next = z.getNextEntry()) != null) {
				Path current = Paths.get(root.toString(), next.toString());
				logger.finer("Unpacking file: " + current);
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
			logger.finer("Unpacked archive: " + root);
		} catch (IOException ex) {
			logger.warning("Error: " + ex.getMessage());
			logger.log(Level.FINEST, ex.getMessage(), ex);
		}
	}


	public boolean isRemoved(ZipEntry e, String...filenames){
		for (String filename : filenames){
			if (e.toString().startsWith(filename)){
				System.out.println(e);
				return true;
			}
		}
		return false;
	}

	public void remove(String name, String... filenames) {
		Path path = normalizePath(name);
		File f = new File("~~temp.zip");
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))){
			ZipFile zipFile = new ZipFile(name.toString());
			Enumeration<? extends ZipEntry> entries= zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				if (!isRemoved(e, filenames)) {
					zos.putNextEntry(e);
					InputStream is = zipFile.getInputStream(e);
					writeAllData(is, zos);
					zos.closeEntry();
					is.close();
				}
			}
			zipFile.close();
			zos.close();
			Files.copy(f.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
			f.delete();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public void deleteFolderRecursively(Path folder) throws IOException {
		Files.walk(folder)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}
	public Path normalizePath(String path){
		return CURRENT_RELATIVE_PATH.toAbsolutePath().resolve(path).normalize();
	}
	public Path[] normalizePaths(String[] paths){
		return Arrays.stream(paths).map(path -> normalizePath(path)).toArray(Path[]::new);
	}

}
// -d -verbose file.zip /parserchain/CLIFIlter.java
// -n -verbose file.zip
// -a -verbose file.zip src/main
// -n -verbose file.zip src