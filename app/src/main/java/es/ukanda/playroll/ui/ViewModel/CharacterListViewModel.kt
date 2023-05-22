package es.ukanda.playroll.ui.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import es.ukanda.playroll.entyties.PartieEntities.CharacterEntity

class CharacterListViewModel: ViewModel()  {
    val characterListLiveData = MutableLiveData<List<CharacterEntity>>()
    val selectedCharactersLiveData = MutableLiveData<List<CharacterEntity>>()

    fun loadCharacterList() {

    }

    fun selectCharacter(character: CharacterEntity) {

    }
}