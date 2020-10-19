package com.example.test14

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Primary Data object wich stores all ideas
 * Using room, it automaticly creates
 */

@Entity(tableName = "CategoryObjects")
data class CategoryObject(@PrimaryKey @ColumnInfo(name = "category") val category: String){

    override fun toString(): String {
        return category;
    }
}