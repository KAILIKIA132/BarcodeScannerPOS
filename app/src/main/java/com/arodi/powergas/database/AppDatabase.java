package com.arodi.powergas.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.arodi.powergas.daos.InvoiceDAO;
import com.arodi.powergas.daos.ChequeDAO;
import com.arodi.powergas.entities.ChequeEntity;
import com.arodi.powergas.entities.InvoiceEntity;


@Database(entities = {InvoiceEntity.class, ChequeEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InvoiceDAO invoiceDAO();
    public abstract ChequeDAO chequeDAO();
}