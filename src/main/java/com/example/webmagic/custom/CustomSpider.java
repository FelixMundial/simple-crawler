package com.example.webmagic.custom;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.CollectorPipeline;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.pipeline.ResultItemsCollectorPipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.thread.CountableThreadPool;
import us.codecraft.webmagic.utils.UrlUtils;
import us.codecraft.webmagic.utils.WMCollections;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yinfelix
 * @date 2020/7/18
 */
public class CustomSpider implements Runnable, Task {

    protected Downloader downloader;

    protected List<Pipeline> pipelines = new ArrayList<>();

    protected PageProcessor pageProcessor;

    protected List<Request> startRequests;

    protected Site site;

    protected String uuid;

    protected Scheduler scheduler = new QueueScheduler();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected CountableThreadPool threadPool;

    protected ExecutorService executorService;

    protected int threadNum = 1;

    protected AtomicInteger stat = new AtomicInteger(STAT_INIT);

    protected boolean exitWhenComplete = true;

    protected final static int STAT_INIT = 0;

    protected final static int STAT_RUNNING = 1;

    protected final static int STAT_STOPPED = 2;

    protected boolean spawnUrl = true;

    protected boolean destroyWhenExit = true;

    private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();

    private List<SpiderListener> spiderListeners;

    private final AtomicLong pageCount = new AtomicLong(0);

    private Date startTime;

    private int emptySleepTime = 30000;

    public static CustomSpider create(PageProcessor pageProcessor) {
        return new CustomSpider(pageProcessor);
    }

    public CustomSpider(PageProcessor pageProcessor) {
        this.pageProcessor = pageProcessor;
        this.site = pageProcessor.getSite();
    }

    public CustomSpider startUrls(List<String> startUrls) {
        checkIfRunning();
        this.startRequests = UrlUtils.convertToRequests(startUrls);
        return this;
    }

    public CustomSpider startRequest(List<Request> startRequests) {
        checkIfRunning();
        this.startRequests = startRequests;
        return this;
    }

    public CustomSpider setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Deprecated
    public CustomSpider scheduler(Scheduler scheduler) {
        return setScheduler(scheduler);
    }

    public CustomSpider setScheduler(Scheduler scheduler) {
        checkIfRunning();
        Scheduler oldScheduler = this.scheduler;
        this.scheduler = scheduler;
        if (oldScheduler != null) {
            Request request;
            while ((request = oldScheduler.poll(this)) != null) {
                this.scheduler.push(request, this);
            }
        }
        return this;
    }

    public CustomSpider pipeline(Pipeline pipeline) {
        return addPipeline(pipeline);
    }

