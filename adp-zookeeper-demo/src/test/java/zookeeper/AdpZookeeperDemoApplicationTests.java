package zookeeper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zookeeper.service.IZKOperationService;

@SpringBootTest
class AdpZookeeperDemoApplicationTests {
    private static final Logger logger = LoggerFactory.getLogger(AdpZookeeperDemoApplication.class);

    @Autowired
    private IZKOperationService zkOperationService;

    @Test
    void contextLoads() throws Exception {
        String path = "/curator";
        //判断节点是否存在
        logger.info(zkOperationService.checkNodeExist(path) + "");
        //新建节点，并给数据
        zkOperationService.createNode(path, "curator");
        //获取节点数据
        String data = zkOperationService.getNodeData(path);
        logger.info("data=" + data);
        //更新数据
        zkOperationService.updateNodeData(path, "curator-new");
        String newData = zkOperationService.getNodeData(path);
        logger.info("new data=" + newData);
        //删除节点
        //zkOperationService.deleteNode(path);
        //节点监听
        zkOperationService.nodeWatcher(path);
        zkOperationService.updateNodeData(path, "1 data");
        Thread.sleep(1000);
        zkOperationService.updateNodeData(path, "2 data");
        Thread.sleep(1000);
        zkOperationService.updateNodeData(path, "3 data");
        Thread.sleep(1000);
        zkOperationService.updateNodeData(path, "4 data");


    }

}
