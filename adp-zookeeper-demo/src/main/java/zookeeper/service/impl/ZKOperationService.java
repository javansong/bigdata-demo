package zookeeper.service.impl;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zookeeper.service.IZKOperationService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * zookeeper操作
 */

@Service
public class ZKOperationService implements IZKOperationService {
    private static final Logger logger = LoggerFactory.getLogger(ZKOperationService.class);
    private static CuratorFramework curatorClient = null;

    @Value("${zk.connectString}")
    private String connectString;
    @Value("${zk.maxRetries}")
    private int maxRetries;
    @Value("${zk.sessionTimeoutMs}")
    private int sessionTimeoutMs;
    @Value("${zk.connectionTimeoutMs}")
    private int connectionTimeoutMs;
    @Value("${zk.baseSleepTimeMs}")
    private int baseSleepTimeMs;
    @Value("${zk.namespace}")
    private String namespace;

    //Zookeeper Curator客户端的初始化
    @PostConstruct
    private void initConnect() {
        //设置重试策略
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, baseSleepTimeMs);
        curatorClient = CuratorFrameworkFactory.builder().connectString(connectString)
                .sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy).namespace(namespace).build();
        try {
            curatorClient.start();
            CuratorFrameworkState curatorFrameworkState = curatorClient.getState();
            if (curatorFrameworkState == CuratorFrameworkState.STARTED) {
                logger.info("zookeeper连接初始化成功!");
            } else {
                logger.error("zookeeper连接初始化失败!");
            }
        } catch (Exception e) {
            logger.error("zookeeper连接初始化失败,error=" + e.getLocalizedMessage());
        }
    }

    @Override
    //判断节点是否存在
    public boolean checkNodeExist(String path) {
        try {
            Stat stat = curatorClient.checkExists().forPath(path);
            if (stat != null) {
                return true;
            }
        } catch (Exception e) {
            logger.error("error=" + e.getMessage());
        }
        return false;
    }

    @Override
    //创建节点
    public void createNode(String path, String data) throws Exception {
        curatorClient.create().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes());

    }

    @Override
    //删除节点
    public void deleteNode(String path) throws Exception {
        //curatorClient.delete().deletingChildrenIfNeeded().forPath(path);
        curatorClient.delete().forPath(path);

    }

    @Override
    //获取节点数据
    public String getNodeData(String path) {
        String data = "";
        try {
            data = new String(curatorClient.getData().forPath(path));
            //获取状态
           /* Stat stat = new Stat();
            curatorClient.getData().storingStatIn(stat).forPath(path);*/
        } catch (Exception e) {
            logger.error("获取数据失败");
        }
        return data;
    }

    @Override
    //更新节点数据
    public void updateNodeData(String path, String newData) throws Exception {
        curatorClient.setData().forPath(path, newData.getBytes());
    }

    @Override
    //获取子节点列表
    public List<String> getChildrens(String path) throws Exception {
        return curatorClient.getChildren().forPath(path);
    }

    @Override
    //节点监听
    public void nodeWatcher(String path) throws Exception {
        NodeCache nodeCache = new NodeCache(curatorClient, path);
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                logger.info("触发监听回调，当前节点数据为：" + new String(nodeCache.getCurrentData().getData()));
            }
        });
    }


    @PreDestroy
    //Zookeeper连接的关闭
    public void destoryConnect() {
        if (curatorClient != null) {
            curatorClient.close();
        }
    }
}
