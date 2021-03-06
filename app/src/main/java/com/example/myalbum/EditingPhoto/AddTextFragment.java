package com.example.myalbum.EditingPhoto;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;


import com.divyanshu.colorseekbar.ColorSeekBar;
import com.example.myalbum.R;


public class AddTextFragment extends DialogFragment implements FragmentCallbacks{
    Context context = null;
    PhotoEditorHandler main;

    EditText editText;
    ColorSeekBar colorSeekBar;
    public Button button;

    private TextEditor mTextEditor;

    public interface TextEditor {
        void onDone(String inputText, int colorCode);
    }

    int selectedColor = 0;

    public static AddTextFragment newInstance(String input, int color)
    {
        AddTextFragment fragment = new AddTextFragment();
        Bundle args = new Bundle();
        args.putString("inputText", input);
        args.putInt("Color", color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try
        {
            context = getActivity();
            main = (PhotoEditorHandler) getActivity();
            //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        }
        catch (IllegalStateException e)
        {
            throw new IllegalStateException( "MainActivity must implement callbacks");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ConstraintLayout textLayout = (ConstraintLayout) inflater.inflate(R.layout.edit_text_fragment_layout, null);
        editText = (EditText) textLayout.findViewById(R.id.inputText);
        colorSeekBar = (ColorSeekBar) textLayout.findViewById(R.id.sbColor);
        button = (Button) textLayout.findViewById(R.id.btnDone);

        final Bundle bundle = getArguments();
        editText.setText(bundle.getString("inputText"));
        editText.setTextColor(bundle.getInt("Color"));
        selectedColor = bundle.getInt("Color");

        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                selectedColor = i;
                editText.setTextColor(i);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                String text = editText.getText().toString();
                bundle.putString("inputText",text);
                bundle.putInt("Color", selectedColor);
                main.onMsgFromFragToMain("TextFragment",bundle);
                mTextEditor.onDone(text, selectedColor);
                dismiss();
            }
        });



        return textLayout;
    }


    @Override
    public void onMsgFromMainToFragment(Bundle bundle) {

    }

    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }
}
