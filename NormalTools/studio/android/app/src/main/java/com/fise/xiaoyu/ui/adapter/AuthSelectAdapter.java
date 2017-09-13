package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.SysConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AuthSelectAdapter
 * 搜索模式下，边边的字母序查找不显示
 */
public class AuthSelectAdapter extends BaseAdapter  implements SectionIndexer,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{
    private Logger logger = Logger.getLogger(AuthSelectAdapter.class);

    private List<UserEntity>  allUserList = new ArrayList<>();
    private List<UserEntity>  backupList = new ArrayList<>();

    /**已经选中的，不能操作*/
    private Set<Integer> alreadyListSet = new HashSet<>();
    /**在选择面板里面选择的*/
    private Set<Integer> checkListSet= new HashSet<>();
    private List<UserEntity> selectList = new ArrayList<>();
    
    
    private boolean isSearchMode= false;
    private String searchKey;
    private Context ctx;
    private IMService imService;

    public AuthSelectAdapter(Context ctx,IMService service){
        this.ctx = ctx;
        this.imService = service;
    }

    public void setAllUserList(List<UserEntity> allUserList) {
        this.allUserList = allUserList;
        this.backupList  = allUserList;
    }

    public void recover(){
        isSearchMode = false;
        allUserList = backupList;
        notifyDataSetChanged();
    }

    public void onSearch(String key){
       isSearchMode = true;
       searchKey = key;
       //allUserList.clear();
        List<UserEntity> searchList = new ArrayList<>();
       for(UserEntity entity:backupList){
            if(IMUIHelper.handleContactSearch(searchKey,entity)){
                searchList.add(entity);
            }
        }
        allUserList = searchList;
        notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }


    // 在搜索模式下，直接返回
    @Override
    public int getPositionForSection(int sectionIndex) {
        logger.d("pinyin#getPositionForSection secton:%d", sectionIndex);
        int index = 0;
        for(UserEntity entity:allUserList){
            int firstCharacter = entity.getSectionName().charAt(0);
            // logger.d("firstCharacter:%d", firstCharacter);
            if (firstCharacter == sectionIndex) {
                logger.d("pinyin#find sectionName");
                return index;
            }
            index++;
        }
        logger.e("pinyin#can't find such section:%d", sectionIndex);
        return -1;
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	 
        UserEntity contact = (UserEntity) getItem(position);
        AuthHolder viewHolder = (AuthHolder) view.getTag();

        if (viewHolder == null || alreadyListSet.contains(contact.getPeerId())) {
            return;
        }
        viewHolder.checkBox.toggle();
        boolean checked = viewHolder.checkBox.isChecked();
        int userId = contact.getPeerId();
        if (checked) {
            checkListSet.add(userId);
            selectList.add(contact);
        } else {
            checkListSet.remove(userId);
            selectList.remove(contact);
        }
    }
    
    
    public void setCheck(int userId, boolean checked,UserEntity user){
        if (checked) {
            checkListSet.add(userId);
            selectList.add(user);
        } else {
            checkListSet.remove(userId);
            selectList.remove(user);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity contact = (UserEntity) getItem(position);
        IMUIHelper.handleContactItemLongClick(contact, ctx);
        return true;
    }

    @Override
    public int getCount() {
       int size = allUserList==null?0:allUserList.size();
       return size;
    }

    @Override
    public Object getItem(int position) {
        return allUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

     
    public List<UserEntity>  getAllUser() {
        return allUserList;
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        UserEntity userEntity = (UserEntity) getItem(position);
        if(userEntity == null){
            logger.e("GroupSelectAdapter#getView#userEntity is null!position:%d",position);
            return null;
        }
 
        
        AuthHolder userHolder = null;
        if (view == null) {
            userHolder = new AuthHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_auth, parent,false);
            userHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            userHolder.realNameView = (TextView) view.findViewById(R.id.contact_realname_title);
            userHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
            userHolder.avatar = (IMBaseImageView)view.findViewById(R.id.contact_portrait);
            userHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (AuthHolder) view.getTag();
        }

        String  showName = "";
        if (userEntity.getComment().equals("")) {
            showName = userEntity.getMainName();
        } else {
            showName = userEntity.getComment();
        }
        userHolder.checkBox.setVisibility(View.VISIBLE);
        if(isSearchMode){
            // 高亮显示
            IMUIHelper.setTextHilighted(userHolder.nameView,showName,
                    userEntity.getSearchElement());
        }else{
            userHolder.nameView.setText(showName);
        }

        userHolder.avatar.setImageResource(R.drawable.tt_default_user_portrait_corner);
        userHolder.divider.setVisibility(View.VISIBLE);

        // 字母序第一个要展示 ,搜索模式下不展示sectionName
        if(!isSearchMode) {
            String sectionName = userEntity.getSectionName();
            String preSectionName = null;
            if (position > 0) {
                preSectionName = ((UserEntity) getItem(position - 1)).getSectionName();
            }
            if (TextUtils.isEmpty(preSectionName) || !preSectionName.equals(sectionName)) {
                userHolder.sectionView.setVisibility(View.VISIBLE);
                userHolder.sectionView.setText(sectionName);
                userHolder.divider.setVisibility(View.GONE);
            } else {
                userHolder.sectionView.setVisibility(View.GONE);
            }
        }else{
            userHolder.sectionView.setVisibility(View.GONE);
        }

        // checkBox 状态的设定
        boolean checked = checkListSet.contains(userEntity.getPeerId());
        userHolder.checkBox.setChecked(checked);
        boolean disable = alreadyListSet.contains(userEntity.getPeerId());
        if(disable){
            userHolder.checkBox.setEnabled(false);
             
        }else{
            userHolder.checkBox.setEnabled(true);
        }

        userHolder.avatar.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        userHolder.avatar.setCorner(0);
        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }


    public  class AuthHolder {
        View divider;
        TextView sectionView;
        public TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
        public CheckBox checkBox;
    }


    /**------------------set/get------------------*/

    public Set<Integer> getAlreadyListSet() {
        return alreadyListSet;
    }

    public void setAlreadyListSet(Set<Integer> alreadyListSet) {
        this.alreadyListSet = alreadyListSet;
    }

    public Set<Integer> getCheckListSet() {
        return checkListSet;
    }
     
    public List<UserEntity> getSelectList() {
        return selectList;
    }
}