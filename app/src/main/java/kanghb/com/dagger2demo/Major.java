package kanghb.com.dagger2demo;

import javax.inject.Inject;

/**
 * 创建时间：2018/8/23
 * 编写人：kanghb
 * 功能描述：专业（依赖提供方）
 */
public class Major {
    private String teacher;


    public Major(String teacher) {
        this.teacher = teacher;
        System.out.println("Create a Major");
    }

    public void test(){
        System.out.println("我是teacher为"+ teacher + "Major的方法");
    }
}