    public CustomSpider addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }

    public CustomSpider setPipelines(List<Pipeline> pipelines) {
        checkIfRunning();
        this.pipelines = pipelines;
        return this;
    }

    public CustomSpider clearPipeline() {
        pipelines = new ArrayList<Pipeline>();
        return this;
    }

    public CustomSpider downloader(Downloader downloader) {
        return setDownloader(downloader);
    }

    public CustomSpider setDownloader(Downloader downloader) {
        checkIfRunning();
        this.downloader = downloader;
        return this;
    }

    protected void initComponent() {
        if (downloader == null) {
            this.downloader = new HttpClientDownloader();
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        downloader.setThread(threadNum);
        if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(threadNum, executorService);
            } else {
                threadPool = new CountableThreadPool(threadNum);
            }
        }
        if (startRequests != null) {
            for (Request request : startRequests) {
                addRequest(request);
            }
            startRequests.clear();
        }
        startTime = new Date();
    }

    @Override
    public void run() {
        checkRunningStat();
        initComponent();
        logger.info("Spider {} started!", getUUID());
        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
            final Request request = scheduler.poll(this);
            if (request == null) {
                if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                threadPool.execute(() -> {
                    try {
                        processRequest(request);
                        onSuccess(request);
                    } catch (Exception e) {
                        onError(request);
                        logger.error("process request " + request + " error", e);
                    } finally {
                        pageCount.incrementAndGet();
                        signalNewUrl();
                    }
                });
            }
        }
        stat.set(STAT_STOPPED);
        // release some resources
        if (destroyWhenExit) {
            close();
        }
        logger.info("Spider {} closed! {} pages downloaded.", getUUID(), pageCount.get());
    }

    protected void onError(Request request) {
        /*
        TODO 发生系统异常时，将失败请求加入队列进行重试
         */
        scheduler.push(request, this);
//        if (CollectionUtils.isNotEmpty(spiderListeners)) {
//            for (SpiderListener spiderListener : spiderListeners) {
//                spiderListener.onError(request);
//            }
//        }
    }

    protected void onSuccess(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("Spider is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }

    public void close() {
        destroyEach(downloader);
        destroyEach(pageProcessor);
        destroyEach(scheduler);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void test(String... urls) {
        initComponent();
        if (urls.length > 0) {
            for (String url : urls) {
                processRequest(new Request(url));
            }
        }
    }

    private void processRequest(Request request) {
        Page page = downloader.download(request, this);
        if (page.isDownloadSuccess()) {
            onDownloadSuccess(request, page);
        }
        /*
        在页面爬取结束后再次判断是否发生数据异常（非系统异常），以便进行循环重试
         */
        if (!page.isDownloadSuccess()) {
            logger.debug("尝试进行循环重试...");
            onDownloadFailure(request);
        }
    }

    private void onDownloadSuccess(Request request, Page page) {
        if (site.getAcceptStatCode().contains(page.getStatusCode())) {
            pageProcessor.process(page);
            extractAndAddRequests(page, spawnUrl);
            if (!page.getResultItems().isSkip()) {
                for (Pipeline pipeline : pipelines) {
                    pipeline.process(page.getResultItems(), this);
                }
            }
        } else {
            logger.info("page status code error, page {} , code: {}", request.getUrl(), page.getStatusCode());
        }
        sleep(site.getSleepTime());
        return;
    }

    private void onDownloadFailure(Request request) {
        if (site.getCycleRetryTimes() == 0) {
            sleep(site.getSleepTime());
        } else {
            // for cycle retry
            doCycleRetry(request);
        }
    }

    private void doCycleRetry(Request request) {
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            addRequest(SerializationUtils.clone(request).setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, 1));
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes < site.getCycleRetryTimes()) {
                addRequest(SerializationUtils.clone(request).setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes));
            }
        }
        sleep(site.getRetrySleepTime());
    }

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted when sleep", e);
        }
    }

    protected void extractAndAddRequests(Page page, boolean spawnUrl) {
        if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetRequests())) {
            for (Request request : page.getTargetRequests()) {
                addRequest(request);
            }
        }
    }

    private void addRequest(Request request) {
        if (site.getDomain() == null && request != null && request.getUrl() != null) {
            site.setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }

    protected void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("Spider is already running!");
        }
    }

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    public CustomSpider addUrl(String... urls) {
        for (String url : urls) {
            addRequest(new Request(url));
        }
        signalNewUrl();
        return this;
    }

    public <T> List<T> getAll(Collection<String> urls) {
        destroyWhenExit = false;
        spawnUrl = false;
        if (startRequests != null) {
            startRequests.clear();
        }
        for (Request request : UrlUtils.convertToRequests(urls)) {
            addRequest(request);
        }
        CollectorPipeline collectorPipeline = getCollectorPipeline();
        pipelines.add(collectorPipeline);
        run();
        spawnUrl = true;
        destroyWhenExit = true;
        return collectorPipeline.getCollected();
    }

    protected CollectorPipeline getCollectorPipeline() {
        return new ResultItemsCollectorPipeline();
    }

    public <T> T get(String url) {
        List<String> urls = WMCollections.newArrayList(url);
        List<T> resultItemses = getAll(urls);
        if (resultItemses != null && resultItemses.size() > 0) {
            return resultItemses.get(0);
        } else {
            return null;
        }
    }

    public CustomSpider addRequest(Request... requests) {
        for (Request request : requests) {
            addRequest(request);
        }
        signalNewUrl();
        return this;
    }

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    public void start() {
        runAsync();
    }

    public void stop() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            logger.info("Spider " + getUUID() + " stop success!");
        } else {
            logger.info("Spider " + getUUID() + " stop fail!");
        }
    }

    public CustomSpider thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    public CustomSpider thread(ExecutorService executorService, int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        this.executorService = executorService;
        return this;
    }

    public boolean isExitWhenComplete() {
        return exitWhenComplete;
    }

    public CustomSpider setExitWhenComplete(boolean exitWhenComplete) {
        this.exitWhenComplete = exitWhenComplete;
        return this;
    }

    public boolean isSpawnUrl() {
        return spawnUrl;
    }

    public long getPageCount() {
        return pageCount.get();
    }

    public CustomSpider.Status getStatus() {
        return CustomSpider.Status.fromValue(stat.get());
    }

    public enum Status {
        Init(0), Running(1), Stopped(2);

        private Status(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static CustomSpider.Status fromValue(int value) {
            for (CustomSpider.Status status : CustomSpider.Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            //default value
            return Init;
        }
    }

    public int getThreadAlive() {
        if (threadPool == null) {
            return 0;
        }
        return threadPool.getThreadAlive();
    }

    public CustomSpider setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }

    @Override
    public String getUUID() {
        if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return site.getDomain();
        }
        uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public CustomSpider setExecutorService(ExecutorService executorService) {
        checkIfRunning();
        this.executorService = executorService;
        return this;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<SpiderListener> getSpiderListeners() {
        return spiderListeners;
    }

    public CustomSpider setSpiderListeners(List<SpiderListener> spiderListeners) {
        this.spiderListeners = spiderListeners;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }
}
