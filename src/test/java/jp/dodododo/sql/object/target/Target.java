package jp.dodododo.sql.object.target;

public class Target {
	public Target2 target = new Target2();

	static class Target2 {
		public String getValue() {
			return "xxx";
		}
	}

}
