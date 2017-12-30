package base;

import java.util.HashMap;
import java.util.Map;

// LINE 1
// 1 to 2	Document code
// 3 to 5	Issuing State or Organization
// 6 to 44	Name

// LINE 2
// 1 to 9	Invoice number
// 10		Check digit
// 11 to 13	Currency
// 14 to 19	Issue date
// 20		Check digit
// 21		Initial / Repeat
// 22 to 27	Due date
// 28		Check digit
// 29 to 42	Total [9 << 00]
// 43		Check digit
// 44 		Composite check digit
public class MRZTextProducer {
	
	public static Map<String, String> issuingState = new HashMap<String, String>();
	public static Map<String, String> currency = new HashMap<String, String>();
	public static Map<Character, Integer> alpha = new HashMap<Character, Integer>();
	
	private static MRZTextProducer instance;
	
	public static MRZTextProducer getInstance(){
		if (instance == null){
			instance = new MRZTextProducer();
		}
		
		return instance;
	}
	
	private MRZTextProducer(){
		issuingState.put("Russian Federation", "RUS");
		issuingState.put("United Arab Emirates", "UAE");
		issuingState.put("Australia", "AUS");
		currency.put("Rubble", "RUB");
		currency.put("Australian Dollar", "AUD");
		currency.put("United Arab Emirates Dirham", "AED");
		
		alpha.put('A', 10);
		alpha.put('B', 11);
		alpha.put('C', 12);
		alpha.put('D', 13);
		alpha.put('E', 14);
		alpha.put('F', 15);
		alpha.put('G', 16);
		alpha.put('H', 17);
		alpha.put('I', 18);
		alpha.put('J', 19);
		alpha.put('K', 20);
		alpha.put('L', 21);
		alpha.put('M', 22);
		alpha.put('N', 23);
		alpha.put('O', 24);
		alpha.put('P', 25);
		alpha.put('Q', 26);
		alpha.put('R', 27);
		alpha.put('S', 28);
		alpha.put('T', 29);
		alpha.put('U', 30);
		alpha.put('V', 31);
		alpha.put('W', 32);
		alpha.put('X', 33);
		alpha.put('Y', 34);
		alpha.put('Z', 35);
	} 
	
	public static char checkDigitNumber(String date){
		int[] weights = new int[]{7,3,1,7,3,1,7,3,1,7,3,1};
		
		int sum = 0;
		for (int i = 0; i < date.length(); i++){
			char c = date.charAt(i);
			int cValue = 0;
			if (Character.isDigit(c)){
	 			cValue = Integer.valueOf(String.valueOf(c)) * weights[i];
			}
			sum += cValue;
		}
		
		int rem = sum % 10;
		
		return String.valueOf(rem).charAt(0);
	}
	
	public static char checkDigit9(String line){
		int[] weights = new int[]{7,3,1,7,3,1,7,3,1};
		
		int sum = 0;
		for (int i = 0; i < 9 && i < line.length(); i++){
			char c = line.charAt(i);
			int cValue = 0;
			if (Character.isDigit(c)){
	 			cValue = Integer.valueOf(String.valueOf(c)) * weights[i];
			}else if (Character.isLetter(c)){
				cValue = alpha.get(c);
				cValue = cValue * weights[i];
			}
			sum += cValue;
		}
		
		int rem = sum % 10;
		
		return String.valueOf(rem).charAt(0);
	}
	
	
	// 1 - 10, 14 - 20, 22-43
	public static char checkAll(String line){
		int[] weights = new int[]{7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1,7,3,1};
		
		int counter = 0;
		
		int sum = 0;
		for (int i = 0; i < 10; i++){
			char c = line.charAt(i);
			int cValue = 0;
			if (Character.isDigit(c)){
	 			cValue = Integer.valueOf(String.valueOf(c)) * weights[counter];
			}else if (Character.isLetter(c)){
				cValue = alpha.get(c);
				cValue = cValue * weights[i];
			}
			sum += cValue;
			counter++;
		}
		
		for (int i = 13; i < 20; i++){
			char c = line.charAt(i);
			int cValue = 0;
			if (Character.isDigit(c)){
	 			cValue = Integer.valueOf(String.valueOf(c)) * weights[counter];
			}else if (Character.isLetter(c)){
				cValue = alpha.get(c);
				cValue = cValue * weights[i];
			}
			sum += cValue;
			counter++;
		}
		
		for (int i = 21; i < 43; i++){
			char c = line.charAt(i);
			int cValue = 0;
			if (Character.isDigit(c)){
	 			cValue = Integer.valueOf(String.valueOf(c)) * weights[counter];
			}else if (Character.isLetter(c)){
				cValue = alpha.get(c);
				cValue = cValue * weights[i];
			}
			sum += cValue;
			counter++;
		}
		
		int rem = sum % 10;
		
		return String.valueOf(rem).charAt(0);
	}
	
