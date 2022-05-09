package control;

import model.Color;
import view.UserInterface;

import java.sql.*;

public class Controller {
    UserInterface view = new UserInterface();

    public void start() {
        view.menuMain();
    }

    //============================ DATABASE CONNECTION =============================//
    public Connection getDatabaseConnection() {

        //<editor-fold desc="url, user, password">
        String url = "jdbc:postgresql://pgserver.mau.se:5432/am2510";
        String user = "am2510";
        String password = "zyvl0ir7";
        //</editor-fold>

        Connection con = null;

        try {
            con = DriverManager.getConnection(url, user, password);
            return con;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //============================ REGISTRATION AND LOGIN =============================//
    public Boolean validateLogin(int adminId, String adminPwd) throws SQLException {
        boolean ok = false;

        String query = "SELECT \"pwd\" FROM \"ose_admin\" WHERE admin_id ='" + adminId + "'";
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        ResultSet rs1 = stmt.executeQuery(query);

        String pwdFromDatabase;

        if (rs1.next()) {
            pwdFromDatabase = rs1.getString("pwd");
            if (adminPwd.equals(pwdFromDatabase)) {
                System.out.println("Admin with ID '" + adminId + "' is logged in successfully!");
                ok = true;
            }
            else {
                System.out.println("Not found admin with such ID '" + adminId + "' or invalid password!");
            }
        }
        else { System.out.println("Admin not found!"); }

        stmt.close();
        con.close();
        return ok;
    }
    public Boolean authenticationLoginCustomer(String customerEmail, String pwd) throws SQLException {
        boolean ok = false;
        String query = "SELECT FROM \"ose_customer\" WHERE email ='" + customerEmail + "'";
        String query2 = "SELECT FROM \"ose_customer\" WHERE pwd ='" + pwd + "'";
        Connection con = getDatabaseConnection();
        Statement stmt1 = con.createStatement();
        Statement stmt2 = con.createStatement();
        ResultSet rs1 = stmt1.executeQuery(query);
        ResultSet rs2 = stmt2.executeQuery(query2);
        if (rs1.next() && rs2.next()) {
            ok = true;
            System.out.println("Customer with email '" + customerEmail + "' is logged in successfully!");
        } else {
            System.out.println("Not found user with such email '" + customerEmail + "' or invalid password!");
        }
        stmt1.close();
        stmt2.close();
        con.close();
        return ok;
    }
    public Boolean authenticationRegisterCustomer(String firstname, String lastname, String email, String address, String city, String country, String phoneNumber, String password) throws SQLException {
        boolean ok = false;

        Connection con = getDatabaseConnection();
        Statement stmt1 = con.createStatement();
        stmt1.executeUpdate("begin;");

        String query1 = "INSERT INTO \"ose_customer\"(f_name, l_name, email, address, city, country, phone_nr, pwd) VALUES( '" + firstname + "', '" + lastname + "', '" + email + "', '" + address + "', '" + city + "', '" + country + "', '" + phoneNumber + "', '" + password + "')";

        try {
            int rs1 = stmt1.executeUpdate(query1);

            if (rs1 != 0 && email != null) {
                stmt1.executeUpdate("commit;");
                System.out.println("Customer with email '" + email + "' is registered in successfully!");
                ok = true;
            } else {
                stmt1.executeUpdate("rollback;");
                System.out.println("Customer with email '" + email + "' is already registered!");
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("UniqueConstraint")) {
                ok = false;
            }
        }

        stmt1.close();
        con.close();
        return ok;
    }
    //============================ CUSTOMER BY ID =============================//
    public int getCustomerIdByEmail(String customerEmail) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        int id = 0;

        stmt.executeUpdate("begin;");

        ResultSet rs = stmt.executeQuery("select customer_id from ose_customer where email = '" + customerEmail + "';");

        if(rs.next()) {

            System.out.println("Logged in!");
            id = rs.getInt("customer_id");
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
            return id;
        }
        else {
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
            return 0;
        }
    }

    //============================ PRODUCT =============================//
    public boolean addProduct(String product_name, int category_id, int product_base_price, int product_quantity, int supplier_id) throws SQLException {

        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        String query1 = "insert into ose_product(product_name, category_id, product_base_price) " +
                "values('" + product_name + "', " + category_id + ", " + product_base_price + " );";

        String query2 = "insert into ose_stock(product_id, product_quantity) " +
                "values((select product_id from ose_product where product_name = '" + product_name + "'), " + product_quantity + " );";

        String query3 = "insert into ose_link_supplier_product(supplier_id, product_id) " +
                "values(" + supplier_id + ", (select product_id from ose_product where product_name = '" + product_name + "'));";

        int product = stmt.executeUpdate(query1);
        int quantity = stmt.executeUpdate(query2);
        int supplier = stmt.executeUpdate(query3);

        if (product != 0 && quantity != 0 && supplier != 0) {
            System.out.println("Product '" + product_name + "' is added!");
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
            return true;
        } else {
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
            return false;
        }
    }
    public boolean deleteProduct(int product_id) throws Exception {
        boolean isProductDeleted = false;

        int affectedrows6 = 0;
        int affectedrows5 = 0;
        int affectedrows4 = 0;
        int affectedrows3 = 0;
        int affectedrows2 = 0;
        int affectedrows = 0;

        String query6 = "delete from ose_order_details where product_id = ?";
        String query5 = "delete from ose_shoppingcard where product_id = ?";
        String query4 = "delete from ose_link_supplier_product where product_id = ?";
        String query3 = "delete from ose_stock where product_id = ?";
        String query2 = "delete from ose_link_product_discount where product_id = ?";
        String query = "delete from ose_product where product_id = ?";

        Connection con = getDatabaseConnection();

        PreparedStatement pstmt6 = con.prepareStatement(query6);
        PreparedStatement pstmt5 = con.prepareStatement(query5);
        PreparedStatement pstmt4 = con.prepareStatement(query4);
        PreparedStatement pstmt3 = con.prepareStatement(query3);
        PreparedStatement pstmt2 = con.prepareStatement(query2);
        PreparedStatement pstmt = con.prepareStatement(query);

        pstmt6.setInt(1, product_id);
        affectedrows6 = pstmt6.executeUpdate();

        pstmt5.setInt(1, product_id);
        affectedrows5 = pstmt5.executeUpdate();

        pstmt4.setInt(1, product_id);
        affectedrows4 = pstmt4.executeUpdate();

        pstmt3.setInt(1, product_id);
        affectedrows3 = pstmt3.executeUpdate();

        pstmt2.setInt(1, product_id);
        affectedrows2 = pstmt2.executeUpdate();

        pstmt.setInt(1, product_id);
        affectedrows = pstmt.executeUpdate();

        if (affectedrows > 0 || affectedrows2 > 0 || affectedrows3 > 0 || affectedrows4 > 0 || affectedrows5 > 0 || affectedrows6 > 0) {
            System.out.println("Product ID '" + product_id + "' is deleted successfully!");
            isProductDeleted = true;
        }

        pstmt2.close();
        pstmt.close();
        con.close();
        return isProductDeleted;
    }
    public void updateProductQuantity(int product_id, int quantity) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        String query = "UPDATE ose_stock SET product_quantity = " + quantity + " WHERE product_id = " + product_id + ";";

        int z = stmt.executeUpdate(query);

        if (z != 0) {
            System.out.println("Product : " + product_id + " now has a quantity of: " + quantity);
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
        } else {
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
        }
    }

