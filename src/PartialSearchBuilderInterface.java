import java.io.IOException;
import java.nio.file.Path;

public interface PartialSearchBuilderInterface {
	// TODO Could provide a default implementation
	// https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html
	public void parseFile(Path file) throws IOException;

	// TODO Remove InvertedIndex as a parameter to parseLine
	public void parseLine(String line, InvertedIndex index);

	public void print(Path output) throws IOException;

}
