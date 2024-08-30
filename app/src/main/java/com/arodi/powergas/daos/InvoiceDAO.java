package com.arodi.powergas.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arodi.powergas.entities.InvoiceEntity;

import java.util.List;

@Dao
public interface InvoiceDAO {
    @Query("SELECT * FROM InvoiceEntity")
    List<InvoiceEntity> getAll();

    @Query("SELECT * FROM InvoiceEntity WHERE shop_name like '%' || :string || '%' OR phone like '%' || :string || '%'")
    List<InvoiceEntity> searchInvoice(String string);

    @Query("DELETE FROM InvoiceEntity")
    void deleteAll();

    @Insert
    void insert(InvoiceEntity route);

    @Delete
    void delete(InvoiceEntity route);

    @Update
    void update(InvoiceEntity route);
}
