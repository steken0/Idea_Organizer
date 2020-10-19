package com.example.test14

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface CategoryDao {
    //Livedata ensures that the program updates when new data is added, even for offline-applikations like this
    @Query("SELECT * FROM CategoryObjects")
    fun getCategories(): LiveData<List<CategoryObject>>

    //On conflict Strategy makes it ignore any new object that are exactly the same as one already on the list
    //This prevents accidental duplication on creation. Safety first ;)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(categoryObject: CategoryObject)

    @Query("DELETE FROM CategoryObjects")
    suspend fun deleteAll()

    @Query("DELETE FROM CategoryObjects WHERE category = :category")
    fun deleteCategory(category: String);
}