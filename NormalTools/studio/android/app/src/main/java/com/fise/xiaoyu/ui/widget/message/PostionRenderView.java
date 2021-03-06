package com.fise.xiaoyu.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.ui.widget.BubbleImageView;
import com.fise.xiaoyu.ui.widget.MGProgressbar;
import com.fise.xiaoyu.ui.widget.MapImageView;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.Logger;

/**
 * PostionRenderView
 */
public class PostionRenderView extends BaseMsgRenderView {
    private Logger logger = Logger.getLogger(PostionRenderView.class);

    // 上层必须实现的接口
    private ImageLoadListener imageLoadListener;
    private BtnImageListener btnImageListener;

    /**
     * 可点击的view
     */
    private View messageLayout;
    /**
     * 图片消息体
     */
    private MapImageView messageImage;
    /**
     * 图片状态指示
     */
    private MGProgressbar imageProgress;
    private TextView info_name;
    private TextView postion_name;
    private static Context _context;

    public PostionRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static PostionRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        _context = context;
        int resource = isMine ? R.layout.tt_mine_postion_message_item : R.layout.tt_other_postion_message_item;
        PostionRenderView postionRenderView = (PostionRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        postionRenderView.setMine(isMine);
        postionRenderView.setParentView(viewGroup);
        return postionRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        messageLayout = findViewById(R.id.message_layout);
        messageImage = (MapImageView) findViewById(R.id.message_image);
        imageProgress = (MGProgressbar) findViewById(R.id.tt_image_progress);
        imageProgress.setShowText(false);
        postion_name = (TextView) findViewById(R.id.postion_name);
        info_name = (TextView) findViewById(R.id.info_name);

    }

    /**
     *
     * */

    /**
     * 控件赋值
     *
     * @param messageEntity
     * @param userEntity    对于mine。 图片send_success 就是成功了直接取地址
     *                      对于sending  就是正在上传
     *                      <p/>
     *                      对于other，消息一定是success，接受成功额
     *                      2. 然后分析loadStatus 判断消息的展示状态
     */
    @Override
    public void render(final MessageEntity messageEntity, final UserEntity userEntity, Context ctx, PeerEntity peerEntity) {
        super.render(messageEntity, userEntity, ctx, peerEntity);
    }


