package kanghb.com.dagger2demo;

import javax.inject.Inject;

/**
 * 创建时间：2018/8/23
 * 编写人：kanghb
 * 功能描述：学生（需求依赖方）
 */
public class Student {

//    @QualifierA
    @Inject
    Major major;
//    @QualifierB
    @Inject
    Major major2;

    public Student() {
        DaggerStudentComponent.builder().makeMajorModule(new MakeMajorModule()).build().inject(this);
    }

    @Override
    public String toString() {
        return "Student{" +
                "major=" + major +
                '}';
    }

    public Major getMajorA() {
        return major;
    }
    public Major getMajorB() {
        return major2;
    }
}
