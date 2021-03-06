package com.fise.xiaoyu.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.ReqFriendsEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.activity.PhoneInfoActivity;
import com.fise.xiaoyu.ui.activity.SearchFriednsActivity;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.jinlin.zxing.example.activity.CaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * 1.18 添加currentUser变量
 */
public class AddFriendsFragment extends MainFragment {

	private View curView = null;
    private IMService imService;         
    private int currentUserId;
    EditText mNameView;
    private UserEntity loginContact;

	private static final int REQUEST_CODE_SCAN = 0x0000;
	private static final String DECODED_CONTENT_KEY = "codedContent";
	private static final String DECODED_BITMAP_KEY = "codedBitmap";
	
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            if (curView == null) {
                return;
            }
             imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getContactManager().isUserDataReady()) {
                logger.i("detail#contact data are not ready");
            } else {
               // init(imService);
            }


            loginContact = imService.getLoginManager().getLoginInfo();

            ArrayList<Integer> userIds = new ArrayList<>(1);
            //just single type
            userIds.add(currentUserId);
            imService.getContactManager().reqGetDetaillUsers(userIds);
        }
        @Override
        public void onServiceDisconnected() {
        	
        }
    };

    
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		imServiceConnector.disconnect(getActivity());
		 	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
 
		imServiceConnector.connect(getActivity());

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_user_add_friends, topContentView);
		super.init(curView); 
		initRes();
		
		RelativeLayout shaomiao = (RelativeLayout) curView.findViewById(R.id.shao_yi_shao);
		shaomiao.setOnClickListener(new View.OnClickListener() {

		    public void onClick(View v){
				Intent intent = new Intent(AddFriendsFragment.this.getActivity(),
						CaptureActivity.class); 
				intent.putExtra(IntentConstant.KEY_QR_ACTIVITY_TYPE, DBConstant.SEX_INFO_USER);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, CaptureActivity.SCANNIN_GREQUEST_CODE);
		    }
			});
		
		//同步通讯录
		RelativeLayout phoneName = (RelativeLayout) curView
				.findViewById(R.id.phoneName);
		phoneName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
	
				 
                Intent intent = new Intent(AddFriendsFragment.this.getActivity(), PhoneInfoActivity.class); 
                AddFriendsFragment.this.getActivity().startActivity(intent);
			}
		});

		return curView;
	}

	@Override
	public void onResume() {
		Intent intent = getActivity().getIntent();
		if (null != intent) {
			String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
			setTopLeftText(fromPage);
		}
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		//setTopTitle(getActivity().getString(R.string.page_user_detail));
		//setTopLeftButton(R.drawable.tt_top_back);
		hideTopBar();
		
		mNameView = (EditText) curView.findViewById(R.id.search_phone); 
		  
		LinearLayout left = (LinearLayout) curView.findViewById(R.id.icon_arrow);
		left.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish(); 
//				if((mNameView.getText().toString())!=null){
//					imService.getLoginManager().reqFriends(mNameView.getText().toString());
//				} 
			}
		});
		
		
        EditText  text = (EditText)curView.findViewById(R.id.search_phone);  
        text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 IMUIHelper.openSearchFriendActivity(getActivity(),DBConstant.SEACHFRIENDS);
	         } 
        });
        
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		
		
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReqFriendsEvent event){
        switch (event){ 
        
//            case REQ_FRIENDS_SUCCESS:    
//                Intent intent = new Intent(this.getActivity(), SearchFriednsActivity.class); 
//                this.getActivity().startActivity(intent);
//                break;
            case REQ_FRIENDS_FAILED:    
                break;
        }
    }
    

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		     
		
		if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
			   
			if (data != null) {
				
				String content = data.getStringExtra(DECODED_CONTENT_KEY); 
				UserEntity contact = imService.getContactManager().getSearchContact(content); 
				
            	if(contact == null)
            	{  
            		imService.getUserActionManager().reqFriends(content);
            		
            	}else{
            		
            		//contact.setFriend(1);
            		imService.getUserActionManager().setSearchInfo(contact); 
            		 
                    Intent intent = new Intent(AddFriendsFragment.this.getActivity(), SearchFriednsActivity.class); 
                    AddFriendsFragment.this.getActivity().startActivity(intent);
            	}
            	
			}
		}
	}
	
	  


}
