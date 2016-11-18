package com.renren.ruolan.travelaround.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hymane.expandtextview.ExpandTextView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okrx.RxAdapter;
import com.renren.ruolan.travelaround.BaseActivity;
import com.renren.ruolan.travelaround.R;
import com.renren.ruolan.travelaround.adapter.ProductDetailHolidayAdapter;
import com.renren.ruolan.travelaround.adapter.ProductDetailHotelAdapter;
import com.renren.ruolan.travelaround.adapter.ProductDetailMoreDataAdapter;
import com.renren.ruolan.travelaround.cn.sharesdk.onekeyshare.OnekeyShare;
import com.renren.ruolan.travelaround.constant.Contants;
import com.renren.ruolan.travelaround.constant.HttpUrlPath;
import com.renren.ruolan.travelaround.entity.CollectData;
import com.renren.ruolan.travelaround.entity.DetailBean;
import com.renren.ruolan.travelaround.entity.DetailBean.ResultEntity;
import com.renren.ruolan.travelaround.entity.DetailBean.ResultEntity.OptionListEntity;
import com.renren.ruolan.travelaround.entity.DetailBean.ResultEntity.OtherInfoEntity;
import com.renren.ruolan.travelaround.entity.DetailBean.ResultEntity.TagListEntity;
import com.renren.ruolan.travelaround.entity.MoreDataDetail;
import com.renren.ruolan.travelaround.entity.MoreDataDetail.ResultEntity.DetailListEntity;
import com.renren.ruolan.travelaround.widget.CustomPrograss;
import com.renren.ruolan.travelaround.widget.PullUpToLoadMore;
import com.renren.ruolan.travelaround.widget.carousel.FlyBannerSecond;
import com.renren.ruolan.travelaround.widget.tag.TagBaseAdapter;
import com.renren.ruolan.travelaround.widget.tag.TagCloudLayout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.sharesdk.framework.ShareSDK;
import rx.android.schedulers.AndroidSchedulers;

import static com.renren.ruolan.travelaround.R.id.add;
import static com.renren.ruolan.travelaround.R.id.etv;


public class ProductDetailActivity extends BaseActivity implements View.OnClickListener {

    private String Platform, ProductID, CityName;
    private RelativeLayout mActivityProductDetail;
    private PullUpToLoadMore mTopScrollView;
    private TextView mTvPrice;
    private TextView mTvName;
    private TextView mTvDes;
    private TagCloudLayout mContainer;
    private ImageView mImgAddress;
    private TextView mTvAddress;
    private ImageView mUserCenterOrderLeft;
    private RecyclerView mHotRecyclerView;
    private ImageView mHolidayNotice;
    private RecyclerView mHolidayRecyclerView;
    private ExpandTextView mEtv;
    private ImageView mUserComment;
    private RecyclerView mCommentRecyclerView;
    private RecyclerView mDetailRecyclerView;
    private FlyBannerSecond mFlyBannerSecond;
    private FloatingActionButton mFabCollect, mFabUp;
    private RelativeLayout mActivityProduct;

    private ImageView mImgShare, mImgBack;

    private ResultEntity mResultEntities;

    //图片集合
    private List<String> mImageLists = new ArrayList<>();

    //标签集合
    private List<TagListEntity> mTagListEntities = new ArrayList<>();

    //放假须知集合
    private List<OtherInfoEntity> mOtherInfoEntities = new ArrayList<>();
    private ProductDetailHolidayAdapter mHolidayAdapter;

    //旅馆
    private List<OptionListEntity> mOptionListEntities = new ArrayList<>();
    private ProductDetailHotelAdapter mHotelAdapter;

    //更多详情信息
    private List<DetailListEntity> mDetailListEntities = new ArrayList<>();
    private ProductDetailMoreDataAdapter mMoreDataAdapter;

    @Override
    protected int getResultId() {
        return R.layout.activity_product_detail;
    }

