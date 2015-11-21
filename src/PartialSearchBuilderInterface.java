import java.io.IOException;
import java.nio.file.Path;

public interface PartialSearchBuilderInterface {
	
	// TODO Assume the index will be set in the constructor, do not take as part of the method parameters

	// TODO public void parseFile(Path file)
	public void parseFile(Path file, InvertedIndex index) throws IOException;

	public void parseLine(String line, InvertedIndex index);

	public void print(Path output) throws IOException;

}
