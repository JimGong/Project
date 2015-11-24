import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public interface PartialSearchBuilderInterface {

	// TODO Could provide a default implementation
	// https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html
	public default void parseFile(Path file) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseLine(line, null);
			}
		}
	}

	// TODO Remove InvertedIndex as a parameter to parseLine
	public void parseLine(String line, InvertedIndex index);

	public void print(Path output) throws IOException;

}
