package ikube.miscellaneous;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FileEncodingTest {

	@Test
	public void encoding() throws Exception {
		File file = new File("E:/TEST_VAT.csv");
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		for (int i = 0; i < 10; i++) {
			System.out.println(bufferedReader.readLine());
		}
		bufferedReader.close();
	}

}
