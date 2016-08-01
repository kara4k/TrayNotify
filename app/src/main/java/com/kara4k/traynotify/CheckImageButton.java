package com.kara4k.traynotify;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class CheckImageButton extends ImageButton {

    private CustomOnClickListener mCustomOnClickListener;
    private boolean isChecked;
    private int backgroundOn;
    private int backgroundOff;
    private Drawable imageOn;
    private Drawable imageOff;

    boolean enableStateChange = true;

    public interface CustomOnClickListener {
        void onClick(View v);
    }

    public CheckImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckImageButton);

        isChecked = a.getBoolean(R.styleable.CheckImageButton_isChecked, false);
        backgroundOn = a.getInteger(R.styleable.CheckImageButton_BackgroundOn, 0);
        backgroundOff = a.getInteger(R.styleable.CheckImageButton_BackgroundOff, 0);
        imageOn = a.getDrawable(R.styleable.CheckImageButton_ImageOn);
        imageOff = a.getDrawable(R.styleable.CheckImageButton_ImageOff);

        setChecked(isChecked);

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (enableStateChange) {
                    if (isChecked) {
                        isChecked = false;
                        if (backgroundOff!=0) {
                            setBackgroundColor(backgroundOff);
                        }
                        setImageDrawable(imageOff);
                    } else {
                        isChecked = true;
                        if (backgroundOn!=0) {
                            setBackgroundColor(backgroundOn);
                        }
                        setImageDrawable(imageOn);
                    }
                    if(mCustomOnClickListener != null) {
                        mCustomOnClickListener.onClick(v);
                    }
                }
            }
        };
        setOnClickListener(ocl);


        a.recycle();
    }

    public void setCustomOnClickListener(CustomOnClickListener cl) {
        mCustomOnClickListener = cl;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        if (checked) {
            isChecked = true;
            if (backgroundOn!=0) {
                setBackgroundColor(backgroundOn);
            }
            setImageDrawable(imageOn);
        } else {
            isChecked = false;
            if (backgroundOff!=0) {
                setBackgroundColor(backgroundOff);
            }
            setImageDrawable(imageOff);
        }
    }

    public void setEnableStateChange(boolean enable) {
        enableStateChange = enable;
    }

}
