package com.example.test14

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The viewmodel enables update on the fly when data changes
 * It also helps to defocus the UI from getting the infomration to just displaying it
 *
 */


class IdeaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IdeaRepository
    var ideas: MutableLiveData<List<IdeaObject>>
    var categories: LiveData<List<CategoryObject>>

    init {
        val database = IdeaDatabase.getDatabase(application)
        val ideaDao = database.IdeaDao()
        val categoryDao = database.CategoryDao()
        repository = IdeaRepository(ideaDao, categoryDao)
        ideas = repository.getIdeas()
        categories = repository.categories
    }

    //Isolating the insert function so it is not to interfere with the rest of the program
    fun insert(ideaObject: IdeaObject) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(ideaObject)
    }
    fun insert(categoryObject: CategoryObject) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(categoryObject)
    }

    fun getAllIdeas() {
        repository.getAllIdeas(ideas)
    }

    fun filter(filter: String) {
        repository.filter(ideas, filter)
    }
    fun getById(id: Int): IdeaObject {
        return repository.getById(id)
    }
    fun updateIdea(ideaObject: IdeaObject) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateIdea(ideaObject)
    }
    fun deleteItem(id: Int)  = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteItem(id);
    }
    fun deleteCategory(category: String){
        repository.deleteCategory(category);
    }
}