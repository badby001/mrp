package com.ivo.mrp.repository;

import com.ivo.mrp.entity.direct.ary.DemandAry;
import com.ivo.mrp.key.DemandKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wj
 * @version 1.0
 */
public interface DemandAryRepository extends JpaRepository<DemandAry, DemandKey> {

    /**
     * 获取MRP需求所有的料号
     * @param ver MRP版本
     * @return List<String>
     */
    @Query(value = "select DISTINCT material from mrp3_demand_ARY where ver=:ver " +
            "UNION " +
            "select DISTINCT material from mrp3_demand_ary_oc where ver=:ver", nativeQuery = true)
    List<String> getMaterial(String ver);


    /**
     * 汇总料号需求
     * @param ver MRP版本
     * @param material 料号
     * @return List<Map<Date, Double>>
     */
    @Query(value = "SELECT t.fab_date as fabDate, sum(t.demand_qty) as demandQty " +
            "    from ( " +
            "            select fab_date,d.demand_qty from mrp3_demand_ary d where d.ver=:ver and d.material=:material and d.demand_qty>0 " +
            "            UNION " +
            "            select fab_date,d.demand_qty from mrp3_demand_ary_oc d where d.ver=:ver and d.material=:material and d.demand_qty>0 " +
            "    ) t " +
            "    GROUP BY fab_date", nativeQuery = true)
    List<Map> getDemandQtyAry(String ver, String material);

    /**
     * 汇总料号、日期需求
     * @param ver MRP版本
     * @param material 料号
     * @param fabDate 日期
     * @return Double
     */
    @Query(value = "select sum(t.demand_qty) as demandQty from ( " +
            "select fab_date,d.demand_qty from mrp3_demand_ary d where d.ver=:ver and d.material=:material and d.demand_qty>0 and d.fab_date=:fabDate " +
            "UNION " +
            "select fab_date,d.demand_qty from mrp3_demand_ary_oc d where d.ver=:ver and d.material=:material and d.demand_qty>0 and d.fab_date=:fabDate " +
            ")t", nativeQuery = true)
    Double getDemandQtyAry(String ver, String material, Date fabDate);
}
