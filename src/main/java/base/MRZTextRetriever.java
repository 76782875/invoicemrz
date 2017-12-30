package base;

public class MRZTextRetriever {
	public static MRZ retrieve(String line1, String line2) throws Exception {
		
		MRZ mrzText = new MRZ();
		
		String issuringCountry = line1.substring(2, 5).replaceAll("<", "");
		mrzText.setIssuringCountry(issuringCountry);
		
		String vendor = line1.substring(5).replaceAll("<", " ").trim();
		mrzText.setVendor(vendor);
		
		String invNumber = line2.substring(0, 9).replaceAll("<", "");
		mrzText.setInvoiceNumber(invNumber);
		
		String issueDate = line2.substring(13, 19).replaceAll("<", "");
		mrzText.setIssueDate(issueDate);
		
		String dueDate = line2.substring(21, 27).replaceAll("<", "");
		mrzText.setDueDate(dueDate);
		
		String dollars = line2.substring(28, 38).replaceAll("<", "");
		mrzText.setDollars(dollars);
		
		//String cents = line2.substring(40, 42).replaceAll("<", "");
        String cents = line2.substring(39, 41).replaceAll("<", "");
		mrzText.setCents(cents);
		
		String currency = line2.substring(10, 13).replaceAll("<", "");
		mrzText.setCurrency(currency);
		
		String initial = line2.substring(20, 21).replaceAll("<", "");
		mrzText.setInitial(initial);
		
		return mrzText;
	}
}
