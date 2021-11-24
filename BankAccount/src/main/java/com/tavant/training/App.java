package com.tavant.training;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.*;

public class App {
	public static final String URLConnect = "jdbc:mysql://localhost:3306/day_3_demodb";
	public static final String USERName = "root";
	public static final String PASSWord = "Killua$123";

	Connection dbConnection;
	String query;
	Statement newStatement;
	PreparedStatement newPreparedStatement;
	Integer minBalance = 1000;
	Integer balance1;

	App() {
		try {
			dbConnection = DriverManager.getConnection(URLConnect, USERName, PASSWord);
			newStatement = dbConnection.createStatement();

			System.out.println("we are able to connect to the db");
		} catch (SQLException e) {
			System.out.println("the db cant be connected to: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		App obj1 = new App();
		Integer balance = 0;

		System.out.println("Choose an option:\n" + "1. Create new bank account\n" + "2. Login\n");
		Scanner sc = new Scanner(System.in);
		Integer cases = sc.nextInt();
		sc.nextLine();
		switch (cases) {
		case 1: { // create an account
			System.out.println("enter the name, phone, address and password of the account");
			obj1.addBankAccounts(sc.nextLine(), sc.nextLine(), sc.nextLine(), sc.nextLine());
			//
			break;
		}
		case 2: // login into a user
			// Scanner sc = new Scanner(System.in);
			System.out.println("Enter userName/accountId and password");
			long userName = sc.nextLong();
			sc.nextLine();
			String passWord = sc.nextLine();
			System.out.println("Choose the options\n" + "1. Check for low balance\n" + "2. deposit\n" + "3. withdraw\n"
					+ "4. funds transfer\n" + "5. print all trancsactions\n");
			Integer types = sc.nextInt();
			sc.nextLine();
			String passwordCheck = obj1.details(userName);
			// System.out.println(passwordCheck+" " + passWord);
			if (passwordCheck.equals(passWord)) {
				switch (types) {
				case 1: { // check low balance
					System.out.println("Checking for balance");
					// System.out.println("the balance is:"+obj1.lowBalanceCheck(userName));
					break;
				}
				case 2: // deposit
					balance = obj1.lowBalanceCheck(userName);
					// System.out.println(balance);
					System.out.println("enter the deposit amount");
					Integer depositAmount = sc.nextInt();
					sc.nextLine();
					obj1.deposit(depositAmount, userName, balance);
					break;
				case 3: // withdraw
					balance = obj1.lowBalanceCheck(userName);
					// System.out.println(balance);
					System.out.println("enter the withdraw amount");
					Integer withdrawAmount = sc.nextInt();
					sc.nextLine();
					obj1.withdraw(withdrawAmount, userName, balance);
					break;
				case 4: // funds transfer
					balance = obj1.lowBalanceCheck(userName);
					// System.out.println(balance);
					System.out.println("enter the accountId(long) and transfer amount");
					long touser = sc.nextLong();
					Integer transferAmount = sc.nextInt();
					sc.nextLine();
					obj1.fundstransfer(transferAmount, userName, balance, touser);
					break;
				case 5: // print all transactions
					obj1.printAllTransactions(userName);
					break;
				}
			}
		}
	}

	void addBankAccounts(String name, String address, String password, String phone) {
		// System.out.println(address+ " "+phone+" "+" "+name+" ");
		// sc.nextLine();
		query = "insert into bankaccount(name, phone, address, password) values(?,?, ?, ?)";
		// query = "insert into details(name, addr, previousCompany, salary) values(?,
		// ?, ?, ?)";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setString(1, name);
			newPreparedStatement.setString(2, phone);
			newPreparedStatement.setString(3, address);
			newPreparedStatement.setString(4, password);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("the details are added successfully");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("the details could not be added because: " + e.getMessage() + " " + e.getErrorCode());
		}
	}

	int lowBalanceCheck(Long accountId) {
		query = "select balance from bankaccount where accountId = ?";
		Integer amountBalance = 0;
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, accountId);

			ResultSet values = newPreparedStatement.executeQuery();
			if (values.next()) {
				if (values.getInt("balance") > minBalance) {
					amountBalance = values.getInt("balance");
					System.out.println("the balance in the account is: " + amountBalance);

				}

				else {
					System.out
							.println("the balance in the account is low please add some more money to avoid penalties");
					return 0;
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("could not execute the low balance check query: " + e.getMessage());
		}
		// return 0;
		// System.out.println(amountBalance);
		return amountBalance;
	}

	String details(Long accountId) {
		String password = new String();
		// System.out.println(accountId);
		query = "select password from bankaccount where accountId = ?";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, accountId);
			ResultSet value = newPreparedStatement.executeQuery();
			if (value.next()) {
				password = value.getString("password");
				System.out.println("successful check for password");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("the error is in fetching the details: " + e.getMessage());
		}
		return password;
	}

