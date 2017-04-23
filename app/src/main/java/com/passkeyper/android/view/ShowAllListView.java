package com.passkeyper.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * ListView implementation that forces all items to show.
 */
public class ShowAllListView extends ListView {

    public ShowAllListView(Context context) {
        super(context);
    }

    public ShowAllListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
