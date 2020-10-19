package com.example.test14

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This class is purely for data abstraction. This is usefull for when multiple datasources
 * might be used, for example both an external and a local. This will however only use a local
 */

class IdeaRepository(private val ideaDao: IdeaDao, private val categoryDao: CategoryDao)
{
    var categories : LiveData<List<CategoryObject>> = categoryDao.getCategories()

    suspend fun insert(ideaObject: IdeaObject) {
        ideaDao.insert(ideaObject)
    }
    suspend fun insert(categoryObject: CategoryObject) {
        categoryDao.insert(categoryObject)
    }

    //Only Called first time!!
    fun getIdeas(): MutableLiveData<List<IdeaObject>> {
        val data: MutableLiveData<List<IdeaObject>> = MutableLiveData<List<IdeaObject>>()
        data.setValue(ideaDao.getIdeas());
        return data;
    }

    fun filter(data: MutableLiveData<List<IdeaObject>>, filter: String) {
        data.setValue(ideaDao.getIFilteredIdeas(filter));
    }

    fun getAllIdeas(data: MutableLiveData<List<IdeaObject>>) {
        data.setValue(ideaDao.getIdeas());
    }

    suspend fun updateIdea(ideaObject: IdeaObject) {
        ideaDao.update(ideaObject)
    }
    fun getById(id: Int): IdeaObject {
        return ideaDao.getById(id)
    }

    suspend fun deleteItem(id: Int) {
        ideaDao.deleteItem(id)
    }
    fun deleteCategory(category: String) {
        ideaDao.deleteCategory(category);
        categoryDao.deleteCategory(category);
    }
}