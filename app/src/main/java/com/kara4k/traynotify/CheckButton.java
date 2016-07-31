package com.kara4k.traynotify;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class CheckButton extends ImageButton {

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


    public CheckButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckButton);

        isChecked = a.getBoolean(R.styleable.CheckButton_isChecked, false);
        backgroundOn = a.getInteger(R.styleable.CheckButton_BackgroundOn, 0);
        backgroundOff = a.getInteger(R.styleable.CheckButton_BackgroundOff, 0);
        imageOn = a.getDrawable(R.styleable.CheckButton_ImageOn);
        imageOff = a.getDrawable(R.styleable.CheckButton_ImageOff);

        if (isChecked) {
            if (backgroundOn!=0) {
                setBackgroundColor(backgroundOn);
            }
            setImageDrawable(imageOn);
        } else {
            if (backgroundOff!=0) {
                setBackgroundColor(backgroundOff);
            }
            setImageDrawable(imageOff);
        }

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
        if (checked == true) {
            isChecked = true;
            if (backgroundOn!=0) {
                setBackgroundColor(backgroundOn);
            }
            setImageDrawable(imageOn);
        } else {
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
