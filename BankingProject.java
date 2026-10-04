import java.sql.*;
import java.util.Scanner;

public class BankingProject {

    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        Scanner sc = new Scanner(System.in);

        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/customers",
            "root",
            "abc123"
        );

        System.out.println("Connected to database successfully!");

        while (true) {

            System.out.println("\n=== WELCOME TO BANKING SYSTEM ====");
            System.out.println("1. Create Account");
            System.out.println("2. Login Account");
            System.out.println("3. Make Transaction");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");
            int ch = sc.nextInt();
            sc.nextLine();


            //CREATE ACCOUNT
            if (ch == 1) {

                System.out.print("Enter customer ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Enter customer name: ");
                String name = sc.nextLine();

                System.out.print("Enter initial balance: ");
                double balance = sc.nextDouble();

                String insert =
                    "INSERT INTO bankdetails (id, name, balance) VALUES (?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(insert);

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setDouble(3, balance);

                ps.executeUpdate();

                System.out.println("Account created successfully!");

                ps.close();
            }


            //LOGIN
            else if (ch == 2) {

                System.out.print("Enter customer name: ");
                String user_name = sc.nextLine();

                System.out.print("Enter customer ID: ");
                int user_id = sc.nextInt();

                String query =
                    "SELECT * FROM bankdetails WHERE id = ? AND name = ?";

                PreparedStatement ps = con.prepareStatement(query);

                ps.setInt(1, user_id);
                ps.setString(2, user_name);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    System.out.println("\nLogin successful!");

                    System.out.println("Customer ID: "
                        + rs.getInt("id"));

                    System.out.println("Customer Name: "
                        + rs.getString("name"));

                    System.out.println("Balance: "
                        + rs.getDouble("balance"));

                } else {

                    System.out.println("Invalid customer ID or name!");
                }

                rs.close();
                ps.close();
            }
            //TRANSACTION
            else if (ch == 3) {

                System.out.print("Enter sender account ID: ");
                int send = sc.nextInt();

                System.out.print("Enter receiver account ID: ");
                int toSend = sc.nextInt();

                System.out.print("Enter amount to transfer: ");
                double amount = sc.nextDouble();


                try{
                    con.setAutoCommit(false);


                    //CHECK SENDER
                    String check =
                        "SELECT balance FROM bankdetails WHERE id = ?";

                    PreparedStatement checkPs =
                        con.prepareStatement(check);

                    checkPs.setInt(1, send);

                    ResultSet rs = checkPs.executeQuery();


                    if (!rs.next()) {

                        System.out.println("Sender account not found!");

                        con.rollback();

                        rs.close();
                        checkPs.close();

                        con.setAutoCommit(true);

                        continue;
                    }


                    // Get sender balance
                    double senderBalance =rs.getDouble("balance");


                    //CHECK BALANCE
                    if (senderBalance < amount) {

                        System.out.println("Insufficient balance!");

                        con.rollback();

                        rs.close();
                        checkPs.close();

                        con.setAutoCommit(true);

                        continue;
                    }


                    //DEDUCT FROM SENDER
                    String withdraw =
                        "UPDATE bankdetails " +
                        "SET balance = balance - ? " +
                        "WHERE id = ?";

                    PreparedStatement withdrawPs =
                        con.prepareStatement(withdraw);

                    withdrawPs.setDouble(1, amount);
                    withdrawPs.setInt(2, send);

                    withdrawPs.executeUpdate();
                    String deposit =
                        "UPDATE bankdetails " +
                        "SET balance = balance + ? " +
                        "WHERE id = ?";

                    PreparedStatement depositPs =
                        con.prepareStatement(deposit);

                    depositPs.setDouble(1, amount);
                    depositPs.setInt(2, toSend);

                    int result =
                        depositPs.executeUpdate();
                    if (result == 0) {

                        System.out.println(
                            "Receiver account not found!"
                        );

                        con.rollback();

                    } else {
                        con.commit();

                        System.out.println(
                            "Transaction successful!"
                        );
                    }
                    rs.close();
                    checkPs.close();
                    withdrawPs.close();
                    depositPs.close();
                    con.setAutoCommit(true);
                }
                catch (Exception e) {

                    con.rollback();
                    System.out.println("Transaction failed!");

                    System.out.println(e);
                    con.setAutoCommit(true);
                }
            }
            //EXIT
            else if (ch == 4) {

                System.out.println("Thank you for using Banking System!");
                break;
            }

            else {

                System.out.println("Invalid choice!");
            }
        }
        con.close();
        sc.close();
    }
}