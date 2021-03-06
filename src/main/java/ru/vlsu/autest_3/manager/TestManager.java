package ru.vlsu.autest_3.manager;

import ru.vlsu.autest_3.dao.model.ActionDo;
import ru.vlsu.autest_3.dao.model.TestCaseDo;
import ru.vlsu.autest_3.dao.model.TestSetDo;

import java.util.List;

public interface TestManager {

    List<TestCaseDo> getTestCaseBySetId(long id);

    TestSetDo getTestSetById(long id);

    TestSetDo getFullTestSetById(long id);

    TestSetDo saveTestSet(TestSetDo testSet, Boolean withResult);

    TestCaseDo saveTestCase(TestCaseDo testCase, String authorization,Boolean withResult);

    ActionDo saveAction(ActionDo action, Boolean withResult);
}