    @Override
    protected void initListener() {
        mImgShare.setOnClickListener(this);
        mImgBack.setOnClickListener(this);
        mFabCollect.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();


        CustomPrograss.show(this,getResources().getString(R.string.loading),false,null);

        OkGo.post(HttpUrlPath.HOME_DETAIL_URL)
                .params("Platform", Platform)
                .params("ProductID", ProductID)
                .params("CityName", CityName)
                .getCall(StringConvert.create(), RxAdapter.<String>create())
                .doOnSubscribe(() -> {
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Type type = new TypeToken<DetailBean>() {
                    }.getType();
                    DetailBean bean = new Gson().fromJson(s, type);
                    if (bean.getStatus().equals("0")) {
                        mResultEntities = bean.getResult();
                        if (mResultEntities != null) {
                            mActivityProduct.setVisibility(View.VISIBLE);

                            CustomPrograss.disMiss();

                            mImageLists = mResultEntities.getImgList();
                            //设置图片
                            if (mImageLists.size() > 0) {
                                mFlyBannerSecond.setImagesUrl(mImageLists);

                            }
                            mTvAddress.setText(mResultEntities.getAddress());

                            mTagListEntities = mResultEntities.getTagList();
                            //标签
                            if (mTagListEntities.size() > 0) {
                                List<String> tags = new ArrayList<>();
                                for (TagListEntity tagListEntity : mTagListEntities) {
                                    tags.add(tagListEntity.getClassName());
                                }
                                mContainer.setAdapter(new
                                        TagBaseAdapter(ProductDetailActivity.this, tags));

                            }

                            //通知
                            mOtherInfoEntities = mResultEntities.getOtherInfo();
                            if (mOtherInfoEntities.size() > 0) {
                                mHolidayAdapter.setDatas(mOtherInfoEntities);
                            }

                            //旅馆
                            mOptionListEntities = mResultEntities.getOptionList();
                            if (mOptionListEntities.size() > 0) {
                                mHotelAdapter.setDatas(mOptionListEntities);
                            }

                            mTvName.setText(mResultEntities.getTitle());
                            mTvDes.setText(mResultEntities.getProName());

                            if (mResultEntities.getTraffic() != null) {
                                mEtv.setContent(mResultEntities.getTraffic());
                                mEtv.setMinVisibleLines(5);
                                mEtv.setContentTextSize(15);
                                mEtv.setTitleTextSize(16);
                                mEtv.setHintTextSize(12);
                                mEtv.setHintTextColor(Color.parseColor("#913242"));
                            }

                            String bgl = "select * from CollectData where ProductID = ?";
                            new BmobQuery<CollectData>().doSQLQuery(bgl, new SQLQueryListener<CollectData>() {
                                @Override
                                public void done(BmobQueryResult<CollectData> bmobQueryResult, BmobException e) {
                                    if (e == null) {
                                        List<CollectData> results = bmobQueryResult.getResults();
                                        if (results != null && results.size() > 0) {
                                            mFabCollect.setImageResource(R.drawable.custom_detail_collecticon_selected);
                                        }
                                    }
                                }
                            }, mResultEntities.getProductID());
                        }
                    }
                }, throwable -> {
                });

        OkGo.post(HttpUrlPath.GET_CITY_MORE_INFO)
                .params("Platform", Platform)
                .params("ProductID", ProductID)
                .params("CityName", CityName)
                .getCall(StringConvert.create(), RxAdapter.<String>create())
                .doOnSubscribe(() -> {
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Type type = new TypeToken<MoreDataDetail>() {
                    }.getType();
                    MoreDataDetail detail = new Gson().fromJson(s, type);
                    if (detail.getStatus().equals("0")) {
                        mDetailListEntities = detail.getResult().getDetailList();
                        mMoreDataAdapter.setDatas(mDetailListEntities);
                    }
                }, throwable -> {
                });
    }

