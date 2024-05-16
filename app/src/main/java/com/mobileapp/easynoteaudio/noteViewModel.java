package com.mobileapp.easynoteaudio;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class noteViewModel extends ViewModel {
    private String title;
    private String content;
    boolean isBulletListModeActive = false;
    public noteViewModel(){
        title="";
        content="";

    }
    public noteViewModel(String title, String content) {
        this.title=title;
        this.content=content;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
   /*
        Sets the title of Note.
        @param title Title to set.
     */
    public void setTitle(String newTitle) {
        this.title=newTitle;
    }
    /*
         Sets the content of Note.
         @param content Content to set.
      */
    public void setContent(String content) {
        this.content=(content);
    }



    /*
        Toggles bullet list mode in the current note.
     */
    void toggleBulletListMode() {
        isBulletListModeActive = !isBulletListModeActive;
    }



}
