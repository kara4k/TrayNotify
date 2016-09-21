package com.kara4k.traynotify;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyView extends RelativeLayout {

    private TextView title;
    private TextView text;
    private CheckBox checkbox;
    private ImageView image;
    private SecondOnClickListener secondOnClickListener;

    private String titleText;
    private String textText;
    private boolean checkVisible;
    private boolean isChecked;
    private boolean mCheckToggle;
    private boolean imageVisible;
    private Drawable drawable;

    public MyView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        mCheckToggle = true;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyView);


        titleText = a.getString(R.styleable.MyView_Title);
        textText = a.getString(R.styleable.MyView_Text);
        checkVisible = a.getBoolean(R.styleable.MyView_CheckVisible, false);
        isChecked = a.getBoolean(R.styleable.MyView_isChecked, false);
        imageVisible = a.getBoolean(R.styleable.MyView_ImageVisible, false);
        drawable = a.getDrawable(R.styleable.MyView_Image);

        a.recycle();

        init();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((checkbox.isEnabled()) && (mCheckToggle)) {
                    if (checkbox.isChecked()) {
                        checkbox.setChecked(false);
                    } else {
                        checkbox.setChecked(true);
                    }
                }
                if (secondOnClickListener != null) {
                    secondOnClickListener.onClick();
                }
            }
        });
    }

    private void init() {
        title.setText(titleText);
        text.setText(textText);

        if (checkVisible) {
            checkbox.setVisibility(VISIBLE);
        } else {
            checkbox.setVisibility(INVISIBLE);
        }

        checkbox.setChecked(isChecked);

        if (imageVisible) {
            image.setVisibility(VISIBLE);
        } else {
            image.setVisibility(INVISIBLE);
        }

        if (drawable!=null) {
            image.setImageDrawable(drawable);
        }
    }

    private void init(Context c) {
        inflate(c, R.layout.my_view, this);
        title = (TextView) findViewById(R.id.name);
        text = (TextView) findViewById(R.id.text);
        checkbox = (CheckBox) findViewById(R.id.check);
        image = (ImageView) findViewById(R.id.image);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextView getText() {
        return text;
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }

    public ImageView getImage() {
        return image;
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setSecondOnClickListener(SecondOnClickListener secondOnClickListener) {
        this.secondOnClickListener = secondOnClickListener;
    }

    public void setmCheckToggle(boolean mCheckToggle) {
        this.mCheckToggle = mCheckToggle;
    }

    public interface SecondOnClickListener {
        void onClick();
    }
}
