package ikube.toolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Head {

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		FileReader reader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(reader);
		int lines = Integer.parseInt(args[1]);
		String line = bufferedReader.readLine();
		while (lines-- >= 0 && line != null && !line.trim().equals("")) {
			line = bufferedReader.readLine();
			System.out.println(line);
		}
		bufferedReader.close();
	}

}