    @Override
    public void initView() {



        Platform = getIntent().getStringExtra(Contants.PLATFORM);
        ProductID = getIntent().getStringExtra(Contants.PRODUCT_ID);
        CityName = getIntent().getStringExtra(Contants.CITY_NAME);

        mActivityProduct = (RelativeLayout) findViewById(R.id.activity_product);
        mActivityProduct.setVisibility(View.INVISIBLE);
        mTopScrollView = (PullUpToLoadMore) findViewById(R.id.top_scroll_view);
        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mFlyBannerSecond = (FlyBannerSecond) findViewById(R.id.hacky_view_pager);
        mTvDes = (TextView) findViewById(R.id.tv_des);
        mContainer = (TagCloudLayout) findViewById(R.id.container);
        mImgAddress = (ImageView) findViewById(R.id.img_address);
        mTvAddress = (TextView) findViewById(R.id.tv_address);
        mUserCenterOrderLeft = (ImageView) findViewById(R.id.user_center_order_left);
        mHotRecyclerView = (RecyclerView) findViewById(R.id.hot_recycler_view);

        //热门商品  旅馆
        mHotRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHotRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mHotelAdapter = new ProductDetailHotelAdapter(this, mOptionListEntities);
        mHotRecyclerView.setAdapter(mHotelAdapter);


        mHolidayNotice = (ImageView) findViewById(R.id.holiday_notice);

        //放假须知
        mHolidayRecyclerView = (RecyclerView) findViewById(R.id.holiday_recycler_view);
        mHolidayRecyclerView.setLayoutManager(new MyLayoutManager(this) {
            @Override
            public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {


            }
        });
        mHolidayRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mHolidayAdapter = new ProductDetailHolidayAdapter(this, mOtherInfoEntities);
        mHolidayRecyclerView.setAdapter(mHolidayAdapter);


        mEtv = (ExpandTextView) findViewById(etv);
        mUserComment = (ImageView) findViewById(R.id.user_comment);
        mCommentRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        mDetailRecyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        mDetailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDetailRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMoreDataAdapter = new ProductDetailMoreDataAdapter(this, mDetailListEntities);
        mDetailRecyclerView.setAdapter(mMoreDataAdapter);

        mFabCollect = (FloatingActionButton) findViewById(R.id.fab_collect);
        mFabUp = (FloatingActionButton) findViewById(R.id.fab_scroll);
        mFabUp.setOnClickListener(view -> mTopScrollView.scrollToTop());

        mImgShare = (ImageView) findViewById(R.id.img_share);

        mImgBack = (ImageView) findViewById(R.id.img_back);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_share:
                showShare();
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.fab_collect:
                collectData();
                break;
        }
    }

    /**
     * 收藏该信息  不需要保存全部，保存必要字段，需要的时候，根据字段进行网络请求即可
     */
    private void collectData() {
        String title, address, imgurl;
        //Platform = Platform;
        title = mResultEntities.getTitle();
        address = mResultEntities.getAddress();
        imgurl = mResultEntities.getImgList().get(0);
        CollectData collectData = new CollectData();
        collectData.setAddress(address);
        collectData.setCityName(CityName);
        collectData.setPlatform(Platform);
        collectData.setProductID(ProductID);
        collectData.setImgurl(imgurl);
        collectData.setTitle(title);
        collectData.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    Toast.makeText(ProductDetailActivity.this,
                            getResources().getString(R.string.collect_success),
                            Toast.LENGTH_SHORT).show();
                    mFabCollect.setImageResource(R.drawable.custom_detail_collecticon_selected);
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            getResources().getString(R.string.collect_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * 用户分享
     */
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(mResultEntities.getTitle());
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("https://github.com/wuyinlei/MyHearts");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(mResultEntities.getProName());
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl(mResultEntities.getShareUrl());
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("GitHub");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("https://github.com/wuyinlei/MyHearts");

// 启动分享GUI
        oks.show(this);


    }


    class MyLayoutManager extends LinearLayoutManager {

        public MyLayoutManager(Context context) {

            super(context);

        }


        @Override

        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {


            final int height = RecyclerView.LayoutManager.chooseSize(heightSpec,

                    getPaddingTop() + getPaddingBottom(),

                    ViewCompat.getMinimumHeight(mHolidayRecyclerView));

            Log.d("MyLayoutManager", "height:" + height);

            setMeasuredDimension(widthSpec, height * mOtherInfoEntities.size());

        }

    }


}
