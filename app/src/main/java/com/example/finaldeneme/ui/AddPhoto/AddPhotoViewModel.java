package com.example.finaldeneme.ui.AddPhoto;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddPhotoViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public AddPhotoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is addphoto fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
