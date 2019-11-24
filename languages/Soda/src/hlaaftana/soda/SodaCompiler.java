package hlaaftana.soda;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SodaCompiler {
	private SodaClassLoader sodaClassLoader = new SodaClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
	private List<SodaFile> files = new ArrayList<>();
	private Path outputFolder = Paths.get("/");

	public SodaClassLoader getSodaClassLoader() {
		return sodaClassLoader;
	}

	public void setSodaClassLoader(SodaClassLoader sodaClassLoader) {
		this.sodaClassLoader = sodaClassLoader;
	}

	public List<SodaFile> getFiles() {
		return files;
	}

	public void setFiles(List<SodaFile> files) {
		this.files = files;
	}

	public Path getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(Path outputFolder) {
		this.outputFolder = outputFolder;
	}

	public static void main(String[] args) throws ParseException, IOException {
		Options options = new Options();
		options.addOption("cp", "classpath", true, "Specify directories to load classes from");
		options.addOption("d", true, "Output folder for converted source files");
		SodaCompiler compiler = new SodaCompiler();
		CommandLine cl = new DefaultParser().parse(options, args);
		if (cl.hasOption("d")) {
			Path path = Paths.get(cl.getOptionValue("d"));
			if (Files.isDirectory(path))
				compiler.outputFolder = path;
			else throw new IllegalArgumentException("Given output folder path is not a folder");
		}
		if (cl.hasOption("cp"))
			for (String x : cl.getOptionValue("cp").split(":"))
				compiler.sodaClassLoader.addURL(Paths.get(x).toUri().toURL());
		for (String file : cl.getArgs()) {
			Path path = Paths.get(file);
			if (Files.isDirectory(path))
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						compiler.files.add(new SodaFile(compiler, file));
						return super.visitFile(file, attrs);
					}
				});
			else compiler.files.add(new SodaFile(compiler, path));
		}
	}
}
