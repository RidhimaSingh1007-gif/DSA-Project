import java.io.*;
import java.util.*;

public class Ecommerce {
    static class Product {
        String id;
        String name;
        String category;
        double price;
        int stock;

        Product(String id, String name, String category, double price, int stock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }

        String toFileLine() {
            return id + "|" + name + "|" + category + "|" + formatPrice(price) + "|" + stock;
        }
    }

    static final String FILE_NAME = "products.txt";
    static HashMap<String, Product> products = new HashMap<>();
    static int nextIdNumber = 101;          // first ID = P101
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadFromFile();

        while (true) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1: addProduct();       break;
                case 2: viewAll();          break;
                case 3: searchProduct();    break;
                case 4: sortByPrice();      break;
                case 5: updateStock();      break;
                case 6:
                    saveToFile();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1-6.");
            }
        }
    }

   
    static void printMenu() {
        System.out.println("\n===== E-Commerce Product Management System =====");
        System.out.println("1. Add product");
        System.out.println("2. View all products");
        System.out.println("3. Search (by Product ID or Category)");
        System.out.println("4. Sort by price (low to high)");
        System.out.println("5. Update stock");
        System.out.println("6. Exit");
    }

    static void addProduct() {
        String name = readNonEmpty("Name: ");
        String category = readNonEmpty("Category: ");

        double price = readDouble("Price: ");
        while (price <= 0) {
            System.out.println("Price must be greater than 0.");
            price = readDouble("Price: ");
        }

        int stock = readInt("Stock: ");
        while (stock < 0) {
            System.out.println("Stock must not be negative.");
            stock = readInt("Stock: ");
        }

        String id = "P" + nextIdNumber++;
        Product p = new Product(id, name, category, price, stock);
        products.put(id, p);

        System.out.println("Product added. ID: " + id + " | " + name + " | "
                + category + " | " + formatPrice(price) + " | Stock: " + stock);
    }

    static void viewAll() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        ArrayList<Product> list = sortedById();
        System.out.println("\nID   | Name | Category | Price | Stock");
        System.out.println("-------------------------------------------");
        for (Product p : list) {
            System.out.println(p.id + " | " + p.name + " | " + p.category + " | "
                    + formatPrice(p.price) + " | " + p.stock);
        }
    }

    static void searchProduct() {
        String key = readNonEmpty("Enter Product ID or Category: ");

        if (key.matches("(?i)P\\d+")) {
            searchById(key.toUpperCase());
        } else {
            searchByCategory(key);
        }
    }

    static void searchById(String id) {
        Product p = products.get(id);          
        if (p == null) {
            System.out.println("Product " + id + " not found.");
        } else {
            System.out.println(p.id + " | " + p.name + " | " + p.category + " | "
                    + formatPrice(p.price) + " | Stock: " + p.stock);
        }
    }

    static void searchByCategory(String category) {
        boolean found = false;
        for (Product p : sortedById()) {
            if (p.category.equalsIgnoreCase(category)) {
                System.out.println(p.id + " | " + p.name + " | " + formatPrice(p.price));
                found = true;
            }
        }
        if (!found) {
            System.out.println("No products found in category " + category + ".");
        }
    }

    static void sortByPrice() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        ArrayList<Product> list = new ArrayList<>(products.values());
        Collections.sort(list, new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                return Double.compare(a.price, b.price);
            }
        });

        int i = 1;
        for (Product p : list) {
            System.out.println(i++ + ". " + p.id + " | " + formatPrice(p.price));
        }
    }

    static void updateStock() {
        String id = readNonEmpty("Product ID: ").toUpperCase();
        Product p = products.get(id);
        if (p == null) {
            System.out.println("Product " + id + " not found.");
            return;
        }

        int newStock = readInt("New stock: ");
        while (newStock < 0) {
            System.out.println("Stock must not be negative.");
            newStock = readInt("New stock: ");
        }

        p.stock = newStock;
        if (newStock == 0) {
            System.out.println("Stock updated. " + id + " is Out of Stock.");
        } else {
            System.out.println("Stock updated. " + id + " now has stock: " + newStock);
        }
    }

    static void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;    
        }

        int maxId = 100;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length != 5) continue;   

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String category = parts[2].trim();
                    double price = Double.parseDouble(parts[3].trim());
                    int stock = Integer.parseInt(parts[4].trim());

                    products.put(id, new Product(id, name, category, price, stock));

                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException e) {
        
                }
            }
            nextIdNumber = maxId + 1;
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Product p : sortedById()) {
                pw.println(p.toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    static ArrayList<Product> sortedById() {
        ArrayList<Product> list = new ArrayList<>(products.values());
        Collections.sort(list, new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                return Integer.compare(Integer.parseInt(a.id.substring(1)),
                                       Integer.parseInt(b.id.substring(1)));
            }
        });
        return list;
    }

    static String formatPrice(double price) {
        if (price == Math.floor(price)) {
            return String.valueOf((long) price);
        }
        return String.valueOf(price);
    }

    static String readNonEmpty(String prompt) {
        String s;
        do {
            System.out.print(prompt);
            s = sc.nextLine().trim().replace("|", "/");   // '|' is the file delimiter
            if (s.isEmpty()) System.out.println("Input cannot be empty.");
        } while (s.isEmpty());
        return s;
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}