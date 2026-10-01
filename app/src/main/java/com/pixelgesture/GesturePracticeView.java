package com.pixelgesture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;

/** Own-view practice illustration. Never dispatches a system action or reads a screen. */
final class GesturePracticeView extends View {
    interface Listener { void onResult(boolean shown, boolean fromTouch); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF phone = new RectF();
    private final RectF screen = new RectF(), shape = new RectF(), card = new RectF();
    private final Path arrow = new Path(), trace = new Path();
    private final ArrayList<PointF> trail = new ArrayList<>();
    private final int background, surface, variant, accent, ink;
    private final Listener listener;
    private ValueAnimator animator;
    private boolean tracking;
    private float downX, downY, progress;

    GesturePracticeView(Context context, boolean alreadyShown, Listener listener) {
        super(context);
        background=context.getColor(R.color.pg_background);surface=context.getColor(R.color.pg_surface);
        variant=context.getColor(R.color.pg_surface_variant);accent=context.getColor(R.color.pg_primary);
        ink=context.getColor(R.color.pg_on_surface);this.listener=listener;
        progress=alreadyShown?1:0;
        // Native buttons provide the equivalent action and live textual result.
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private float dp(float value) {return value*getResources().getDisplayMetrics().density;}
    private void geometry() {
        float h=Math.max(1,getHeight()-dp(34));float w=Math.min(getWidth()*.46f,h*.46f);
        phone.set((getWidth()-w)/2,dp(12),(getWidth()+w)/2,dp(12)+h);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);geometry();
        paint.setStyle(Paint.Style.FILL);paint.setColor(variant);
        shape.set(0,dp(36),getWidth(),getHeight()-dp(12));c.drawRoundRect(shape,dp(30),dp(30),paint);
        paint.setColor(ink);c.drawRoundRect(phone,dp(23),dp(23),paint);
        screen.set(phone);screen.inset(dp(5),dp(5));paint.setColor(background);
        c.drawRoundRect(screen,dp(19),dp(19),paint);
        paint.setColor(ink);shape.set(phone.centerX()-dp(15),phone.top+dp(10),phone.centerX()+dp(15),phone.top+dp(14));c.drawRoundRect(shape,dp(2),dp(2),paint);
        c.save();c.clipRect(screen.left,phone.top+dp(28),screen.right,screen.bottom-dp(17));
        float scale=1-.22f*progress;
        float cardW=(screen.width()-dp(18))*scale,cardH=(screen.height()-dp(58))*(1-.28f*progress);
        float center=screen.centerX()+dp(12)*progress,cy=screen.centerY();
        // Abstract cards: no borrowed app screenshot, app name or OS result.
        drawCard(c,center-cardW-dp(10),cy,cardW,cardH,progress*.65f);
        drawCard(c,center,cy,cardW,cardH,1);
        c.restore();
        paint.setColor(accent);shape.set(phone.left+dp(2),phone.centerY()-dp(28),phone.left+dp(6),phone.centerY()+dp(28));c.drawRoundRect(shape,dp(2),dp(2),paint);
        paint.setColor(ink);shape.set(phone.centerX()-dp(18),phone.bottom-dp(13),phone.centerX()+dp(18),phone.bottom-dp(10));c.drawRoundRect(shape,dp(2),dp(2),paint);
        float arrowY=phone.centerY();paint.setColor(accent);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));
        arrow.reset();arrow.moveTo(phone.left-dp(38),arrowY);arrow.lineTo(phone.left-dp(10),arrowY);arrow.moveTo(phone.left-dp(18),arrowY-dp(7));arrow.lineTo(phone.left-dp(10),arrowY);arrow.lineTo(phone.left-dp(18),arrowY+dp(7));c.drawPath(arrow,paint);
        if(!trail.isEmpty()) {
            trace.reset();trace.moveTo(trail.get(0).x,trail.get(0).y);
            for(int i=1;i<trail.size();i++)trace.lineTo(trail.get(i).x,trail.get(i).y);
            paint.setStrokeWidth(dp(3));paint.setStrokeCap(Paint.Cap.ROUND);c.drawPath(trace,paint);
            PointF p=trail.get(trail.size()-1);paint.setStyle(Paint.Style.FILL);c.drawCircle(p.x,p.y,dp(5),paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawCard(Canvas c,float cx,float cy,float w,float h,float opacity) {
        if(opacity<=0)return;
        paint.setColor(surface);paint.setAlpha(Math.round(opacity*255));
        card.set(cx-w/2,cy-h/2,cx+w/2,cy+h/2);c.drawRoundRect(card,dp(13),dp(13),paint);
        paint.setColor(accent);paint.setAlpha(Math.round(opacity*255));c.drawCircle(card.left+dp(18),card.top+dp(25),dp(6),paint);
        paint.setColor(variant);paint.setAlpha(Math.round(opacity*255));
        shape.set(card.left+dp(11),card.top+dp(44),card.right-dp(11),card.top+h*.48f);c.drawRoundRect(shape,dp(7),dp(7),paint);
        for(int i=0;i<3;i++){shape.set(card.left+dp(12),card.top+h*.58f+i*dp(17),card.right-dp(18+i*6),card.top+h*.58f+i*dp(17)+dp(5));c.drawRoundRect(shape,dp(2),dp(2),paint);}
        paint.setAlpha(255);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        geometry();float x=event.getX(),y=event.getY();
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(Math.abs(x-phone.left)>dp(24)||Math.abs(y-phone.centerY())>phone.height()*.25f)return false;
                if(animator!=null)animator.cancel();progress=0;trail.clear();
                listener.onResult(false,false);tracking=true;downX=x;downY=y;trail.add(new PointF(x,y));
                getParent().requestDisallowInterceptTouchEvent(true);invalidate();return true;
            case MotionEvent.ACTION_MOVE:
                if(!tracking)return false;
                if(trail.size()<80)trail.add(new PointF(x,y));invalidate();return true;
            case MotionEvent.ACTION_UP:
                if(!tracking)return false;performClick();tracking=false;getParent().requestDisallowInterceptTouchEvent(false);
                if(x-downX>=phone.width()*.32f&&Math.abs(y-downY)<phone.height()*.18f)showPreview(true);
                else {trail.clear();listener.onResult(false,true);invalidate();}
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
                tracking=false;trail.clear();listener.onResult(false,false);getParent().requestDisallowInterceptTouchEvent(false);invalidate();return true;
            default:return tracking;
        }
    }

    @Override public boolean performClick(){super.performClick();return true;}

    void showPreview(boolean fromTouch) {
        if(animator!=null)animator.cancel();listener.onResult(true,fromTouch);
        if(!ValueAnimator.areAnimatorsEnabled()){progress=1;invalidate();return;}
        animator=ValueAnimator.ofFloat(progress,1);animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a->{progress=(float)a.getAnimatedValue();invalidate();});animator.start();
    }

    void reset() {if(animator!=null)animator.cancel();tracking=false;progress=0;trail.clear();listener.onResult(false,false);invalidate();}
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();tracking=false;trail.clear();super.onDetachedFromWindow();}
}
