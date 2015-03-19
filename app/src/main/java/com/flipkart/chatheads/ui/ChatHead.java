package com.flipkart.chatheads.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.R;

/**
 * Created by kirankumar on 10/02/15.
 */
public class ChatHead<T> extends ImageView implements SpringListener {

    private static final int radius = 75;
    private ChatHeadContainer container;
    private ChatHeadSpringsHolder springsHolder;
    private State state;
    private T key;

    private float downX = -1;
    private float downY = -1;
    private VelocityTracker velocityTracker;
    private boolean isDragging;
    private float downTranslationX;
    private float downTranslationY;
    private final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private final int CLOSE_ATTRACTION_THRESHOLD = ChatHeadUtils.dpToPx(getContext(), 110);
    private final float DELTA = ChatHeadUtils.dpToPx(getContext(), 10);



    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    private int unreadCount = 0;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        Spring activeHorizontalSpring = springsHolder.getActiveHorizontalSpring();
        Spring activeVerticalSpring = springsHolder.getActiveVerticalSpring();
        float deltaX = (float) (DELTA * ((float) container.getMaxWidth() / 2f - (activeHorizontalSpring.getCurrentValue() + getMeasuredWidth() / 2)) / ((float) container.getMaxWidth() / 2f));
        springsHolder.setChainDelta(deltaX, 0);
        double distanceCloseButtonFromHead = container.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + getMeasuredWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + getMeasuredHeight() / 2);
        int totalVelocity = (int) Math.hypot(activeHorizontalSpring.getVelocity(), activeVerticalSpring.getVelocity());
        container.getActiveArrangement().onSpringUpdate(this, isDragging, container.getMaxWidth(), container.getMaxHeight(), spring, activeHorizontalSpring, activeVerticalSpring, totalVelocity);
        if (!isDragging) {


            /** Capturing check **/

            if (distanceCloseButtonFromHead < CLOSE_ATTRACTION_THRESHOLD && totalVelocity < 1000) {

                int[] coords = container.getChatHeadCoordsForCloseButton(this);
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeHorizontalSpring.setEndValue(coords[0]);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeVerticalSpring.setEndValue(coords[1]);
                //closeButton.appear(false);
                if (activeHorizontalSpring.currentValueIsApproximately(coords[0]) && activeVerticalSpring.currentValueIsApproximately(coords[0])) {
                    activeHorizontalSpring.setAtRest();
                    activeVerticalSpring.setAtRest();
                    setState(ChatHead.State.CAPTURED);
                }

            }
            if (getState() == ChatHead.State.CAPTURED) {
                //scaleSpring.setEndValue(0);
                //closeButton.disappear(true);
                //captureChatHeads();
            }
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    public enum State {
        FREE, CAPTURED;
    }

    public ChatHead(Context context) {
        super(context);
        init();
    }

    public ChatHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private SpringListener xPositionListener;
    private SpringListener yPositionListener;

    public ChatHead(ChatHeadContainer container, ChatHeadSpringsHolder springsHolder, Context context) {
        super(context);
        init();
        this.container = container;
        this.springsHolder = springsHolder;
    }

    private void init() {
        int radiusInDp = ChatHeadUtils.dpToPx(getContext(), radius);
        setLayoutParams(new ViewGroup.LayoutParams(radiusInDp, radiusInDp));
        setImageResource(R.drawable.chathead);
        xPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationX((float) spring.getCurrentValue());
            }
        };
        yPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationY((float) spring.getCurrentValue());
            }
        };
    }

    public SpringListener getHorizontalPositionListener() {
        return xPositionListener;
    }

    public SpringListener getVerticalPositionListener() {
        return yPositionListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            container.selectSpring(this);
        }

        //Chathead view will set the correct active springs on touch
        Spring activeHorizontalSpring = springsHolder.getActiveHorizontalSpring();
        Spring activeVerticalSpring = springsHolder.getActiveVerticalSpring();

        int action = event.getAction();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        float offsetX = rawX - downX;
        float offsetY = rawY - downY;


        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        if (action == MotionEvent.ACTION_DOWN) {
            setState(ChatHead.State.FREE);
            downX = rawX;
            downY = rawY;
            downTranslationX = (float) activeHorizontalSpring.getCurrentValue();
            downTranslationY = (float) activeVerticalSpring.getCurrentValue();
            //scaleSpring.setEndValue(1.2f);
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
            velocityTracker.addMovement(event);
            //closeButton.setShouldNotDismissOnRest(true);
            //postDelayed(closeButtonDisplayer, 500);

        } else if (action == MotionEvent.ACTION_MOVE) {
            if(Math.hypot(offsetX,offsetY)>touchSlop)
            {
                isDragging = true;
            }
            if (isDragging) {
                //closeButton.pointTo(rawX, rawY);
                double distanceCloseButtonFromHead = container.getDistanceCloseButtonFromHead(rawX, rawY);
                if (distanceCloseButtonFromHead < CLOSE_ATTRACTION_THRESHOLD) {
                    setState(ChatHead.State.CAPTURED);
                    int[] coords = container.getChatHeadCoordsForCloseButton(this);
                    activeHorizontalSpring.setEndValue(coords[0]);
                    activeVerticalSpring.setEndValue(coords[1]);
                    //closeButton.capture();

                } else {
                    setState(ChatHead.State.FREE);
                    activeHorizontalSpring.setEndValue(downTranslationX + offsetX);
                    activeVerticalSpring.setEndValue(downTranslationY + offsetY);
                    velocityTracker.addMovement(event);
                    //closeButton.release();
                }
            }

        } else {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                boolean wasDragging = isDragging;
                isDragging = false;

                //removeCallbacks(closeButtonDisplayer);
                //closeButton.disappear(true);
                //scaleSpring.setEndValue(1);
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                int xVelocity = (int) velocityTracker.getXVelocity();
                int yVelocity = (int) velocityTracker.getYVelocity();
                boolean touchUpHandled = container.getActiveArrangement().handleTouchUp(this, xVelocity, yVelocity, activeHorizontalSpring, activeVerticalSpring, wasDragging);
                if(!touchUpHandled)
                {
                    container.getActiveArrangement().onDeactivate(this, container.getMaxWidth(), container.getMaxHeight(), activeHorizontalSpring, activeVerticalSpring);
                    container.toggleArrangement(this);
                }


            }
        }

        return true;
    }



}
