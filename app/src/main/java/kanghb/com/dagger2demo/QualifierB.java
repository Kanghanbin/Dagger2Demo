package kanghb.com.dagger2demo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * 创建时间：2018/8/24
 * 编写人：kanghb
 * 功能描述：
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface QualifierB {

}
