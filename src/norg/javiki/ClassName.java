package norg.javiki;

public class ClassName {
	public static String CALLED_CLASS_NAME() {
		return Thread.currentThread().getStackTrace()[3].getClassName();
	}
}
