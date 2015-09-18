import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyDriver {

	public static void main(String[] args) throws IOException {
		InvertedIndex i = new InvertedIndex();
		i.dirTraverse(new File("/Users/JiaMinGong/Desktop/TestFolder"));
		String name = "output.json";
		Path outputPath = Paths.get(".", "output", name);

		Files.createDirectories(outputPath.getParent());
		Files.deleteIfExists(outputPath);

		i.printWordMap(outputPath);
	}

}
