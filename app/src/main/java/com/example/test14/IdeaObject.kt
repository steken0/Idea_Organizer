package com.example.test14

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Primary Data object wich stores all ideas
 * Using room, it automaticly creates
 */

@Entity(tableName = "IdeaObjects")
data class IdeaObject(@ColumnInfo(name = "text") var text: String?,
                      @ColumnInfo(name = "category") var category: String?,
                      @ColumnInfo(name = "img") var img: String?,
                      @ColumnInfo(name = "rcd") var rcd: String?,
                      @ColumnInfo(name = "completed") var completed: Boolean)
{
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int = 0;

}