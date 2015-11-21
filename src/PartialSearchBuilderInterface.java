import java.io.IOException;
import java.nio.file.Path;

public interface PartialSearchBuilderInterface {

	public void parseFile(Path file) throws IOException;

	public void parseLine(String line, InvertedIndex index);

	public void print(Path output) throws IOException;

}
