package com.ivo.mrp.repository;

import com.ivo.mrp.entity.direct.cell.BomCellMtrl;
import com.ivo.mrp.key.BomCellMtrlKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * @author wj
 * @version 1.0
 */
public interface BomCellMtrlRepository extends JpaRepository<BomCellMtrl, BomCellMtrlKey> {


    /**
     * 筛选机种、是否使用
     * @param product 机种
     * @param useFlag 是否使用
     * @return  List<BomCellMtrl>
     */
    List<BomCellMtrl> findByProductAndUseFlag(String product, boolean useFlag);
}
