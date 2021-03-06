package es.weso.ontoloci.worker;

import com.google.common.collect.ImmutableCollection;
import es.weso.ontoloci.hub.OntolociHubImplementation;
import es.weso.ontoloci.hub.build.HubBuild;
import es.weso.ontoloci.worker.build.Build;
import es.weso.ontoloci.worker.build.BuildResult;
import es.weso.ontoloci.worker.build.BuildResultStatus;
import es.weso.ontoloci.worker.test.TestCase;
import es.weso.ontoloci.worker.test.TestCaseResult;
import es.weso.ontoloci.worker.test.TestCaseResultStatus;
import es.weso.ontoloci.worker.validation.ResultValidation;
import es.weso.ontoloci.worker.validation.Validate;
import es.weso.shapeMaps.ShapeMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WorkerTest {

    // Repository for testing (https://github.com/weso/ontolo-ci-test)
    private final static String DEFAULT_OWNER = "weso";
    private final static String DEFAULT_REPO = "ontolo-ci-test";
    private final static String DEFAULT_COMMIT = "1ad23547eca78153327b4b0c005a43f0907964c1";
    private final static String FAILURE_COMMIT = "aa3ee4b0ab1766312a00ee2d1593120a38d52e5f";
    private final static String EXCEPTION_COMMIT = "b01db1082105ea0600bbf983bbff775aa563263b";
    private final static String FILE_NOT_FOUND_COMMIT = "dd34aac295450521ec0698bd8c0a768897f7915c";
    private final static String EMPTY_FILE_COMMIT = "1e874d755408e40840c90f043acc941dc705e398";

    private static Build defaultBuild;
    private static Build failureBuild;
    private static Build cancelledBuild;
    private static Build fileNotFoundBuild;
    private static Build emptyFileBuild;


    @BeforeAll
    public static void setUp(){

        Map<String,String> metadata = new HashMap<>();
        metadata.put("owner",DEFAULT_OWNER);
        metadata.put("repo",DEFAULT_REPO);

        defaultBuild = Build.from(new ArrayList<>());
        metadata.put("commit",DEFAULT_COMMIT);
        defaultBuild.setMetadata(new HashMap<>(metadata));

        failureBuild = Build.from(new ArrayList<>());
        metadata.put("commit",FAILURE_COMMIT);
        failureBuild.setMetadata(new HashMap<>(metadata));

        cancelledBuild = Build.from(new ArrayList<>());
        metadata.put("commit",EXCEPTION_COMMIT);
        cancelledBuild.setMetadata(new HashMap<>(metadata));

        fileNotFoundBuild = Build.from(new ArrayList<>());
        metadata.put("commit",FILE_NOT_FOUND_COMMIT);
        fileNotFoundBuild.setMetadata(new HashMap<>(metadata));

        emptyFileBuild = Build.from(new ArrayList<>());
        metadata.put("commit",EMPTY_FILE_COMMIT);
        emptyFileBuild.setMetadata(new HashMap<>(metadata));
    }


    @Test
    public void workerExecutorTest(){

        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(defaultBuild);

        assertNotNull(buildResult);
        assertTrue(buildResult.getTestCaseResults().size()>0);

    }

    @Test
    public void workerSequentialTest(){

        OntolociHubImplementation ontolociHubImplementation = new OntolociHubImplementation();
        HubBuild hubBuild = defaultBuild.toHubBuild();
        hubBuild = ontolociHubImplementation.addTestsToBuild(hubBuild);
        defaultBuild = Build.from(hubBuild);

        WorkerSequential workerSequential = new WorkerSequential();
        BuildResult buildResult = workerSequential.executeBuild(defaultBuild);

        assertNotNull(buildResult);
        assertTrue(buildResult.getTestCaseResults().size()>0);

    }

    @Test
    public void metadataTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(defaultBuild);
        assertNotNull(buildResult.getMetadata());
    }

    @Test
    public void buildResultSuccessStatusTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(defaultBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.SUCCESS);
    }


    @Test
    public void testCaseResultSuccessStatusTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(defaultBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.SUCCESS);
        for(TestCaseResult testCaseResult:buildResult.getTestCaseResults()){
            assertEquals(testCaseResult.getStatus(), TestCaseResultStatus.SUCCESS);
        }
    }

    @Test
    public void buildResultFailureStatusTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(failureBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.FAILURE);
    }

    @Test
    public void testCaseResultFailureStatusTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(failureBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.FAILURE);
        for(TestCaseResult testCaseResult:buildResult.getTestCaseResults()){
            assertEquals(testCaseResult.getStatus(), TestCaseResultStatus.FAILURE);
        }
    }

    @Test
    public void buildResultCancelledStatusTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(cancelledBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.CANCELLED);
    }

    @Test
    public void buildResultFileNotFoundExceptionTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(fileNotFoundBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.CANCELLED);
        assertEquals(buildResult.getMetadata().get("exceptions"), "true");
        assertEquals(buildResult.getMetadata().get("checkTitle"), "FileNotFound");
    }

    @Test
    public void buildResultEmptyFileExceptionTest() {
        WorkerSequential workerSequential = new WorkerSequential();
        WorkerExecutor workerExecutor = WorkerExecutor.from(workerSequential);
        BuildResult buildResult = workerExecutor.executeBuild(emptyFileBuild);
        assertEquals(buildResult.getStatus(), BuildResultStatus.CANCELLED);
        assertEquals(buildResult.getMetadata().get("exceptions"), "true");
        assertEquals(buildResult.getMetadata().get("checkTitle"), "EmptyContentFile");
    }


    @Test
    public void validationTest(){

        OntolociHubImplementation ontolociHubImplementation = new OntolociHubImplementation();
        HubBuild hubBuild = defaultBuild.toHubBuild();
        hubBuild = ontolociHubImplementation.addTestsToBuild(hubBuild);
        defaultBuild = Build.from(hubBuild);

        TestCase testCase = defaultBuild.getTestCases().iterator().next();
        Validate v = new Validate();
        ShapeMap resultValidation = v.validateStr(
                testCase.getOntology(),
                testCase.getInstances(),
                testCase.getSchema(),
                testCase.getProducedShapeMap()).unsafeRunSync();

        assertNotNull(resultValidation);
        assertTrue(resultValidation.toJson().spaces2().length()>0);
    }

    @Test
    public void validationWithExpectedResultTest(){

        OntolociHubImplementation ontolociHubImplementation = new OntolociHubImplementation();
        HubBuild hubBuild = defaultBuild.toHubBuild();
        hubBuild = ontolociHubImplementation.addTestsToBuild(hubBuild);
        defaultBuild = Build.from(hubBuild);

        TestCase testCase = defaultBuild.getTestCases().iterator().next();
        Validate v = new Validate();
        ResultValidation resultValidation = new ResultValidation();

        assertNull(resultValidation.getResultShapeMap());
        assertNull(resultValidation.getExpectedShapeMap());

        resultValidation = v.validateStrResultValidation(
                testCase.getOntology(),
                testCase.getInstances(),
                testCase.getSchema(),
                testCase.getProducedShapeMap(),
                testCase.getExpectedShapeMap()).unsafeRunSync();

        assertNotNull(resultValidation.getResultShapeMap());
        assertNotNull(resultValidation.getExpectedShapeMap());

    }


}