	void deposit(Integer amount, Long accountId, Integer balance) {
		query = "update bankaccount set balance = ? where accountId = ?;";
		// query = "update bankaccount set balance = ? where accountId = ?;";

		try {

			newPreparedStatement = dbConnection.prepareStatement(query);
			amount = amount + balance;
			// System.out.println(balance);
			newPreparedStatement.setInt(1, amount);
			newPreparedStatement.setLong(2, accountId);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("Successfully deposited");

			}
		} catch (SQLException e) {
			System.out.println("the error is:" + e.getMessage());
		}

		query = "insert into transactions(rxId, txId, amount, time) values(?, 1, ?, CURRENT_TIMESTAMP);";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, accountId);
			newPreparedStatement.setInt(2, amount);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("successfully added transactions");
			}
		} catch (SQLException e) {
			System.out.println("error is due to this " + e.getMessage());
		}
	}

	void withdraw(Integer amount, Long accountId, Integer balance) {
		query = "update bankaccount set balance = ? where accountId = ?;";
		// query = "update bankaccount set balance = ? where accountId = ?;";

		try {

			newPreparedStatement = dbConnection.prepareStatement(query);
			amount = balance - amount;
			// System.out.println(balance);
			if (amount > minBalance) {
				newPreparedStatement.setInt(1, amount);
				newPreparedStatement.setLong(2, accountId);
				if (newPreparedStatement.executeUpdate() > 0) {
					System.out.println("Successfully withdrawn");
				}
			}

		} catch (SQLException e) {
			System.out.println("the error is:" + e.getMessage());
		}
		query = "insert into transactions(rxId, txId, amount, time) values(1, ?, ?, CURRENT_TIMESTAMP);";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, accountId);
//			newPreparedStatement.setLong(2, accountId);
			newPreparedStatement.setInt(2, amount);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("successfully added transactions");
			}
		} catch (SQLException e) {
			System.out.println("error is due to this " + e.getMessage());
		}
	}

	void fundstransfer(Integer amount, Long accountId, Integer balance, Long touser) {
		query = "update bankaccount set balance = ? where accountId = ?;";

		// sender
		try {

			newPreparedStatement = dbConnection.prepareStatement(query);

			// System.out.println(balance);
			if (amount > minBalance) {
				newPreparedStatement.setInt(1, balance - amount);
				newPreparedStatement.setLong(2, accountId);
				if (newPreparedStatement.executeUpdate() > 0) {
					System.out.println("Successfully withdrawn");
				}
			}

		} catch (SQLException e) {
			System.out.println("the error is in withdrawal:" + e.getMessage());
		}
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);

			// System.out.println(balance);

			newPreparedStatement.setInt(1, balance + amount);
			newPreparedStatement.setLong(2, touser);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("Successfully deposited");

			}

		} catch (SQLException e) {
			System.out.println("the error is depositing:" + e.getMessage());
		}

		query = "insert into transactions(rxId, txId, amount, time) values(?, ?, ?, CURRENT_TIMESTAMP);";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, touser);
			newPreparedStatement.setLong(2, accountId);
			newPreparedStatement.setInt(3, amount);
			if (newPreparedStatement.executeUpdate() > 0) {
				System.out.println("successfully added transactions");
			}
		} catch (SQLException e) {
			System.out.println("error is due to this " + e.getMessage());
		}
	}

	void printAllTransactions(Long UserId) {
		query = " select * from transactions where rxId = ? or txId = ?;";
		try {
			newPreparedStatement = dbConnection.prepareStatement(query);
			newPreparedStatement.setLong(1, UserId);
			newPreparedStatement.setLong(2, UserId);
			ResultSet values = newPreparedStatement.executeQuery();
			while (values.next())
				System.out.println("tId:" + values.getString("tId") + " rxId: " + values.getString("rxId") + " txId "
						+ values.getString("txId") + " timestamp " + values.getString("time"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("error is in here " + e.getMessage());
		}

	}
}
