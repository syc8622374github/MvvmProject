package com.goldze.sign.ui;

import android.app.Application;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;

import com.goldze.base.contract._Login;
import com.goldze.base.global.SPKeyGlobal;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Created by goldze on 2018/6/21 0021.
 */

public class LoginViewModel extends BaseViewModel {
    //用户名的绑定
    public ObservableField<String> userName = new ObservableField<>("");
    //验证码绑定
    public ObservableField<String> checkCode = new ObservableField<>("");
    //验证码按钮绑定
    public ObservableField<String> checkCodeBtn = new ObservableField<>("获取验证码");
    //封装一个界面发生改变的观察者
    public UIChangeObservable uc = new UIChangeObservable();

    public class UIChangeObservable {
        //密码开关观察者
        //public ObservableBoolean pSwitchObservable = new ObservableBoolean(false);
        //验证码按钮观察者
        public ObservableBoolean pCheckCodeBtnObservable = new ObservableBoolean(false);
    }

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }


    //密码显示开关  (你可以尝试着狂按这个按钮,会发现它有防多次点击的功能)
    /*public BindingCommand passwordShowSwitchOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //让观察者的数据改变,在View层的监听则会被调用
            uc.pSwitchObservable.set(!uc.pSwitchObservable.get());
        }
    });*/

    //登录按钮的点击事件
    public BindingCommand loginOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            login();
        }
    });

    //验证码按钮的点击事件
    public BindingCommand checkCodeOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            sendCheckCode();
        }
    });

    public BindingCommand userNameTextChangedCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {

        }
    });

    /**
     * 发送验证码
     */
    private void sendCheckCode() {
        /*RaJava模拟一个延迟操作
          60秒倒计时
         */
        Observable.interval(60,60, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return aLong-1;
                    }
                }).subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long aLong) throws Exception {
                //如果倒计时为0 重置获取验证码状态
                checkCodeBtn.set(aLong+"");
                if(aLong==0){
                    checkCodeBtn.set("重新获取");
                    uc.pCheckCodeBtnObservable.set(true);
                }
            }
        });
    }

    /**
     * 网络模拟一个登陆操作
     */
    private void login() {
        if (TextUtils.isEmpty(userName.get())) {
            ToastUtils.showShort("请输入账号！");
            return;
        }
        if (TextUtils.isEmpty(checkCode.get())) {
            ToastUtils.showShort("请输入验证码！");
            return;
        }

        //RaJava模拟一个延迟操作
        Observable.just("")
                .delay(3, TimeUnit.SECONDS) //延迟3秒
                .compose(RxUtils.bindToLifecycle(getLifecycleProvider()))//界面关闭自动取消
                .compose(RxUtils.schedulersTransformer()) //线程调度
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        showDialog();
                    }
                })
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        dismissDialog();
                        //保存用户信息
                        SPUtils.getInstance().put(SPKeyGlobal.USER_INFO, userName.get());
                        _Login _login = new _Login();
                        //采用ARouter+RxBus实现组件间通信
                        RxBus.getDefault().post(_login);
                        //关闭页面
                        finish();
                    }
                });
    }
}