    //============================ ASSIGN DISCOUNT TO PRODUCT =============================//
    public void assignDiscountToProduct(int product_id, int discount_id) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        String query = "insert into ose_link_product_discount(product_id, discount_id) values (" + product_id + "," + discount_id + ");";

        int z = stmt.executeUpdate(query);

        if (z != 0) {
            System.out.println("Product '" + product_id + "' got discount id '" + discount_id + "'!");
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
        } else {
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
        }
    }

    //============================ SUPPLIER =============================//
    public boolean addSupplier(String supplier_f_name, String supplier_l_name, String supplier_email, String supplier_address, String supplier_city, String supplier_country, String supplier_phone_nr) throws SQLException {
        boolean isSupplierAdded = false;

        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        String query = "insert into ose_supplier(supplier_f_name, supplier_l_name, supplier_email, supplier_address, supplier_city, supplier_country, supplier_phone_nr)" +
                "values( '" + supplier_f_name + "', '" + supplier_l_name + "', '" + supplier_email + "', '" + supplier_address + "', '" + supplier_city + "', '" + supplier_country + "', '" + supplier_phone_nr + "');";

        int rs = stmt.executeUpdate(query);

        if (rs != 0) {
            System.out.println("Supplier '" + supplier_f_name + " " + supplier_l_name + "' is added!");
            isSupplierAdded = true;
            stmt.executeUpdate("commit;");
        } else {
            stmt.executeUpdate("rollback;");
        }

        stmt.close();
        con.close();
        return isSupplierAdded;
    }

    //============================ CATEGORIES =============================//
    public void getAllCategories() throws Exception {
        String query = "select * from \"ose_category\";";
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        System.out.println("***********  Categories ***********");
        while (rs.next()) {
            int categoryId = rs.getInt("category_id");
            String categoryName = rs.getString("category_name");
            System.out.println("Category ID: " + categoryId + "\t Category Name: " + categoryName);
        }

        System.out.println();

    }

    //============================ STOCK =============================//
    public void getStock() throws SQLException {
        String query = "select * from ose_stock;";
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        System.out.println(" == Stock == ");
        System.out.println();
        while (rs.next()) {
            int productId = rs.getInt("product_id");
            int productQuantity = rs.getInt("product_quantity");
            System.out.println("Product ID: " + productId + "\tProduct Quantity: " + productQuantity);
        }
    }

    //============================ SEARCH PRODUCTS =============================//
    public boolean searchProduct(String keyWord, int keyPrice, int supplier_id, int category_id) {
        boolean foundSearchItem = false;
        try {
            String query = "null";
            if ((keyPrice > 0)) {
                query = "select * from \"ose_product\" where product_base_price = " + keyPrice + " or " + " category_id = " + keyPrice + ";";
            } else if (keyWord != null) {
                query = "select * from \"ose_product\" where product_name Ilike " + "'" + keyWord + "%" + "'" + ";";
            }else if (category_id > -1) {
                query = "select * from \"ose_product\" where category_id =" + category_id;
            }
            else if ((supplier_id > 0)) {
                query = "SELECT ose_product.product_id, product_name, category_id, product_base_price FROM ose_link_supplier_product JOIN ose_product on ose_product.product_id = ose_link_supplier_product.product_id WHERE supplier_id =" + supplier_id + ";";
            }

            Connection con = getDatabaseConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            System.out.println();
            while (rs.next()) {
                System.out.println(Color.BLUE + "ID: " + rs.getString("product_id"));
                System.out.println("Name: " + rs.getString("product_name") +
                        " | Price: " + rs.getString("product_base_price") + " SEK" +
                        " | Category id: " + rs.getString("category_id"));
                System.out.println("----------------------------------------------------------" + Color.RESET);
                foundSearchItem = true;
            }
        }catch (SQLException ex ){
            System.out.println(ex.getMessage());
            foundSearchItem = false;
        }

        return foundSearchItem;
    }
    public boolean finalStepShowProduct(int valdeCategory) throws SQLException {
        boolean okToSearch = false;
        int affectedIndex = 0;

        String query = "select product_id, product_name, product_base_price from \"ose_product\" where ose_product.category_id = " + valdeCategory + ";";
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            int productId = rs.getInt("product_id");
            String productName = rs.getString("product_name");
            int productPrice = rs.getInt("product_base_price");
            System.out.println("Product ID: " + productId + "\tProduct Name: " + productName + "\t\tProduct Base Price: " + productPrice);
            okToSearch = true;
        }
        stmt.close();
        con.close();
        return okToSearch;
    }

    //============================ DISCOUNT =============================//
    public boolean addDiscount(String discount_code, String discount_name, int discount_percentage, String discount_start_date, String discount_end_date) throws SQLException {

        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        String query = "insert into ose_discount(discount_code, discount_name, discount_percentage, discount_start_date, discount_end_date)" +
                " values ( '" + discount_code + "', '" + discount_name + "', " + discount_percentage + ", '" + discount_start_date + "', '" + discount_end_date + "');";

        int z = stmt.executeUpdate(query);

        if (z != 0) {
            System.out.println("Discount '" + discount_name + "' is added!");
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
            return true;
        } else {
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
            return false;
        }
    }
    public void updateDiscount(int product_id, int discount_id) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        int z = stmt.executeUpdate("update ose_link_product_discount set discount_id = " + discount_id + "where product_id = " + product_id + ";");

        if (z != 0) {
            System.out.println("Product : " + product_id + " now has discount: " + discount_id + "!");
            stmt.executeUpdate("commit;");
            stmt.close();
            con.close();
        } else {
            System.out.println(product_id + " has no discount!");
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
        }
    }
    public void getDiscountHistory() throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from ose_discount where CAST(discount_end_date AS DATE) <= current_date ;");
        System.out.println(" == Discount History == ");
        System.out.println();
        while (rs.next()) {
            System.out.println(Color.BLUE + "ID: " + rs.getString("discount_id"));
            System.out.println("Code: " + rs.getString("discount_code") +
                    " | Name: " + rs.getString("discount_name") +
                    " | Percentage: " + rs.getString("discount_percentage") +
                    " | Start: " + rs.getString("discount_start_date") +
                    " | End: " + rs.getString("discount_end_date"));
            System.out.println("----------------------------------------------------------" + Color.RESET);
        }
    }

    //============================ CART =============================//
    public void addToCart(int product_id, int product_quantity, String loginAdmin) throws SQLException {

        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        // insert to cart
        int insertToCart = stmt.executeUpdate("insert into ose_shoppingcard(customer_id,product_id,product_quantity) values (" + loginAdmin + "," + product_id + "," + product_quantity + ");");

        // check quantity here
        ResultSet totalQuantity = stmt.executeQuery("select product_quantity from ose_stock where product_id = " + product_id + ";");

        if (insertToCart != 0) {

            if (totalQuantity.next() && totalQuantity.getInt("product_quantity") >= product_quantity) {
                System.out.println("Product added to shopping cart!");
                //stmt.executeUpdate("UPDATE ose_stock product_quantity set product_quantity = product_quantity - " +  product_quantity + " WHERE product_id = " + product_id + ";"); // DECREASE
                stmt.executeUpdate("commit;");
                stmt.close();
                con.close();
            } else {
                System.out.println("Could not add to shopping cart! [Reason: Out of stock range]");
                stmt.executeUpdate("rollback;");
                stmt.close();
                con.close();
            }
        }
        else {
            System.out.println("Could not add to shopping cart! [Reason: Null]");
            stmt.executeUpdate("rollback;");
            stmt.close();
            con.close();
        }
    }
    public void seeCart(String customer) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("select * from ose_shoppingcard where customer_id =" + customer + ";");

        System.out.println(" == Shopping  Cart == ");
        System.out.println();
        while (rs.next()) {
            System.out.println(Color.BLUE + "Product ID: " + rs.getString("product_id") + " |  Quantity: " + rs.getString("product_quantity") + Color.RESET);
        }

        System.out.println("---------------------------");

        ResultSet rs2 = stmt.executeQuery("SELECT SUM(\n" +
                "ose_shoppingcard.product_quantity * CASE WHEN ose_discount.discount_percentage IS NOT NULL THEN ((ose_product.product_base_price/100)*(100-ose_discount.discount_percentage))\n" +
                "ELSE ose_product.product_base_price END) as total\n" +
                "FROM ose_shoppingcard\n" +
                "LEFT OUTER JOIN ose_link_product_discount ON ose_shoppingcard.product_id = ose_link_product_discount.product_id\n" +
                "JOIN ose_product ON ose_product.product_id = ose_shoppingcard.product_id\n" +
                "LEFT OUTER JOIN ose_discount ON ose_link_product_discount.discount_id = ose_discount.discount_id\n" +
                "WHERE ose_shoppingcard.customer_id = " + customer + ";");

        while(rs2.next()) {
            System.out.println(Color.BLUE + "Total Price:" + rs2.getString("total"));
        }

        System.out.println();
    }

    //============================ ORDERS =============================//
    public void seeOrdersAdmin() throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("select * from ose_order where admin_confirmed = false;");
        System.out.println(" == Unconfirmed orders == ");
        System.out.println();
        while (rs.next()) {
            System.out.println(Color.BLUE + "ID: " + rs.getString("order_id"));
            System.out.println("Customer: " + rs.getString("customer_id") +
                    " | Confirmed: false");
            System.out.println("----------------------------------------------------------" + Color.RESET);
        }

        System.out.println("");

        ResultSet rs2 = stmt.executeQuery("select * from ose_order where admin_confirmed = true;");
        System.out.println(" == Confirmed orders == ");
        System.out.println();
        while (rs2.next()) {
            System.out.println(Color.BLUE + "ID: " + rs2.getString("order_id"));
            System.out.println("Customer: " + rs2.getString("customer_id") +
                    " | Confirmed: true");
            System.out.println("----------------------------------------------------------" + Color.RESET);
        }

        System.out.println("");
    }
    public void order(String customer) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();
        System.out.println(customer + " from order");
        stmt.executeUpdate("insert into ose_order(order_id,customer_id,admin_confirmed,order_date, product_id) " +
                "values (default," + customer + ",false, (select current_date),(select product_id from ose_shoppingcard where customer_id = "+ customer +"));");
        stmt.executeUpdate("insert into ose_order_details(order_id,product_id,product_quantity,order_date) " +
                "select ose_order.order_id, ose_shoppingcard.product_id, ose_shoppingcard.product_quantity, (select current_date) " +
                "from ose_shoppingcard join ose_order on ose_shoppingcard.customer_id = ose_order.customer_id " +
                "where ose_shoppingcard.customer_id = " + customer + " and ose_order.product_id = ose_shoppingcard.product_id;");
        stmt.executeUpdate("UPDATE ose_stock SET product_quantity = ose_stock.product_quantity - total.sum FROM (SELECT SUM(product_quantity), " +
                "product_id FROM ose_shoppingcard AS shopping WHERE shopping.customer_id = " + customer +
                "GROUP BY shopping.product_id) AS total WHERE total.product_id = ose_stock.product_id; "); // DECREASE QUANTITY
        stmt.executeUpdate("delete from ose_shoppingcard where customer_id =" + customer + ";");
        System.out.println("Order added!");
        stmt.close();
        con.close();
    }
    public void cancelOrder(int order_id, String customer) throws SQLException {
        boolean isOrderCanceled = false;

        int affectedrows = 0;

        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        stmt.executeUpdate("UPDATE ose_stock SET product_quantity = ose_stock.product_quantity + total.sum FROM (SELECT SUM(product_quantity), product_id FROM ose_order_details AS orders WHERE orders.order_id = " + order_id + "GROUP BY orders.product_id) AS total WHERE total.product_id = ose_stock.product_id; "); // INCREASE QUANTITY

        ResultSet isTrue = stmt.executeQuery("select admin_confirmed from ose_order where order_id = '" + order_id + "';");

        PreparedStatement pstmt2 = con.prepareStatement("delete from ose_order_details where order_id = ?");
        PreparedStatement pstmt1 = con.prepareStatement("delete from ose_order where order_id = ?");


        pstmt2.setInt(1, order_id);
        affectedrows = pstmt2.executeUpdate();

        pstmt1.setInt(1, order_id);
        affectedrows = pstmt1.executeUpdate();

        if (affectedrows > 0) {

            if(isTrue.next()) {
                if(!isTrue.getBoolean("admin_confirmed")) {
                    System.out.println("Order ID '" + order_id  + "' is canceled (deleted) successfully!");
                    stmt.executeUpdate("commit;");
                    isOrderCanceled = true;
                }
                else if (isTrue.getBoolean("admin_confirmed")) {
                    System.out.println("Cannot delete because order is confirmed!");
                    stmt.executeUpdate("rollback;");
                    pstmt2.close();
                    pstmt1.close();
                    con.close();
                    return;
                }
            }

        } else {
            System.out.println("Nothing!");
            pstmt2.close();
            pstmt1.close();
            con.close();
            return;
        }

        pstmt2.close();
        pstmt1.close();
        con.close();
    }
    public void seeOrderCustomer(String customer) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("select * from ose_order where customer_id = " + customer + ";");
        System.out.println(" == Orders == ");
        System.out.println();
        while (rs.next()) {
            System.out.println(Color.BLUE + "ID: " + rs.getString("order_id"));
            System.out.println("Customer: " + rs.getString("customer_id") + " | Confirmed: " + rs.getBoolean("admin_confirmed"));
            System.out.println("----------------------------------------------------------" + Color.RESET);
        }

        System.out.println("");
    }

    //============================ CONFIRM AN ORDER =============================//
    public void confirmAnOrder(int order_id) throws SQLException {
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        stmt.executeUpdate("begin;");

        stmt.executeUpdate("update ose_order admin_confirmed set admin_confirmed = true where order_id = " + order_id + ";");
        stmt.executeUpdate("commit;");
        System.out.println("Order confirmed!");

        stmt.close();
        con.close();
    }

    //============================ DELETE & RESET SHOPPING CARD =============================//
    public void deleteProductFromShoppingCard(String customer, int product_id) throws Exception {
        boolean isProductDeleted = false;

        int affectedrows = 0;
        String query = "delete from \"ose_shoppingcard\" where customer_id = '" + customer + "' and product_id = ?";
        Connection con = getDatabaseConnection();
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, product_id);
        affectedrows = pstmt.executeUpdate();
        if (affectedrows > 0) {

            System.out.println("Product ID '" + product_id + "' is deleted successfully!");
            isProductDeleted = true;
        }

        pstmt.close();
        con.close();
    }
    public void resetShoppingCard(String customer) throws Exception {

        String query = "delete from \"ose_shoppingcard\" where customer_id ='" + customer + "';";
        Connection con = getDatabaseConnection();
        Statement stmt = con.createStatement();

        int rs = stmt.executeUpdate(query);

        if (rs != 0) {
            System.out.println("Shopping card is now reseted!");
        }

        stmt.close();
        con.close();
    }

    //============================ ======== =============================//
    //============================ UNSORTED =============================//
    //============================ ======== =============================//




}