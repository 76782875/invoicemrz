package base;

public class MRZTextChecker {
	public static void main(String[] args) throws Exception {
		String line2 = "P4536784<6RUB1703157017041541500<<<<<<<00<28";
		System.out.println(check(line2));
	}
	
	public static boolean check(String line2) throws Exception {
		
		if (line2.length() != 44){
			return false;
		}
		
		char invNumber = MRZTextProducer.getInstance().checkDigit9(line2.substring(0, 9));
		if (invNumber != line2.charAt(9)){
			return false;
		}
		
		char issueDate = MRZTextProducer.getInstance().checkDigitNumber(line2.substring(13, 19));
		if (issueDate != line2.charAt(19)){
			return false;
		}
		
		char dueDate = MRZTextProducer.getInstance().checkDigitNumber(line2.substring(21, 27));
		if (dueDate != line2.charAt(27)){
			return false;
		}
		
		char total = MRZTextProducer.getInstance().checkDigitNumber(line2.substring(28, 42).replaceAll("<", ""));
		if (total != line2.charAt(42)){
			return false;
		}
		
		char all = MRZTextProducer.getInstance().checkAll(line2);
		if (all != line2.charAt(43)){
			return false;
		}
		
		return true;
	}
}