	public char[][] generate(String name, String invoiceNumber, boolean isInitial, 
			String issueDate, String dueDate, String totalMain, String totalSub, 
			String state, String cur){
		char[] line1 = new char[44];
		char[] line2 = new char[44];
		
		for (int i = 0; i < 44; i++){
			line1[i] = '<';
			line2[i] = '<';
		}
		
		/*
		String name = "Test Ltd";
		String invoiceNumber = "P4536784";
		boolean isInitial = true;
		String issueDate = "170315";
		String dueDate = "170415";
		String totalMain = "1500";
		String totalSub = "00";
		*/
		
		state = issuingState.get(state);
		cur = currency.get(cur);
		
		name = name.toUpperCase();
		invoiceNumber = invoiceNumber.toUpperCase();
		
		line1[0] = 'I';
		line1[1] = '<';
		
		
		for (int i = 0; i < 3; i++){
			if (state.length() < i + 1){
				line1[i + 2] = '<';
			}else{
				line1[i + 2] = state.charAt(i);
			} 
		}
		
		for (int i = 0; i < 38; i++){
			if (name.length() < i + 1){
				line1[i + 5] = '<';
			}else{
				char ch = name.charAt(i);
				if (Character.isLetter(ch)){
					line1[i + 5] = name.charAt(i);
				}else if (Character.isWhitespace(ch) || ch == '-'){
					line1[i + 5] = '<';
				}
			} 
		}
		
		for (int i = 0; i < 9; i++){
			if (invoiceNumber.length() < i + 1){
				line2[i + 0] = '<';
			}else{
				char ch = invoiceNumber.charAt(i);
				if (Character.isLetterOrDigit(ch)){
					line2[i + 0] = invoiceNumber.charAt(i);
				}
			} 
		}
		
		line2[9] = checkDigit9(invoiceNumber);
		
		for (int i = 0; i < 3; i++){
			if (cur.length() < i + 1){
				line2[i + 10] = '<';
			}else{
				line2[i + 10] = cur.charAt(i);
			} 
		}
		
		for (int i = 0; i < 6; i++){
			if (issueDate.length() < i + 1){
				line2[i + 13] = '<';
			}else{
				line2[i + 13] = issueDate.charAt(i);
			} 
		}
		
		line2[19] = checkDigitNumber(issueDate);
		line2[20] = '0';
		if (!isInitial){
			line2[20] = '1';
		}
		
		for (int i = 0; i < 6; i++){
			if (dueDate.length() < i + 1){
				line2[i + 21] = '<';
			}else{
				line2[i + 21] = dueDate.charAt(i);
			} 
		}
		
		line2[27] = checkDigitNumber(dueDate);
		
		for (int i = 0; i < 9; i++){
			if (totalMain.length() < i + 1){
				line2[i + 28] = '<';
			}else{
				line2[i + 28] = totalMain.charAt(i);
			} 
		}
		
		line2[37] = '<';
		line2[38] = '<';
		
		for (int i = 0; i < 2; i++){
			if (totalSub.length() < i + 1){
				line2[i + 39] = '<';
			}else{
				line2[i + 39] = totalSub.charAt(i);
			} 
		}
		
		line2[42] = checkDigitNumber(totalMain + totalSub);
		line2[43] = checkAll(String.valueOf(line2));
		
		char[][] lines = new char[2][44];
		lines[0] = line1;
		lines[1] = line2;
		
		return lines;
		
		//String textTop = String.valueOf(line1);
		//String textBottom = String.valueOf(line2);
		
		//System.out.println(textTop);
		//System.out.println(textBottom);
	}
}
