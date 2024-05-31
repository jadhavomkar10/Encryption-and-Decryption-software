import java.util.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Establishing connection to the database
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/encryption","root", "Omkarjadhav1973");

            // Creating table if not exists
            createTable(connection);

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("Choose an option:");
                System.out.println("1. Encrypt a value");
                System.out.println("2. Decrypt a value");
                System.out.println("3. Delete a value");
                System.out.println("4. Show all encrypted values");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        encryptValue(connection);
                        break;
                    case 2:
                        decryptValue(connection);
                        break;
                    case 3:
                        deleteValue(connection);
                        break;
                    case 4:
                        showEncryptedValues(connection);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        connection.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void encryptValue(Connection connection) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Value For Encryption : ");
        String value = sc.nextLine();
        System.out.println("Enter the Secret Key : ");
        int secretKey = sc.nextInt();

        String encrypt = getEncryptedValue(value, secretKey);
        System.out.println("Encrypted value : " + encrypt);

        saveEncryptedValue(connection, encrypt, secretKey);
    }

    private static void decryptValue(Connection connection) throws SQLException {
        Scanner sc = new Scanner(System.in);
        String[] encryptedData = getEncryptedData(connection);
        int id = Integer.parseInt(encryptedData[0]);
        String encryptedValue = encryptedData[1];
        int savedSecretKey = Integer.parseInt(encryptedData[2]);

        System.out.println("Enter the Secret Key : ");
        int secretKey = sc.nextInt();

        if (secretKey == savedSecretKey) {
            String decryptedValue = getDecryptedValue(encryptedValue, secretKey);
            System.out.println("ID: " + id + ", Decrypted value : " + decryptedValue);
        } else {
            System.out.println("Secret key verification failed!");
        }
    }

    private static void deleteValue(Connection connection) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the ID of the value to delete: ");
        int id = sc.nextInt();

        if (id > 0) {
            String deleteSQL = "DELETE FROM encrypted_values WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Value with ID " + id + " deleted successfully.");
            } else {
                System.out.println("No value found with ID " + id + ".");
            }
        } else {
            System.out.println("Invalid ID.");
        }
    }

    private static void showEncryptedValues(Connection connection) throws SQLException {
        System.out.println("Encrypted values:");
        String selectSQL = "SELECT id, encrypted_value FROM encrypted_values";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSQL);
        int srNo = 1;
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String encryptedValue = resultSet.getString("encrypted_value");
            System.out.println("Sr. No: " + srNo + ", ID: " + id + ", Encrypted value: " + encryptedValue);
            srNo++;
        }
    }

    private static String getDecryptedValue(String encrypt, int secretKey) {
        String decrypt = "";
        for (int i = 0; i < encrypt.length(); i++) {
            char ch = encrypt.charAt(i);
            ch -= secretKey;
            decrypt = decrypt + ch;
        }
        return decrypt;
    }

    private static String getEncryptedValue(String value, int secretKey) {
        String encrypt = "";
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            ch += secretKey;
            encrypt = encrypt + ch;
        }
        return encrypt;
    }

    private static void createTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS encrypted_values (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "encrypted_value VARCHAR(255)," +
                "secret_key INT)";
        Statement statement = connection.createStatement();
        statement.execute(createTableSQL);
    }

    private static void saveEncryptedValue(Connection connection, String encryptedValue, int secretKey) throws SQLException {
        String insertSQL = "INSERT INTO encrypted_values (encrypted_value, secret_key) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        preparedStatement.setString(1, encryptedValue);
        preparedStatement.setInt(2, secretKey);
        preparedStatement.executeUpdate();
        System.out.println("Value encrypted and saved to the database.");
    }

    private static String[] getEncryptedData(Connection connection) throws SQLException {
        String selectSQL = "SELECT id, encrypted_value, secret_key FROM encrypted_values ORDER BY id DESC LIMIT 1";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSQL);
        resultSet.next();
        int id = resultSet.getInt("id");
        String encryptedValue = resultSet.getString("encrypted_value");
        int secretKey = resultSet.getInt("secret_key");
        return new String[]{String.valueOf(id), encryptedValue, String.valueOf(secretKey)};
    }
}
