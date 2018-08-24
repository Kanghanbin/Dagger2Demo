package kanghb.com.dagger2demo;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;

/**
 * 创建时间：2018/8/24
 * 编写人：kanghb
 * 功能描述：
 */
@Module
public class MakeMajorModule {
    public MakeMajorModule() {
    }
//    @QualifierA
    @StudentScope
    @Provides
    public Major provideMajorA(){
        return new Major("kanghanbin");
    }
//    @QualifierB
//    @Provides
//    public Major provideMajorB(){
//        return new Major("fenglin");
//    }
}
