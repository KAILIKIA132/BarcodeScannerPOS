package com.arodi.powergas.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.arodi.powergas.models.CustomerModel;
import com.arodi.powergas.models.ExpenditureModel;
import com.arodi.powergas.models.HistoryModel;
import com.arodi.powergas.models.PeopleModel;
import com.arodi.powergas.models.ProductModel;
import com.arodi.powergas.models.ProductQuantity;
import com.arodi.powergas.models.SaleModel;
import com.arodi.powergas.models.StockModel;
import com.arodi.powergas.models.StoreModel;
import com.arodi.powergas.models.TodayModel;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {
    public final static String DB_NAME = "gas.db";
    public final static int DB_VER = 2;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public JSONArray fetchLatLng() {
        SQLiteDatabase db = this.getReadableDatabase();
        final JSONArray jsonArray = new JSONArray();

        Cursor res = db.rawQuery("SELECT lat, lng FROM customers WHERE lat !=? AND lng !=?", new String[]{"0", "0"});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lng", res.getString(res.getColumnIndex("lng")));
                    jsonObject.put("lat", res.getString(res.getColumnIndex("lat")));
                    jsonArray.put(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return jsonArray;
    }

    public List<PeopleModel> search_people(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<PeopleModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM people WHERE  name LIKE? OR plate_no LIKE?", new String[]{'%' + s + '%', '%' + s + '%'});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new PeopleModel(
                        res.getString(res.getColumnIndex("id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("email")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("plate_no")),
                        res.getString(res.getColumnIndex("distributor_id")),
                        res.getString(res.getColumnIndex("vehicle_id")),
                        res.getString(res.getColumnIndex("route_id")),
                        res.getString(res.getColumnIndex("route_name"))));

            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return list;
    }

    public List<CustomerModel> search_customers(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<CustomerModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM customers WHERE  shop_name LIKE? OR phone LIKE?", new String[]{'%' + s + '%', '%' + s + '%'});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new CustomerModel(
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("county_name")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("email")),
                        res.getString(res.getColumnIndex("logo")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("customer_group_id")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("town_name")),
                        res.getString(res.getColumnIndex("town_id")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("distance"))));

            }
            while (res.moveToNext());

        }
        db.close();
        res.close();
        return list;
    }

    public List<HistoryModel> search_history(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM history WHERE  shop_name LIKE? OR phone LIKE?", new String[]{'%' + s + '%', '%' + s + '%'});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));


            }
            while (res.moveToNext());

        }
        db.close();
        res.close();
        return list;
    }

    public List<HistoryModel> search_discount(String s) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM discount WHERE  shop_name LIKE? OR phone LIKE?", new String[]{'%' + s + '%', '%' + s + '%'});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));


            }
            while (res.moveToNext());

        }
        db.close();
        res.close();
        return list;
    }

    public void create_history(HistoryModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = String.format("INSERT INTO history(sale_id, date, customer, customer_id, payment_status, grand_total, products, shop_name, lat, lng, image, phone, customer_group_name, city, shop_id, payments, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                model.getSale_id(),
                model.getDate(),
                model.getCustomer(),
                model.getCustomer_id(),
                model.getPayment_status(),
                model.getGrand_total(),
                model.getProducts(),
                model.getShop_name(),
                model.getLat(),
                model.getLng(),
                model.getImage(),
                model.getPhone(),
                model.getCustomer_group_name(),
                model.getCity(),
                model.getShop_id(),
                model.getPayments(),
                model.getUpdated_at());

        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void create_discount(HistoryModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = String.format("INSERT INTO discount (sale_id, date, customer, customer_id, payment_status, grand_total, products, shop_name, lat, lng, image, phone, customer_group_name, city, shop_id, payments, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                model.getSale_id(),
                model.getDate(),
                model.getCustomer(),
                model.getCustomer_id(),
                model.getPayment_status(),
                model.getGrand_total(),
                model.getProducts(),
                model.getShop_name(),
                model.getLat(),
                model.getLng(),
                model.getImage(),
                model.getPhone(),
                model.getCustomer_group_name(),
                model.getCity(),
                model.getShop_id(),
                model.getPayments(),
                model.getUpdated_at());

        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void create_customer(CustomerModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM customers WHERE shop_id =?", new String[]{model.getShop_id()});

        if (cursor.getCount() == 0) {
            String query = String.format("INSERT INTO customers(customer_id, name, customer_group_name, county_name, phone, email, logo,lat, lng, customer_group_id, shop_name, town_name, town_id, shop_id, distance) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                    model.getCustomer_id(),
                    model.getName(),
                    model.getCustomer_group_name(),
                    model.getCounty_name(),
                    model.getPhone(),
                    model.getEmail(),
                    model.getLogo(),
                    model.getLat(),
                    model.getLng(),
                    model.getCustomer_group_id(),
                    model.getShop_name(),
                    model.getTown_name(),
                    model.getTown_id(),
                    model.getShop_id(),
                    model.getDistance());

            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.close();
        }
        cursor.close();
    }

    public void create_expense(ExpenditureModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = String.format("INSERT INTO expense(expense_id, date, company_id, reference, amount, approved, note, status) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                model.getExpense_id(),
                model.getDate(),
                model.getCompany_id(),
                model.getReference(),
                model.getAmount(),
                model.getApproved(),
                model.getNote(),
                model.getStatus());

        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void create_product(ProductModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM products WHERE  product_id=?", new String[]{model.getProduct_id()});
        if (cursor.getCount() == 0) {
            String query = String.format("INSERT INTO products(product_id, product_code, product_name, price, quantity, plate_no, discount_enabled, target, portion1, portion1qty, portion2, portion2qty, portion3, portion3qty, portion4, portion4qty, portion5, portion5qty, isKitchen) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                    model.getProduct_id(),
                    model.getProduct_code(),
                    model.getProduct_name(),
                    model.getPrice(),
                    model.getQuantity(),
                    model.getPlate_no(),
                    model.getDiscount_enabled(),
                    model.getTarget(),
                    model.getPortion1(),
                    model.getPortion1qty(),
                    model.getPortion2(),
                    model.getPortion2qty(),
                    model.getPortion3(),
                    model.getPortion3qty(),
                    model.getPortion4(),
                    model.getPortion4qty(),
                    model.getPortion5(),
                    model.getPortion5qty(),
                    model.getIsKitchen());

            sqLiteDatabase.execSQL(query);
        }
        cursor.close();
        sqLiteDatabase.close();
    }

    public void create_people(PeopleModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = String.format("INSERT INTO people(id, name, email, phone, plate_no, distributor_id, vehicle_id, route_id, route_name) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getPhone(),
                model.getPlate_no(),
                model.getDistributor_id(),
                model.getVehicle_id(),
                model.getRoute_id(),
                model.getRoute_name());

        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void create_store(StoreModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = String.format("INSERT INTO store(id, product_name, product_quantity, product_id, product_price) VALUES ('%s', '%s', '%s', '%s', '%s');",
                model.getId(),
                model.getProduct_name(),
                model.getProduct_quantity(),
                model.getProduct_id(),
                model.getProduct_price());
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void create_today(TodayModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM today WHERE  product_id=?", new String[]{model.getProduct_id()});
        if (cursor.getCount() == 0) {
            String query = String.format("INSERT INTO today(product_id, name, quantity, total) VALUES ('%s', '%s', '%s', '%s');",
                    model.getProduct_id(),
                    model.getName(),
                    model.getQuantity(),
                    model.getTotal());

            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.close();
        }
        cursor.close();
    }

    public List<TodayModel> fetch_today() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<TodayModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM today", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new TodayModel(
                        res.getString(res.getColumnIndex("product_id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("quantity")),
                        res.getString(res.getColumnIndex("total"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<StoreModel> fetch_store() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<StoreModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM store", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new StoreModel(
                        res.getString(res.getColumnIndex("id")),
                        res.getString(res.getColumnIndex("product_name")),
                        res.getString(res.getColumnIndex("product_quantity")),
                        res.getString(res.getColumnIndex("product_id")),
                        res.getString(res.getColumnIndex("product_price"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<PeopleModel> fetch_people() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<PeopleModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM people", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new PeopleModel(
                        res.getString(res.getColumnIndex("id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("email")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("plate_no")),
                        res.getString(res.getColumnIndex("distributor_id")),
                        res.getString(res.getColumnIndex("vehicle_id")),
                        res.getString(res.getColumnIndex("route_id")),
                        res.getString(res.getColumnIndex("route_name"))
                ));
            }
            while (res.moveToNext());
        }

        res.close();
        db.close();
        return list;
    }


    public void create_product_quantity(ProductQuantity model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM product_quantity WHERE  product_id=?", new String[]{model.getProduct_id()});
        if (cursor.getCount() == 0) {
            String query = String.format("INSERT INTO product_quantity(product_id, quantity)VALUES ('%s', '%s');",
                    model.getProduct_id(),
                    model.getQuantity());

            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.close();
        }
        cursor.close();
    }

    public String create_sale(SaleModel model) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String response = null;
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM sale WHERE product_id=? AND customer_id=?", new String[]{model.getProduct_id(), model.getCustomer_id()});

        if (cursor.getCount() == 0) {
            ContentValues newValues = new ContentValues();
            newValues.put("product_id", model.getProduct_id());
            newValues.put("code", model.getCode());
            newValues.put("name", model.getName());
            newValues.put("price", model.getPrice());
            newValues.put("quantity", model.getQuantity());
            newValues.put("total", model.getTotal());
            newValues.put("customer_id", model.getCustomer_id());
            newValues.put("discount", model.getDiscount());

            long result = sqLiteDatabase.insert("sale", null, newValues);
            if (result == -1) {
                response = "Selected product already exist";
            } else {
                response = "Product added";
            }


        } else response = "Selected product already exist!";

        sqLiteDatabase.close();
        cursor.close();
        return response;
    }

    public List<CustomerModel>  fetch_customers() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<CustomerModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM customers", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new CustomerModel(
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("county_name")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("email")),
                        res.getString(res.getColumnIndex("logo")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("customer_group_id")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("town_name")),
                        res.getString(res.getColumnIndex("town_id")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("distance"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<HistoryModel> fetch_history(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM history WHERE id>? LIMIT 10", new String[]{String.valueOf(position)});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));
            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return list;
    }

    public List<HistoryModel> fetch_discount(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM discount WHERE id>? LIMIT 10", new String[]{String.valueOf(position)});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));
            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return list;
    }

    public List<HistoryModel> fetch_all_history() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM history", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));
            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return list;
    }

    public List<HistoryModel> fetch_all_discount() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<HistoryModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM discount", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new HistoryModel(
                        res.getString(res.getColumnIndex("sale_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("customer")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("payment_status")),
                        res.getString(res.getColumnIndex("grand_total")),
                        res.getString(res.getColumnIndex("products")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("image")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("city")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("payments")),
                        res.getString(res.getColumnIndex("updated_at"))));
            }
            while (res.moveToNext());

        }
        res.close();
        db.close();
        return list;
    }

    public List<CustomerModel> fetch_all_customers() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<CustomerModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM customers", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new CustomerModel(
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("customer_group_name")),
                        res.getString(res.getColumnIndex("county_name")),
                        res.getString(res.getColumnIndex("phone")),
                        res.getString(res.getColumnIndex("email")),
                        res.getString(res.getColumnIndex("logo")),
                        res.getString(res.getColumnIndex("lat")),
                        res.getString(res.getColumnIndex("lng")),
                        res.getString(res.getColumnIndex("customer_group_id")),
                        res.getString(res.getColumnIndex("shop_name")),
                        res.getString(res.getColumnIndex("town_name")),
                        res.getString(res.getColumnIndex("town_id")),
                        res.getString(res.getColumnIndex("shop_id")),
                        res.getString(res.getColumnIndex("distance"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public Integer fetch_product_quantity(String product_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        int response = 0;
        Cursor res = db.rawQuery("SELECT quantity FROM product_quantity WHERE product_id=? LIMIT 1", new String[]{product_id});
        res.moveToFirst();

        if (res.moveToFirst() && res.getCount() >= 1) {
            try {
                response = Integer.parseInt(res.getString(res.getColumnIndex("quantity")));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        res.close();
        db.close();
        return response;
    }

    public Integer fetch_outright_price(String product_id){
        SQLiteDatabase db = this.getReadableDatabase();
        int product_price = 0;

        Cursor res = db.rawQuery("SELECT price FROM products WHERE product_id=? LIMIT 1", new String[]{product_id});
        res.moveToFirst();
        if (res.moveToFirst() && res.getCount() >= 1) {
            try {
                product_price = Integer.parseInt(res.getString(res.getColumnIndex("price")));
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        res.close();
        db.close();

        return  product_price;
    }


    public List<ProductModel> fetch_products() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<ProductModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM products", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new ProductModel(
                        res.getString(res.getColumnIndex("product_id")),
                        res.getString(res.getColumnIndex("product_code")),
                        res.getString(res.getColumnIndex("product_name")),
                        res.getString(res.getColumnIndex("price")),
                        res.getString(res.getColumnIndex("quantity")),
                        res.getString(res.getColumnIndex("plate_no")),
                        res.getString(res.getColumnIndex("discount_enabled")),
                        res.getString(res.getColumnIndex("target")),
                        res.getString(res.getColumnIndex("portion1")),
                        res.getString(res.getColumnIndex("portion1qty")),
                        res.getString(res.getColumnIndex("portion2")),
                        res.getString(res.getColumnIndex("portion2qty")),
                        res.getString(res.getColumnIndex("portion3")),
                        res.getString(res.getColumnIndex("portion3qty")),
                        res.getString(res.getColumnIndex("portion4")),
                        res.getString(res.getColumnIndex("portion4qty")),
                        res.getString(res.getColumnIndex("portion5")),
                        res.getString(res.getColumnIndex("portion5qty")),
                        res.getString(res.getColumnIndex("isKitchen"))

                ));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<StockModel> fetch_stock() {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<StockModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT products.product_id, products.product_code, products.product_name, products.price, products.target, product_quantity.quantity FROM products INNER JOIN product_quantity ON products.product_id = product_quantity.product_id", new String[]{});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new StockModel(
                        res.getString(res.getColumnIndex("product_id")),
                        res.getString(res.getColumnIndex("product_name")),
                        res.getString(res.getColumnIndex("quantity")),
                        res.getString(res.getColumnIndex("target")),
                        res.getString(res.getColumnIndex("price")),
                        res.getString(res.getColumnIndex("product_code"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<SaleModel> fetch_sales(String customer_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<SaleModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM sale WHERE customer_id=?", new String[]{customer_id});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new SaleModel(
                        res.getString(res.getColumnIndex("product_id")),
                        res.getString(res.getColumnIndex("code")),
                        res.getString(res.getColumnIndex("name")),
                        res.getString(res.getColumnIndex("price")),
                        res.getString(res.getColumnIndex("quantity")),
                        res.getString(res.getColumnIndex("total")),
                        res.getString(res.getColumnIndex("customer_id")),
                        res.getString(res.getColumnIndex("discount"))));
            }

            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }

    public List<ExpenditureModel> fetch_expense(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        final List<ExpenditureModel> list = new ArrayList<>();

        Cursor res = db.rawQuery("SELECT * FROM expense WHERE id>? LIMIT 10", new String[]{String.valueOf(position)});
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                list.add(new ExpenditureModel(
                        res.getString(res.getColumnIndex("expense_id")),
                        res.getString(res.getColumnIndex("date")),
                        res.getString(res.getColumnIndex("company_id")),
                        res.getString(res.getColumnIndex("reference")),
                        res.getString(res.getColumnIndex("amount")),
                        res.getString(res.getColumnIndex("approved")),
                        res.getString(res.getColumnIndex("note")),
                        res.getString(res.getColumnIndex("status"))));
            }
            while (res.moveToNext());
        }
        res.close();
        db.close();
        return list;
    }


    public void clear_customers() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"customers"});
        String query = "DELETE FROM customers";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_products() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"products"});
        String query = "DELETE FROM products";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_today() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"today"});
        String query = "DELETE FROM today";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_store() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"store"});
        String query = "DELETE FROM store";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_people() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"people"});
        String query = "DELETE FROM people";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_expense() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"expense"});
        String query = "DELETE FROM expense";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_customer_sales(String customer_id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("sale", "customer_id=?", new String[]{customer_id});
        sqLiteDatabase.close();
    }

    public void clear_single_sale(String product_id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("sale", "product_id=?", new String[]{product_id});
        sqLiteDatabase.close();
    }


    public void clear_product_quantity() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"product_quantity"});
        String query = "DELETE FROM product_quantity";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_history() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"history"});
        String query = "DELETE FROM history";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    public void clear_discount() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.delete("SQLITE_SEQUENCE", "NAME=?", new String[]{"discount"});
        String query = "DELETE FROM discount";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }


}
