package zookeeper.service;

import java.util.List;

public interface IZKOperationService {

    boolean checkNodeExist(String path);

    void createNode(String path, String data) throws Exception;

    void deleteNode(String path) throws Exception;

    String getNodeData(String path);

    void updateNodeData(String path, String newData) throws Exception;

    List<String> getChildrens(String path) throws Exception;

    void nodeWatcher(String path) throws Exception;
}
