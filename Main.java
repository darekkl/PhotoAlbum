public class Main {

	public static void main(String[] args) {
		if (args.length == 1)
			new Window(args[0]);
		else
			System.out.println("Error - one parameter needed");		

	}

}
