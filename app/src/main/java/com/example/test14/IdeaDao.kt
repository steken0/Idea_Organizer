package com.example.test14

import androidx.room.*


/**
 * An interface to add functionality to the databe (add, remove, get)
 *
 */

@Dao
interface IdeaDao {

    //Livedata ensures that the program updates when new data is added, even for offline-applications like this
    @Query("SELECT * FROM IdeaObjects")
    fun getIdeas(): List<IdeaObject>

    @Query("SELECT * FROM IdeaObjects where category = :filter")
    fun getIFilteredIdeas(filter: String): List<IdeaObject>

    //On conflict Strategy makes it ignore any new object that are exactly the same as one already on the list
    //This prevents accidental duplication on creation. Safety first ;)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(IdeaObject: IdeaObject)

    @Query("DELETE FROM IdeaObjects")
    suspend fun deleteAll()

    @Query("DELETE FROM IdeaObjects WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("DELETE FROM IdeaObjects WHERE category = :category")
    suspend fun deleteCategory(category: String);

    @Update
    suspend fun update(IdeaObject: IdeaObject)

    @Query("SELECT * FROM IdeaObjects where id = :id")
    fun getById(id: Int): IdeaObject

}