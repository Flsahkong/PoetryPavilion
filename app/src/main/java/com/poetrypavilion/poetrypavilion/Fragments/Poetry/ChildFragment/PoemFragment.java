package com.poetrypavilion.poetrypavilion.Fragments.Poetry.ChildFragment;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.alexvasilkov.foldablelayout.UnfoldableView;
import com.poetrypavilion.poetrypavilion.Acache.ACache;
import com.poetrypavilion.poetrypavilion.Adapters.PaintingsAdapter;
import com.poetrypavilion.poetrypavilion.Beans.Poetry.PoemDetail;
import com.poetrypavilion.poetrypavilion.Fragments.BaseFragment;
import com.poetrypavilion.poetrypavilion.R;
import com.poetrypavilion.poetrypavilion.ViewModels.Poetry.PoemViewModel;
import com.poetrypavilion.poetrypavilion.databinding.PoemFragmentBinding;

import java.util.List;
import java.util.Objects;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class PoemFragment extends BaseFragment<PoemFragmentBinding>
        implements WaveSwipeRefreshLayout.OnRefreshListener{
    private PoemViewModel poemViewModel;
    private ACache aCache;
    //设置打开app的时候，refresh_time是1
    private int refresh_time = 1;

    @Override
    public int setViewXml() {
        return R.layout.poem_fragment;
    }

    @Override
    public void OtherProcess() {
        //设置缓存对象
        aCache = ACache.get(getContext());

        bindingView.poemInclude.poemBoardDetailLayout.setVisibility(View.INVISIBLE);
        //设置详情页面不能使用手势收起，从而使其适配scrollview
        bindingView.poemInclude.poemDetailUnfoldableview.setGesturesEnabled(false);
        bindingView.poemInclude.poemDetailUnfoldableview.setOnFoldingListener(new UnfoldableView.SimpleFoldingListener() {
            @Override
            public void onUnfolding(UnfoldableView unfoldableView) {
                //这一句在点击listview里面的具体部件的时候首先执行
                bindingView.poemInclude.poemBoardDetailLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUnfolded(UnfoldableView unfoldableView) {
                //这个在打开完成具体的页面之后调用

            }

            @Override
            public void onFoldingBack(UnfoldableView unfoldableView) {
                //这个在开始关闭的时候调用
            }

            @Override
            public void onFoldedBack(UnfoldableView unfoldableView) {
                //这个在关闭即将结束的时候调用
                bindingView.poemInclude.poemBoardDetailLayout.setVisibility(View.INVISIBLE);
            }
        });

        //设置下拉刷新
        bindingView.poemInclude.poemBoardRefresh.setOnRefreshListener(this);
        bindingView.poemInclude.poemBoardRefresh.setWaveColor(Color.argb(230,34,153,84));
        bindingView.poemInclude.poemBoardRefresh.setColorSchemeColors(Color.WHITE, Color.WHITE);

        //从缓存加载数据
        loadData();
    }

    public void openDetails(View coverView, PoemDetail item) {
        bindingView.poemInclude.poemBoardDetailUsername.setText(item.getEditor());
        bindingView.poemInclude.poemBoardDetailUserUplodeTime.setText(item.getDynasty());
        bindingView.poemInclude.detailsTitle.setText(item.getTitle());
        bindingView.poemInclude.detailsPoemText.setText(item.getPoetry());
        bindingView.poemInclude.detailsPoemNote.setText(item.getNote());
        bindingView.poemInclude.detailsPoemTranslate.setText(item.getTranslate());
//        RichText.from(item.getTranslate()).into(bindingView.poemInclude.detailsPoemTranslate);
        bindingView.poemInclude.detailsPoemShangxi.setText(item.getShangxi());
        bindingView.poemInclude.poemDetailUnfoldableview.unfold(
                coverView, bindingView.poemInclude.poemBoardDetailLayout);

        /*
        这里当时是设置具体的图片内容的地方，里面包含了描述的格式
        SpannableBuilder builder = new SpannableBuilder(PoemView.getContext());
        builder.createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(R.string.year).append(": ")
                .clearStyle()
                .append(painting.getYear()).append("\n")
                .createStyle().setFont(Typeface.DEFAULT_BOLD).apply()
                .append(R.string.location).append(": ")
                .clearStyle()
                .append(painting.getLocation());
        description.setText(builder.build());*/
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        poemViewModel = ViewModelProviders.of(this).get(PoemViewModel.class);

        poemViewModel.getPoemDetails().observe(this ,poemDetails->{
            PaintingsAdapter adapter = new PaintingsAdapter(poemDetails);
            bindingView.poemInclude.poemBoardsListView.setAdapter(adapter);

            adapter.setOnXXClickListener(new PaintingsAdapter.XXListener(){
                @Override
                public void onXXClick(View view, PoemDetail item) {
                    openDetails(view,item);
                }
            });
        });
    }

    @Override
    public void loadData(){
        //打开app之后加载数据
        //调用poemViewModel方法获取数据
        new Thread(
                ()->{
                    try {
                        poemViewModel.setOnLoadFromAcacheListener(new PoemViewModel.LoadFromAcacheListener() {
                            @Override
                            public void onLoadOk(List<PoemDetail> poemDetails) {
                                // TODO 这里还要写处理底部UI动画的命令
                                Objects.requireNonNull(getActivity()).runOnUiThread(
                                        ()->poemViewModel.getPoemDetails().setValue(poemDetails)
                                );
                            }
                            @Override
                            public void onLoadFailure() {
                                // TODO 这里还要写处理底部UI动画的命令
                                Objects.requireNonNull(getActivity()).runOnUiThread(
                                        () -> Toast.makeText(getContext(),
                                                "没有缓存数据，请下拉刷新！",Toast.LENGTH_LONG).show());
                            }
                            //TODO 这里写一个加载缓存失败之后自动刷新
                        });
                        poemViewModel.getPoemsFromRepository(0,PoemViewModel.TYPE.FIRSTLOAD,aCache);
                    }catch(Exception e){
                        //出错处理
                        Objects.requireNonNull(getActivity()).runOnUiThread(
                                () -> Toast.makeText(getContext(),
                                        "加载缓存数据时出错！",Toast.LENGTH_LONG).show());
                        //TODO 这里写一个加载缓存失败之后自动刷新
                    }
                }
        ).start();

    }

    @Override
    public void refreshData(){
        //每刷新一次，页面数目加一
        refresh_time++;
        //下拉刷新后刷新数据
        //使用子线程加载数据
        new Thread(() -> {
            try{
                poemViewModel.setOnLoadFromHttpListener(new PoemViewModel.LoadFromHttpListener() {
                    @Override
                    public void onLoadOk(boolean IsOldPoemListNull,List<PoemDetail> list) {
                        //创建新线程取消刷新的UI,由于数据加载过快，导致刷新动画刚显示即刷新成功，所以这里延迟1s关闭刷新动画
                        int delay_time;
                        if(!IsOldPoemListNull){
                            delay_time = 1000;
                        }else {
                            delay_time=400;
                        }
                        Objects.requireNonNull(getActivity()).runOnUiThread(()->
                                new Handler().postDelayed(() -> {
                                    bindingView.poemInclude.poemBoardRefresh.setRefreshing(false);
                                    if(!IsOldPoemListNull){
                                        //如果刷新前就有数据
                                        poemViewModel.getPoemDetails().setValue(poemViewModel.getPoemDetails().getValue());
                                    }else {
                                        //如果刷新之前无数据
                                        poemViewModel.getPoemDetails().setValue(list);
                                    }
                                }, delay_time)
                        );
                    }
                    @Override
                    public void onLoadFailure() {
                        //通过handler取消刷新的UI，并且显示刷新失败
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                            bindingView.poemInclude.poemBoardRefresh.setRefreshing(false);
                            Toast.makeText(getContext(), "刷新失败，请稍后重试", Toast.LENGTH_LONG).show();
                        });
                    }
                });
                poemViewModel.getPoemsFromRepository(refresh_time,PoemViewModel.TYPE.REFRESH,aCache);
            }catch(Exception e){
                //出错处理
                Objects.requireNonNull(getActivity()).runOnUiThread(
                        () -> Toast.makeText(getContext(),
                                "刷新失败，请稍后重试！",Toast.LENGTH_LONG).show());
            }
        }){
        }.start();
    }
    public boolean onBackPressed() {
        if (bindingView.poemInclude.poemDetailUnfoldableview != null
                && (bindingView.poemInclude.poemDetailUnfoldableview.isUnfolded()
                || bindingView.poemInclude.poemDetailUnfoldableview.isUnfolding())) {
            bindingView.poemInclude.poemDetailUnfoldableview.foldBack();
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onRefresh() {
        //由于是刷新，因此需要向后端请求数据而不是加载缓存数据，所以调用freshdata方法
        //异步加载数据，通过监听得到返回的数据
        refreshData();
    }
}
