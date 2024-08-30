package com.arodi.powergas.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arodi.powergas.entities.ChequeEntity;

import java.util.List;

@Dao
public interface ChequeDAO {
    @Query("SELECT * FROM ChequeEntity")
    List<ChequeEntity> getAll();

    @Query("SELECT * FROM ChequeEntity WHERE shop_name like '%' || :string || '%' OR phone like '%' || :string || '%'")
    List<ChequeEntity> searchInvoice(String string);

    @Query("DELETE FROM ChequeEntity")
    void deleteAll();

    @Insert
    void insert(ChequeEntity route);

    @Delete
    void delete(ChequeEntity route);

    @Update
    void update(ChequeEntity route);
}