    /**
     * 多端同步也不会拉到本地失败的数据
     * 只有isMine才有的状态，消息发送失败
     * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
     * 2. 图片上传成功，但是发送失败。 点击重新发送??
     * 3. 比较悲剧的是 图片上传失败和消息发送失败都是这个状态 不过可以通过另外一个状态来区别 图片load状态
     *
     * @param entity
     */
    @Override
    public void msgFailure(final MessageEntity entity) {
        super.msgFailure(entity);
        messageImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**判断状态，重新发送resend*/
                btnImageListener.onMsgFailure();
            }
        });
        if (FileUtil.isFileExist(((PostionMessage) entity).getPath())) {
            messageImage.setImageUrl("file://" + ((PostionMessage) entity).getPath());
        } else {
            messageImage.setImageUrl(((PostionMessage) entity).getUrl());
        }
        imageProgress.hideProgress();
    }


    @Override
    public void msgStatusError(final MessageEntity entity) {
        super.msgStatusError(entity);
        imageProgress.hideProgress();
    }


    /**
     * 图片信息正在发送的过程中
     * 1. 上传图片
     * 2. 发送信息
     */
    @Override
    public void msgSendinging(final MessageEntity entity) {
        if (isMine()) {
            if (FileUtil.isFileExist(((PostionMessage) entity).getPath())) {

                messageImage.setImageLoaddingCallback(new MapImageView.ImageLoaddingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        imageProgress.hideProgress();
                    }
                });
                messageImage.setImageUrl("file://" + ((PostionMessage) entity).getPath());
            } else {
                //todo  文件不存在
            }
        }

    }


    /**
     * 消息成功
     * 1. 对方图片消息
     * 2. 自己多端同步的消息
     * 说明imageUrl不会为空的
     */
    @Override
    public void msgSuccess(final MessageEntity entity) {
        super.msgSuccess(entity);
        final PostionMessage postionMessage = (PostionMessage) entity;
        final String imagePath = postionMessage.getPath();
        final String url = postionMessage.getUrl();
        int loadStatus = postionMessage.getLoadStatus();
        if (TextUtils.isEmpty(url)) {
            /**消息状态异常*/
            msgStatusError(entity);
            return;
        }

        switch (loadStatus) {
            case MessageConstant.IMAGE_UNLOAD: {
                messageImage.setImageLoaddingCallback(new MapImageView.ImageLoaddingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                        if (imageLoadListener != null) {
                            imageLoadListener.onLoadComplete(imageUri);
                        }
                        getImageProgress().hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        getImageProgress().showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {
                        getImageProgress().hideProgress();
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        getImageProgress().hideProgress();
                        imageLoadListener.onLoadFailed();
                    }
                });

                if (isMine()) {
                    if (FileUtil.isFileExist(imagePath)) {
                        messageImage.setImageUrl("file://" + imagePath);
                    } else {
                        messageImage.setImageUrl(url);
                    }
                } else {
                    messageImage.setImageUrl(url);
                }
            }
            break;

            case MessageConstant.IMAGE_LOADING: {

            }
            break;

            case MessageConstant.IMAGE_LOADED_SUCCESS: {
                messageImage.setImageLoaddingCallback(new MapImageView.ImageLoaddingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        imageProgress.showProgress();
                    }
                });

                if (isMine()) {
                    if (FileUtil.isFileExist(imagePath)) {
                        messageImage.setImageUrl("file://" + imagePath);
                    } else {
                        messageImage.setImageUrl(url);
                    }
                } else {
                    messageImage.setImageUrl(url);
                }
                messageImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (btnImageListener != null) {
                            btnImageListener.onMsgSuccess();

//                            PostionMessage postionMessage = (PostionMessage)entity;
//                    		final double lat = postionMessage.getLat();
//                    		final double lng = postionMessage.getLng(); 
//                    		
//                			Intent intent = new Intent(_context, PostionTouchActivity.class);
//            				intent.putExtra(IntentConstant.POSTION_LAT, lat);
//            				intent.putExtra(IntentConstant.POSTION_LNG, lng);
//            				_context.startActivity(intent);
                        }
                    }
                });

            }
            break;

            //todo 图像失败了，允许点击之后重新下载
            case MessageConstant.IMAGE_LOADED_FAILURE: {
//                msgStatusError(imageMessage);
//                getImageProgress().hideProgress();
                messageImage.setImageLoaddingCallback(new MapImageView.ImageLoaddingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        getImageProgress().hideProgress();
                        imageLoadListener.onLoadComplete(imageUri);
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        getImageProgress().showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                    }
                });
                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        messageImage.setImageUrl(url);

//                        PostionMessage postionMessage = (PostionMessage)entity;
//                		final double lat = postionMessage.getLat();
//                		final double lng = postionMessage.getLng(); 
//                		
//            			Intent intent = new Intent(_context, PostionTouchActivity.class);
//        				intent.putExtra(IntentConstant.POSTION_LAT, lat);
//        				intent.putExtra(IntentConstant.POSTION_LNG, lng);
//        				_context.startActivity(intent);
                    }
                });
            }
            break;

        }
    }

    /**
     * ---------------------图片下载相关、点击、以及事件回调start-----------------------------------
     */
    public interface BtnImageListener {
        public void onMsgSuccess();

        public void onMsgFailure();
    }

    public void setBtnImageListener(BtnImageListener btnImageListener) {
        this.btnImageListener = btnImageListener;
    }


    public interface ImageLoadListener {
        public void onLoadComplete(String path);

        // 应该把exception 返回结构放进去
        public void onLoadFailed();

    }


    public void setImageLoadListener(ImageLoadListener imageLoadListener) {
        this.imageLoadListener = imageLoadListener;
    }

    /**---------------------图片下载相关、以及事件回调 end-----------------------------------*/


    /**
     * ----------------------set/get------------------------------------
     */
    public View getMessageLayout() {
        return messageLayout;
    }

    public ImageView getMessageImage() {
        return messageImage;
    }

    public TextView getPostionText() {
        return postion_name;
    }

    public TextView getInfoText() {
        return info_name;
    }

    public MGProgressbar getImageProgress() {
        return imageProgress;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

}
