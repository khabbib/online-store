package view;

import control.Controller;
import model.Color;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class UserInterface {

    // Start controller
    private Controller controller;
    private Color color;

    // Those boolean flags are used to: log in/out and be able to go 'back'
    private boolean loggedInAsAdmin = false;
    private boolean loggedInAsCustomer = false;

    // These strings records any logged-in user.
    private String loginAdminId; // IMPORTANT: Admin cannot use to add to cart/order !!!!
    private String loginCustomerId;

    //============================ HEAD MENU ACTION =============================//
    public void menuMain(){
        System.out.println( Color.LIGHT_PINK + "****************************************************");
        System.out.println("==================" + Color.GREEN_BOLD_BRIGHT + " ONLINE STORE " + Color.LIGHT_PINK +"====================");
        System.out.println("****************************************************" + Color.RESET);
        controller = new Controller();
        try {
            while(true){
                menuAltMain();
                int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE + "Chose alt:"));
                switch(userInput) {
                    case 1: menuShowAndSearchProduct(); break;
                    case 2: menuLogin(); break;
                    case 3: register(); break;
                    case 4: System.out.println("Come back later!"); System.exit(0); break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Error!");
        }
    }

    //============================ SEARCH PRODUCT RELATED MENU ACTIONS =============================//
    private void menuShowAndSearchProduct() throws Exception {
        menuAltProduct();
        while (true){
            int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE + "Chose alt:" + Color.RESET));

            switch(userInput) {
                case 1: controller.getAllCategories();
                    //controller = new Controller();
                    int valdeCategory = Integer.parseInt(getUserInput(Color.PURPLE + "Which category ID?" + Color.RESET));
                    boolean ok = controller.finalStepShowProduct(valdeCategory);
                    if(ok){
                        System.out.println("Product found!");
                        menuShowAndSearchProduct();
                    }else {
                        System.out.println("Product not found...");
                    }

                    break;

                case 2: searchProductMenu(); break;
                case 3:
                    //This is a back menu for admin and customer, is used in different way
                    if(loggedInAsAdmin) {admin();}
                    else if(loggedInAsCustomer) {customer();}
                    else {menuMain();} break;
            }
        }
    }

    private void searchProductMenu() throws Exception {

        menuSearch();
        boolean ok = false;

        int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE + "Chose alt:" + Color.RESET));

        switch (userInput){
            case 1: String keyWord  = getUserInput("Product name:");
                ok = controller.searchProduct(keyWord, -1, -1,-1); break;
            case 2: int keyPrice  = Integer.parseInt(getUserInput("Price: "));
                ok = controller.searchProduct(null,keyPrice, -1,-1);break;
            case 3: int supId = Integer.parseInt(getUserInput("Supplier id: "));
                ok = controller.searchProduct(null,-1,supId, -1); break;
            case 4: int category = Integer.parseInt(getUserInput("Category id: "));
                ok = controller.searchProduct(null,-1,-1,category); break;
            case 5: menuShowAndSearchProduct(); break;
        }
        if (ok){
            System.out.println();
            System.out.println(Color.GREEN +"msg => Items found..."+ Color.RESET);
            searchProductMenu();
        }else {
            System.out.println(Color.RED +"msg => No items found..." + Color.RESET);
            searchProductMenu();
        }

    }

    //============================ LOGIN MENU ACTION =============================//
    private void menuLogin() throws Exception {
        while (true){

            menuAltLogin();

            int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE +"Chose alt:"+ Color.RESET));


            switch(userInput) {
                case 1: System.out.println("You chose to login as a USER."); loginCustomer(); break;
                case 2: System.out.println("You chose to login as an ADMIN."); loginAdmin(); break;
                case 3: menuMain(); break;
            }
        }
    }

    //============================ REGISTRATION AND LOGIN ACTION =============================//
    private void register() throws Exception {

        while(true){
            String firstname = getUserInput("Firstname:");
            String lastname = getUserInput("Lastname:");
            String email = getUserInput("Email:");
            String address = getUserInput("Address:");
            String city = getUserInput("City:");
            String country = getUserInput("Country:");
            String phoneNumber = getUserInput("Phone Number:");
            String password = getUserInput("Create password:");

            if (firstname != null && lastname != null && email != null && address != null && city != null && country != null && phoneNumber != null && password != null){
                Boolean ok = controller.authenticationRegisterCustomer(firstname, lastname, email, address, city, country, phoneNumber, password);
                if (ok){
                    System.out.println(Color.GREEN +"Registration for customer completed!");
                    menuMain();
                }
                else {
                    System.out.println(Color.ORANGE +"Email already exists!"+ Color.RESET);
                    menuMain();
                }
            }
        }
    }
    private void loginCustomer() throws Exception {
        while (true){
            String customerEmail = getUserInput(Color.PURPLE +"Email:");
            String pwd = getUserInput(Color.PURPLE +"Password:");

            if (customerEmail != null && pwd != null){
                Boolean ok = controller.authenticationLoginCustomer(customerEmail, pwd);
                if (ok){
                    System.out.println(Color.GREEN +"Logged in as Customer!");
                    loginCustomerId = Integer.toString(controller.getCustomerIdByEmail(customerEmail));
                    customer();
                }
                else {
                    System.out.println(Color.RED +"Error 101: Invalid username or password!" + Color.RESET);
                    menuMain();
                }
            }
        }
    }
    private void loginAdmin() throws Exception {
        while (true){
            int adminId = Integer.parseInt(getUserInput(Color.PURPLE +"ID:"));
            String pwd = getUserInput(Color.PURPLE +"Password:");

            if (adminId != 0 && pwd != null){
                Boolean ok = controller.validateLogin(adminId, pwd);
                if (ok){
                    System.out.println(Color.GREEN +"Logged in as Admin!");
                    loginAdminId = Integer.toString(adminId);
                    admin();
                }
                else {
                    System.out.println(Color.RED +"Error 101: Invalid username or password!" + Color.RESET);
                    menuMain();
                }
            }
        }
    }

    //============================ ADMIN AND CUSTOMER MENU ACTION =============================//
    private void admin() throws Exception {
        while (true){
            menuAltAdmin();
            loggedInAsAdmin = true;

            int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE +"Chose alt:"+ Color.RESET));

            switch (userInput) {
                case 1:
                    System.out.println("You choose to show & search product!");
                    menuShowAndSearchProduct();
                    break;

                case 2:
                    System.out.println("You choose to add discount!");
                    String discountCode = getUserInput("Discount code:");
                    String discountName = getUserInput("Discount name:");
                    int discountPercentage = Integer.parseInt(getUserInput("Discount percentage:"));
                    String discountStartDate = getUserInput("Discount start date:");
                    String discoundEndDate = getUserInput("Discount end date:");
                    boolean okToAddDiscount = controller.addDiscount(discountCode, discountName, discountPercentage, discountStartDate, discoundEndDate);
                    if (okToAddDiscount){
                        System.out.println("Discount added!");
                    }else {
                        System.out.println("Product not added!");
                    }
                    break;

                case 3:
                    System.out.println("You choose to add supplier!");
                    String supplierFirstName = getUserInput("Supplier firstname:");
                    String supplierLastName = getUserInput("Supplier lastname:");
                    String supplierEmail = getUserInput("Supplier email:");
                    String supplierAddress = getUserInput("Supplier address:");
                    String supplierCity = getUserInput("Supplier city:");
                    String supplierCountry = getUserInput("Supplier country:");
                    String supplierPhoneNumber = getUserInput("Supplier phone number:");
                    boolean okToAddSupplier = controller.addSupplier(supplierFirstName, supplierLastName, supplierEmail, supplierAddress, supplierCity, supplierCountry, supplierPhoneNumber);
                    if (okToAddSupplier){
                        System.out.println("Supplier added!");
                    }else {
                        System.out.println("Supplier NOT added!");
                    }
                    break;

                case 4:
                    System.out.println("You choose to add product!");
                    String productName = getUserInput("Product name:");
                    int categoryId = Integer.parseInt(getUserInput("Category id:"));
                    int productBasePrice = Integer.parseInt(getUserInput("Product base price:"));
                    int product_quantity = Integer.parseInt(getUserInput("Product quantity: "));
                    int supplier_id = Integer.parseInt(getUserInput("Supplier ID: "));
                    boolean okToAddProduct = controller.addProduct(productName, categoryId, productBasePrice, product_quantity, supplier_id);
                    if (okToAddProduct){
                        System.out.println("Product added!");
                    }else {
                        System.out.println("Product not added!");
                    }
                    break;

                case 5:
                    System.out.println("You choose to assign discount to product!");
                    int product_id = Integer.parseInt(getUserInput("Product ID:"));
                    int discount_id = Integer.parseInt(getUserInput("Discount ID:"));
                    controller.assignDiscountToProduct(product_id, discount_id);
                    break;

                case 6:
                    System.out.println("You choose to see discount history!");
                    controller.getDiscountHistory();
                    break;

                case 7:
                    System.out.println("You choose to update product quantity!");
                    product_id = Integer.parseInt(getUserInput("Product ID:"));
                    int quantity = Integer.parseInt(getUserInput("Quantity:"));
                    controller.updateProductQuantity(product_id,quantity);
                    break;

                case 8:
                    System.out.println("You choose to add a discount to a product!");
                    product_id = Integer.parseInt(getUserInput("Product ID:"));
                    discount_id = Integer.parseInt(getUserInput("Discount ID:"));
                    controller.updateDiscount(product_id,discount_id);
                    break;

                case 9:
                    System.out.println("You choose stock list!");
                    controller.getStock();
                    break;

                case 10:
                    System.out.println("You choose to delete product!");
                    int productId = Integer.parseInt(getUserInput("Which ID?"));
                    boolean okToDeleteAProduct = controller.deleteProduct(productId);
                    if (okToDeleteAProduct){
                        System.out.println("Deleted successfully!");
                    }else {
                        System.out.println("Could not delete!");
                    }
                    break;

                case 11:
                    System.out.println("You choose orders!");
                    controller.seeOrdersAdmin();
                    break;

                case 12:
                    System.out.println("You choose confirm an order!");
                    int order_id = Integer.parseInt(getUserInput("Order ID:"));
                    controller.confirmAnOrder(order_id);
                    break;

                case 13:
                    System.out.println("You choose products with max orders/month");
                    // TO DO
                    break;

                case 14:
                    System.out.println("Log outs!");
                    loggedInAsAdmin = false;
                    loginAdminId = null;
                    menuMain();
                    break;
            }
        }
    }
    private void customer() throws Exception {
        while (true){

            menuAltCustomer();

            loggedInAsCustomer = true;

            int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE +"Chose alt:" + Color.RESET));


            switch (userInput) {
                case 1:
                    System.out.println("You choose to show & search product");
                    menuShowAndSearchProduct();
                    break;

                case 2:
                    System.out.println("You choose shopping/order!");
                    menuShoppingAndOrder();
                    break;

                case 3:
                    System.out.println("Log outs!");
                    loggedInAsCustomer = false;
                    menuMain();
                    break;
            }
        }
    }

    private void menuShoppingAndOrder() throws Exception {

        while(true) {
            shoppingMenu();

            int userInput = Integer.parseInt(getUserInput("" + Color.PURPLE + "Chose alt:" + Color.RESET));

            switch (userInput){
                case 1:
                    System.out.println("You choose to add product to shopping card!");
                    int product_id = Integer.parseInt(getUserInput("Product ID:"));
                    int quantity = Integer.parseInt(getUserInput("Quantity:"));
                    controller.addToCart(product_id, quantity, loginCustomerId);
                    break;

                case 2:
                    System.out.println("You choose to see shopping card!");
                    controller.seeCart(loginCustomerId);
                    break;

                case 3:
                    System.out.println("Order completed! Waiting to be confirmed...");
                    controller.order(loginCustomerId);
                    break;

                case 4:
                    System.out.println("You choose to see orders!");
                    // TO DO
                    controller.seeOrderCustomer(loginCustomerId);
                    break;

                case 5:
                    System.out.println("You choose to delete product from shopping list!");
                    product_id = Integer.parseInt(getUserInput("Product ID:"));
                    controller.deleteProductFromShoppingCard(loginCustomerId, product_id);
                    break;

                case 6:
                    System.out.println("Shopping card is now reseted!");
                    controller.resetShoppingCard(loginCustomerId);
                    break;

                case 7:
                    System.out.println("You choose to delete order (if order isn't confirmed yet!)");
                    // TO DO
                    int order_id = Integer.parseInt(getUserInput("Order ID:"));
                    controller.cancelOrder(order_id, loginCustomerId);
                    break;

                case 8:
                    //This is a back menu for admin and customer, is used in different way
                    if(loggedInAsAdmin) {admin();}
                    else if(loggedInAsCustomer) {customer();}
                    else {menuMain();} break;
            }
        }
    }

    //============================ MENUS DISPLAYS AND VIEWS =============================//
    // This menu is main menu
    private void menuAltMain() {
        System.out.println("===" + Color.ORANGE +" MAIN MENU "+ Color.RESET +"======================================");
        System.out.println("[1] - Show & Search Product");
        System.out.println("[2] - Login");
        System.out.println("[3] - Register");
        System.out.println("[4] - Exit");
        System.out.println("====================================================");
        System.out.println();
    }

    // This menu is when you chose login type
    private void menuAltLogin() {
        System.out.println();
        System.out.println("===" + Color.ORANGE +" LOGIN MENU "+ Color.RESET +"=====================================");
        System.out.println("[1] - Customer");
        System.out.println("[2] - Admin");
        System.out.println("[3] - Back");
        System.out.println("====================================================");
        System.out.println();
    }

    // This menu is for admin
    private void menuAltAdmin() {
        System.out.println();
        System.out.println("===" + Color.ORANGE +" ADMIN MENU "+ Color.RESET +"=====================================");
        System.out.println("============================");
        System.out.println("[alt] - Name of alt                     [depends]");
        System.out.println("============================");
        System.out.println("[1] - Show & Search Product             [none]");
        System.out.println("[2] - Add discount                      [none]");
        System.out.println("[3] - Add supplier                      [none]");
        System.out.println("[4] - Add product                       [category, supplier]");
        System.out.println("[5] - Assign discount to product        [discount, product]");
        System.out.println("[6] - List of discount history          [discount]");
        System.out.println("============================");
        System.out.println("[7] - Update product quantity           [product]");
        System.out.println("[8] - Update discount to product        [discount, product]");
        System.out.println("[9] - Stock list                        [product]");
        System.out.println("[10] - Delete product                   [product]");
        System.out.println("============================");
        System.out.println("[11] - List of orders                   [order]");
        System.out.println("[12] - Confirm an order                 [order]");
        System.out.println("[13] - Products with max orders/month   [none?]");
        System.out.println("============================");
        System.out.println("[14] - Log out                          [none]");

        System.out.println("====================================================");
        System.out.println();
    }

    // This menu is for customer
    private void menuAltCustomer() {
        System.out.println();
        System.out.println("===" + Color.ORANGE +" CUSTOMER MENU "+ Color.RESET +"===================================");
        System.out.println("[1] - Show & Search Product");
        System.out.println("[2] - Shopping & Order");
        System.out.println("[3] - Log out");
        System.out.println("====================================================");
        System.out.println();
    }

    // This menu is to see shopping menu (ONLY FOR CUSTOMERS!)
    private void shoppingMenu() {
        System.out.println("[1] - Add product to shopping card");
        System.out.println("[2] - See shopping card");
        System.out.println("[3] - Order");
        System.out.println("[4] - See orders");
        System.out.println("[5] - Delete product from shopping card");
        System.out.println("[6] - Reset shopping card");
        System.out.println("[7] - Cancel (Delete) an order (if order isn't confirmed yet!)");
        System.out.println("[8] - Back");
    }

    // These two menus are for search products
    private void menuAltProduct() {
        System.out.println();
        System.out.println("===" + Color.ORANGE +" SEARCH MENU "+ Color.RESET +"====================================");
        System.out.println("[1] - Show product's category");
        System.out.println("[2] - Search product");
        System.out.println("[3] - Back");
        System.out.println("[4] - add product to cart");
        System.out.println("====================================================");
        System.out.println();
    }
    private void menuSearch() {
        System.out.println();
        System.out.println("===" + Color.ORANGE +" SEARCH MENU "+ Color.RESET +"====================================");
        System.out.println("[1] - Search by name");
        System.out.println("[2] - Search by price");
        System.out.println("[3] - Search by supplier id");
        System.out.println("[4] - Search by category id");
        System.out.println("[5] - Back");
        System.out.println("====================================================");
        System.out.println();
    }

    //============================ OTHER =============================//
    // This is algorithm to hash password (not in use)
    private String hashPassword(String password) {

        String generatedPassword = null;

        try
        {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add password bytes to digest
            md.update(password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // This bytes[] has bytes in decimal format. Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //System.out.println(generatedPassword);

        return generatedPassword;
    }

    // This is where all input goes to the user
    private String getUserInput(String msg) {
        Scanner myObj = new Scanner(System.in);
        if (msg != null)
            System.out.println(msg);

        String input = myObj.nextLine();
        return input;
    }
}
