package com.ivo.mrp.service;

import com.ivo.mrp.entity.MpsVer;
import com.ivo.mrp.entity.direct.ary.MpsAry;
import com.ivo.mrp.entity.direct.cell.MpsCell;
import com.ivo.mrp.entity.direct.lcm.MpsLcm;
import org.springframework.data.domain.Page;

import java.io.InputStream;
import java.util.List;

/**
 * MPS的服务接口
 * @author wj
 * @version 1.0
 */
public interface MpsService {

    /**
     * 获取MPS版本对象
     * @param ver mps版本
     * @return MpsVer
     */
    MpsVer getMpsVer(String ver);

    /**
     * 生成MPS版本
     * @return String
     */
    String generateMpsVer();

    /**
     * 同步LCM MPS
     */
    void syncMpsLcm();

    /**
     * 同步CELL MPS
     */
    void syncMpsCell();

    /**
     * 同步ARY MPS
     */
    void syncMpsAry();

    /**
     * 上传LCM MPS
     * @param inputStream EXCEL文件
     * @param fileName 文件名
     * @param user 用户
     */
    void importMpsLcm(InputStream inputStream, String fileName, String user);

    /**
     * 上传Ary MPS
     * @param inputStream EXCEL文件
     * @param fileName 文件名
     * @param user 用户
     */
    void importMpsAry(InputStream inputStream, String fileName, String user);

    /**
     * 上传CELL MPS
     * @param inputStream EXCEL文件
     * @param fileName 文件名
     * @param user 用户
     */
    void importMpsCell(InputStream inputStream, String fileName, String user);

    /**
     * 获取LCM DPS的所有机种
     * @param mpdVer MPS版本
     * @return  List<String>
     */
    List<String> getMpsLcmProduct(String mpdVer);

    /**
     * 获取ARY DPS的所有机种
     * @param mpdVer MPS版本
     * @return  List<String>
     */
    List<String> getMpsAryProduct(String mpdVer);

    /**
     * 获取CELL DPS的所有机种
     * @param mpdVer MPS版本
     * @return  List<String>
     */
    List<String> getMpsCellProduct(String mpdVer);

    /**
     * 获取LCM 机种的MPS数据
     * @param mpsVer MPS版本
     * @param product 机种
     * @return  List<MpsLcm>
     */
    List<MpsLcm> getMpsLcm(String mpsVer, String product);

    /**
     * 获取Ary 机种的MPS数据
     * @param mpsVer MPS版本
     * @param product 机种
     * @return  List<MpsLcm>
     */
    List<MpsAry> getMpsAry(String mpsVer,  String product);

    /**
     * 获取Cell 机种的MPS数据
     * @param mpsVer MPS版本
     * @param product 机种
     * @return  List<MpsLcm>
     */
    List<MpsCell> getMpsCell(String mpsVer,  String product);

    /**
     * 获取LCM的MPS数据
     * @param mpsVer MPS版本
     * @return  List<MpsLcm>
     */
    List<MpsLcm> getMpsLcm(String mpsVer);

    /**
     * 获取Ary的MPS数据
     * @param mpsVer MPS版本
     * @return  List<MpsLcm>
     */
    List<MpsAry> getMpsAry(String mpsVer);

    /**
     * 获取Cell的MPS数据
     * @param mpsVer MPS版本
     * @return  List<MpsLcm>
     */
    List<MpsCell> getMpsCell(String mpsVer);

    /**
     * 分页查询
     * @param page 页数
     * @param limit 分页大小
     * @param searchFab 厂别
     * @param searchVer 版本
     * @return Page<MpsVer>
     */
    Page<MpsVer> queryMpsVer(int page, int limit, String searchFab, String searchVer);

    /**
     * 获取MPS版本的数据
     * @param ver MPS版本
     * @return List
     */
    List getDpsDate(String ver);

}
