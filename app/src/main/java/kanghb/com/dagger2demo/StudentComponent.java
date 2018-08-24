package kanghb.com.dagger2demo;

import dagger.Component;

/**
 * 创建时间：2018/8/24
 * 编写人：kanghb
 * 功能描述：
 */
@StudentScope
@Component(modules = {MakeMajorModule.class})
public interface StudentComponent {
    void inject(Student student);
}
