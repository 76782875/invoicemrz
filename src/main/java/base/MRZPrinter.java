package base;

public class MRZPrinter {
	public static void print(MRZ mrz){
		System.out.println("Vendor: " + mrz.getVendor());
		System.out.println("Issuring Country: " + mrz.getIssuringCountry());
		System.out.println("Invoice Number: " + mrz.getInvoiceNumber());
		
		System.out.println("Copy: " + mrz.getInitial());
		
		System.out.println("Issue Date: " + mrz.getIssueDate());
		System.out.println("Due Date: " + mrz.getDueDate());
		System.out.println("Total: " + mrz.getDollars() + "." + mrz.getCents() + " " + mrz.getCurrency());
	}
}
