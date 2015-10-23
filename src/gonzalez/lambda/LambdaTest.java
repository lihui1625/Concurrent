package gonzalez.lambda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LambdaTest {

	int aa;

	public static void main(String[] args) throws IOException {

		List<String> list = Files.readAllLines(Paths.get("d:\\yellow.txt"));

		Map<Object, Integer> map = list.stream()
				.flatMap(x -> Stream.of(x.split(" ")))
				.collect(Collectors.toMap(x -> x, x -> 1, Integer::sum));
		map.entrySet()
				.forEach(x -> System.out.println(x.getKey() + " : "+ x.getValue()));

	}

}

interface MyInterface {
	int aa = 0;
	static int bb = 0;

	String dd = "";
	static String ee = "";

	void aa();

	static void bb() {
		System.out.println("static");
	}

	default void cc() {
		System.out.println("default");
	}
}