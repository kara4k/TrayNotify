//package com.kara4k.traynotify;
//
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.Button;
//
//public class CheckButton extends Button {
//
//    CustomOnClickListener customOnClickListener;
//
//
//    private boolean isChecked;
//    private String textOn;
//    private String textOff;
//    private int backgroundOn;
//    private int backgroundOff;
//    private int textColorOn;
//    private int textColorOff;
//
//    boolean enableStateChange = true;
//
//    public interface CustomOnClickListener {
//        void onClick(View v);
//    }
//
//    public CheckButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckButton);
//
//        isChecked = a.getBoolean(R.styleable.CheckButton_isCheck, false);
//        textOn = a.getString(R.styleable.CheckButton_TextOn);
//        textOff = a.getString(R.styleable.CheckButton_TextOff);
//        backgroundOn = a.getInteger(R.styleable.CheckButton_BackgroundColorOn, 0);
//        backgroundOff = a.getInteger(R.styleable.CheckButton_BackgroundColorOff, 0);
//        textColorOn = a.getInteger(R.styleable.CheckButton_TextColorOn, getTextColors().getDefaultColor());
//        textColorOff = a.getInteger(R.styleable.CheckButton_TextColorOff, getTextColors().getDefaultColor());
//
//
//        setChecked(isChecked);
//
//
//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (enableStateChange) {
//                    if (isChecked) {
//                        isChecked = false;
//                        if (textOff != null) {
//                            setText(textOff);
//                        }
//                        if (textColorOff != 0) {
//                            setTextColor(textColorOff);
//                        }
//                        if (backgroundOff != 0) {
//                            setBackgroundColor(backgroundOff);
//                        }
//                    } else {
//                        isChecked = true;
//                        if (textOn != null) {
//                            setText(textOn);
//                        }
//                        if (textColorOn != 0) {
//                            setTextColor(textColorOn);
//                        }
//                        if (backgroundOn != 0) {
//                            setBackgroundColor(backgroundOn);
//                        }
//                    }
//                }
//                if (customOnClickListener != null) {
//                    customOnClickListener.onClick(view);
//                }
//
//            }
//        });
//        a.recycle();
//    }
//
//    public boolean isChecked() {
//        return isChecked;
//    }
//
//    public void setChecked(boolean checked) {
//        if (enableStateChange) {
//            if (checked) {
//                isChecked = true;
//                if (textOn != null) {
//                    setText(textOn);
//                }
//                if (textColorOn != 0) {
//                    setTextColor(textColorOn);
//                }
//                if (backgroundOn != 0) {
//                    setBackgroundColor(backgroundOn);
//                }
//            } else {
//                isChecked = false;
//                if (textOff != null) {
//                    setText(textOff);
//                }
//                if (textColorOff != 0) {
//                    setTextColor(textColorOff);
//                }
//                if (backgroundOff != 0) {
//                    setBackgroundColor(backgroundOff);
//                }
//            }
//        }
//    }
//
//
//    public void setEnableStateChange(boolean enableStateChange) {
//        this.enableStateChange = enableStateChange;
//    }
//
//    public void setCustomOnClickListener(CustomOnClickListener customOnClickListener) {
//        this.customOnClickListener = customOnClickListener;
//    }
//}
