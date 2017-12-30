package base;

public class MRZ {
	private String issuringCountry;
	private String invoiceNumber;
	private String vendor;
	private String initial;
	private String currency;
	private String issueDate;
	private String dueDate;
	private String dollars;
	private String cents;
	
	public String getIssuringCountry() {
		return issuringCountry;
	}
	public void setIssuringCountry(String issuringCountry) {
		this.issuringCountry = issuringCountry;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getInitial() {
		return initial;
	}
	public void setInitial(String initial) {
		this.initial = initial;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getDollars() {
		return dollars;
	}
	public void setDollars(String dollars) {
		this.dollars = dollars;
	}
	public String getCents() {
		return cents;
	}
	public void setCents(String cents) {
		this.cents = cents;
	}
}
