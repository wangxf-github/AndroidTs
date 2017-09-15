package com.fingersoft.im.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fingersoft.im.R;

/**
 * Created by Administrator on 2017/9/11.
 */

public class DialogActivity extends Activity {
    Button btn_show;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        initView();
    }

    private void initView() {
        btn_show = findViewById(R.id.btn_show_dialog);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = createLoadingDialog(view.getContext(),"loading...");
//                Dialog dialog = initLayout(view.getContext(), "loading...");
                dialog.show();
            }
        });
    }


    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(createAnimation());
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(true);// 不可以用“返回键”取消
        LinearLayout.LayoutParams layoutParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        loadingDialog.setContentView(layout, layoutParams);// 设置布局
        return loadingDialog;
    }

    public Dialog initLayout(Context context, String msg) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackground(createDrawableStyle());
        linearLayout.setMinimumHeight(dip2px(context, 100));
        linearLayout.setMinimumWidth(dip2px(context, 250));
        linearLayout.setGravity(Gravity.CENTER);
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imgParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageView.setMinimumHeight(dip2px(context, 50));
        imageView.setMinimumWidth(dip2px(context, 50));
        imageView.setAnimation(createAnimation());
        imageView.setImageResource(R.drawable.publicloading);
        LinearLayout.LayoutParams textParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMarginStart(dip2px(context, 20));
        TextView textView = new TextView(context);
        textView.setLayoutParams(textParams);
        textView.setText(msg);
        textView.setTextSize(dip2px(context, 20));

        linearLayout.addView(imageView, imgParams);
        linearLayout.addView(textView, textParams);


        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(true);// 不可以用“返回键”取消
        LinearLayout.LayoutParams dialogParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        loadingDialog.setContentView(linearLayout, dialogParams);// 设置布局
        return loadingDialog;
    }


    /**
     * 创建drawable属性
     *
     * @return
     */
    public Drawable createDrawableStyle() {
        int roundRadius = dip2px(this, 5);
        int fillColor = Color.parseColor("#FFFFFF");

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        gd.setCornerRadius(roundRadius);
        return gd;
    }

    /**
     * 创建图片旋转动画
     *
     * @return
     */
    public Animation createAnimation() {
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(1500);//设置动画持续时间
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setStartOffset(-1);//执行前的等待时间
        rotate.setRepeatMode(Animation.RESTART);
        return rotate;
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
