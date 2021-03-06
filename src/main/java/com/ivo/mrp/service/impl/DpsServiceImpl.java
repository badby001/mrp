package com.ivo.mrp.service.impl;

import com.ivo.common.utils.ResultUtil;
import com.ivo.mrp.entity.*;
import com.ivo.mrp.entity.direct.ary.DpsAry;
import com.ivo.mrp.entity.direct.ary.DpsAryOc;
import com.ivo.mrp.entity.direct.cell.DpsCell;
import com.ivo.mrp.entity.packaging.BomPackage;
import com.ivo.mrp.entity.direct.lcm.DpsLcm;
import com.ivo.mrp.entity.packaging.DpsPackage;
import com.ivo.mrp.repository.*;
import com.ivo.mrp.service.BomPackageService;
import com.ivo.mrp.service.DpsService;
import com.ivo.rest.RestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wj
 * @version 1.0
 */
@Slf4j
@Service
public class DpsServiceImpl implements DpsService {

    private DpsVerRepository dpsVerRepository;

    private DpsLcmRepository dpsLcmRepository;

    private DpsAryRepository dpsAryRepository;

    private DpsCellRepository dpsCellRepository;

    private DpsPackageRepository dpsPackageRepository;

    private DpsAryOcRepository dpsAryOcRepository;

    private RestService restService;

    private BomPackageService bomPackageService;

    @Autowired
    public DpsServiceImpl(DpsVerRepository dpsVerRepository, DpsLcmRepository dpsLcmRepository, DpsAryRepository dpsAryRepository,
                          DpsCellRepository dpsCellRepository, DpsPackageRepository dpsPackageRepository,
                          DpsAryOcRepository dpsAryOcRepository,
                          RestService restService, BomPackageService bomPackageService) {
        this.dpsVerRepository = dpsVerRepository;
        this.dpsLcmRepository = dpsLcmRepository;
        this.dpsAryRepository = dpsAryRepository;
        this.dpsCellRepository = dpsCellRepository;
        this.dpsPackageRepository = dpsPackageRepository;
        this.dpsAryOcRepository = dpsAryOcRepository;
        this.restService = restService;
        this.bomPackageService = bomPackageService;
    }

    @Override
    public DpsVer getDpsVer(String ver) {
        return dpsVerRepository.findById(ver).orElse(null);
    }

    @Override
    public List<DpsAry> getDpsAry(String ver) {
        return dpsAryRepository.findByVer(ver);
    }

    @Override
    public List<DpsCell> getDpsCell(String ver) {
        return dpsCellRepository.findByVer(ver);
    }

    @Override
    public List<DpsLcm> getDpsLcm(String ver) {
        return dpsLcmRepository.findByVer(ver);
    }

    @Override
    public List<DpsPackage> getDpsPackage(String ver) {
        return dpsPackageRepository.findByVer(ver);
    }

    @Override
    public List<DpsAryOc> getDpsAryOc(String ver) {
        return dpsAryOcRepository.findByVer(ver);
    }

    private List<DpsVer> getDpsVerByFileVer(String ver, String type) {
        return dpsVerRepository.findByDpsFileAndType(ver, type);
    }

    @Override
    public String generateDpsVer() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String str = String.format("%03d", (dpsVerRepository.findAll().size()+1)%1000);
        return  sdf.format(new java.util.Date())+str;
    }

    @Override
    public void syncDpsLcm() {
        log.info("同步DPS LCM数据 >> START");
        List<String> verList = restService.getDpsLcmVer();
        if(verList == null || verList.size()==0) return;
        for(String ver : verList) {
            List list = getDpsVerByFileVer(ver, DpsVer.Type_Lcm);
            if(list == null || list.size() == 0) {
                syncDpsLcm(ver);
            }
        }
        log.info("同步DPS LCM数据 >> END");
    }

    @Override
    public void syncDpsLcm(String ver) {
        log.info("同步LCM DPS版本" + ver);
        List<Map> mapList = restService.getDpsLcm(ver);
        if(mapList==null || mapList.size() == 0) return;
        String fab = (String) mapList.get(0).get("fab_id");
        fab = fab.toUpperCase();
        String dps_ver = generateDpsVer();
        DpsVer dpsVer = new DpsVer();
        dpsVer.setFab(fab);
        dpsVer.setDpsFile(ver);
        dpsVer.setVer(dps_ver);
        dpsVer.setSource(DpsVer.Source_DPS);
        dpsVer.setType(DpsVer.Type_Lcm);
        dpsVer.setCreator("SYS");
        Date startDate = null;
        Date endDate = null;
        List<DpsLcm> dpsLcmList = new ArrayList<>();
        for(Map map : mapList) {
            String product = (String) map.get("prod_id");
            String project = (String) map.get("model_id");
            double demandQty = (Integer) map.get("bpc_qty");
            Date fabDate = (Date) map.get("fab_date");
            DpsLcm dpsLcm = new DpsLcm();
            dpsLcm.setVer(dps_ver);
            dpsLcm.setFab(fab);
            dpsLcm.setProduct(product);
            dpsLcm.setProject(project);
            dpsLcm.setCreator("SYS");
            dpsLcm.setFabDate(fabDate);
            dpsLcm.setDemandQty(demandQty);
            dpsLcm.setMemo("DPS同步");
            dpsLcmList.add(dpsLcm);

            //日期区间
            if(startDate == null || startDate.after(fabDate)) {
                startDate =fabDate;
            }
            if(endDate == null || endDate.before(fabDate)) {
                endDate = fabDate;
            }
        }
        dpsVer.setStartDate(startDate);
        dpsVer.setEndDate(endDate);
        dpsVerRepository.save(dpsVer);
        dpsLcmRepository.saveAll(dpsLcmList);
    }

    @Override
    public void syncDpsCell() {
        log.info("同步DPS CELL数据 >> START");
        List<String> verList = restService.getDpsCellAryVer();
        if(verList == null || verList.size()==0) return;
        for(String ver : verList) {
            List list = getDpsVerByFileVer(ver, DpsVer.Type_Cell);
            if(list == null || list.size() == 0) {
                syncDpsCell(ver);
            }
        }
        log.info("同步DPS CELL数据 >> END");
    }

    @Override
    public void syncDpsCell(String ver) {
        log.info("同步CELL DPS版本" + ver);
        List<Map> mapList = restService.getDpsCell(ver);
        if(mapList==null || mapList.size() == 0) return;
        String fab = "CELL";
        String dps_ver = generateDpsVer();
        DpsVer dpsVer = new DpsVer();
        dpsVer.setFab(fab);
        dpsVer.setDpsFile(ver);
        dpsVer.setVer(dps_ver);
        dpsVer.setSource(DpsVer.Source_Cell);
        dpsVer.setType(DpsVer.Type_Cell);
        dpsVer.setCreator("SYS");
        Date startDate = null;
        Date endDate = null;
        List<DpsCell> dpsCellList = new ArrayList<>();
        for(Map map : mapList) {
            String project = (String) map.get("model_id_dps");
            String outputName = (String) map.get("output_name");
            double demandQty = (Double) map.get("qty");
            Date fabDate = (Date) map.get("fab_date");
            DpsCell dpsCell = new DpsCell();
            dpsCell.setVer(dps_ver);
            dpsCell.setFab(fab);
            dpsCell.setProject(project);
            dpsCell.setOutputName(outputName);
            String product;
            if(StringUtils.contains(outputName, ",")) {
                product = outputName.substring(0, outputName.indexOf(","));
            } else {
                product = outputName;
            }
            dpsCell.setProduct(product);
            dpsCell.setCreator("SYS");
            dpsCell.setFabDate(fabDate);
            dpsCell.setDemandQty(demandQty);
            dpsCell.setMemo("DPS同步");
            dpsCellList.add(dpsCell);

            //日期区间
            if(startDate == null || startDate.after(fabDate)) {
                startDate =fabDate;
            }
            if(endDate == null || endDate.before(fabDate)) {
                endDate = fabDate;
            }
        }
        dpsVer.setStartDate(startDate);
        dpsVer.setEndDate(endDate);
        dpsVerRepository.save(dpsVer);
        dpsCellRepository.saveAll(dpsCellList);
    }

    @Override
    public void syncDpsAry() {
        log.info("同步ARY DPS数据 >> START");
        List<String> verList = restService.getDpsCellAryVer();
        if(verList == null || verList.size()==0) return;
        for(String ver : verList) {
            List list = getDpsVerByFileVer(ver, DpsVer.Type_Ary);
            if(list == null || list.size() == 0) {
                syncDpsAry(ver);
            }
        }
        log.info("同步ARY DPS数据 >> END");
    }

    @Override
    public void syncDpsAry(String ver) {
        log.info("同步ARY DPS版本" + ver);
        List<Map> mapList = restService.getDpsAry(ver);
        if(mapList==null || mapList.size() == 0) return;
        String fab = "ARY";
        String dps_ver = generateDpsVer();
        DpsVer dpsVer = new DpsVer();
        dpsVer.setFab(fab);
        dpsVer.setDpsFile(ver);
        dpsVer.setVer(dps_ver);
        dpsVer.setSource(DpsVer.Source_Array);
        dpsVer.setType(DpsVer.Type_Ary);
        dpsVer.setCreator("SYS");
        Date startDate = null;
        Date endDate = null;
        List<DpsAry> dpsAryArrayList = new ArrayList<>();
        for(Map map : mapList) {
            String product = (String) map.get("model_id_dps");
            double demandQty = (Double) map.get("qty");
            Date fabDate = (Date) map.get("fab_date");
            DpsAry dpsAry = new DpsAry();
            dpsAry.setVer(dps_ver);
            dpsAry.setFab(fab);
            dpsAry.setProduct(product);
            dpsAry.setCreator("SYS");
            dpsAry.setFabDate(fabDate);
            dpsAry.setDemandQty(demandQty);
            dpsAry.setMemo("DPS同步");
            dpsAryArrayList.add(dpsAry);

            //日期区间
            if(startDate == null || startDate.after(fabDate)) {
                startDate =fabDate;
            }
            if(endDate == null || endDate.before(fabDate)) {
                endDate = fabDate;
            }
        }
        dpsVer.setStartDate(startDate);
        dpsVer.setEndDate(endDate);
        dpsVerRepository.save(dpsVer);
        dpsAryRepository.saveAll(dpsAryArrayList);
        syncDpsAryOc(dps_ver, ver);
    }

    @Override
    public void syncDpsAryOc(String dps_ver, String fileVer) {
        log.info("同步ARY OC DPS版本" + fileVer);
        String fab = "ARY";
        List<Map> mapList = restService.getDpsAryOc(fileVer);
        List<DpsAryOc> dpsAryOcList = new ArrayList<>();
        for(Map map : mapList) {
            String product = (String) map.get("model_id_dps");
            double demandQty = (Double) map.get("qty");
            Date fabDate = (Date) map.get("fab_date");
            DpsAryOc dpsAryOc = new DpsAryOc();
            dpsAryOc.setVer(dps_ver);
            dpsAryOc.setFab(fab);
            dpsAryOc.setProduct(product);
            dpsAryOc.setCreator("SYS");
            dpsAryOc.setFabDate(fabDate);
            dpsAryOc.setDemandQty(demandQty);
            dpsAryOc.setMemo("DPS同步");
            dpsAryOcList.add(dpsAryOc);
        }
        dpsAryOcRepository.saveAll(dpsAryOcList);
    }

    @Override
    public void syncDpsPackage() {
        log.info("同步CELL包材DPS >> START");
        List<String> verList = restService.getDpsCellAryVer();
        if(verList == null || verList.size()==0) return;
        for(String ver : verList) {
            List list = getDpsVerByFileVer(ver, DpsVer.Type_Package);
            if(list == null || list.size() == 0) {
                syncDpsPackage(ver);
            }
        }
        log.info("同步CELL包材DPS >> END");
    }

    @Override
    public void syncDpsPackage(String ver) {
        log.info("同步CELL包材DPS版本" + ver);
        List<String> productList = bomPackageService.getPackageProduct();
        List<Map> mapList = restService.getDpsPackage(ver, productList);
        if(mapList==null || mapList.size() == 0) return;
        String fab = "CELL";
        String dps_ver = generateDpsVer();
        DpsVer dpsVer = new DpsVer();
        dpsVer.setFab(fab);
        dpsVer.setDpsFile(ver);
        dpsVer.setVer(dps_ver);
        dpsVer.setSource(DpsVer.Source_Cell);
        dpsVer.setType(DpsVer.Type_Package);
        dpsVer.setCreator("SYS");
        Date startDate = null;
        Date endDate = null;
        List<DpsPackage> dpsPackageList = new ArrayList<>();
        for(Map map : mapList) {
            String product = (String) map.get("model_id_dps");
            double demandQty = (Double) map.get("qty");
            Date fabDate = (Date) map.get("fab_date");
            List<BomPackage> bomPackageList = bomPackageService.getBomPackage(product);
            for(BomPackage bomPackage : bomPackageList) {
                DpsPackage dpsPackage = new DpsPackage();
                dpsPackage.setVer(dps_ver);
                dpsPackage.setFab(fab);
                dpsPackage.setProduct(product);
                dpsPackage.setType(bomPackage.getType());
                dpsPackage.setLinkQty(bomPackage.getLinkQty());
                dpsPackage.setMode(bomPackage.getMode());
                dpsPackage.setCreator("SYS");
                dpsPackage.setFabDate(fabDate);
                dpsPackage.setDemandQty(demandQty);
                dpsPackage.setMemo("DPS同步");
                dpsPackageList.add(dpsPackage);
            }

            //日期区间
            if(startDate == null || startDate.after(fabDate)) {
                startDate =fabDate;
            }
            if(endDate == null || endDate.before(fabDate)) {
                endDate = fabDate;
            }
        }
        dpsVer.setStartDate(startDate);
        dpsVer.setEndDate(endDate);
        dpsVerRepository.save(dpsVer);
        dpsPackageRepository.saveAll(dpsPackageList);
    }


    @Override
    public List<String> getDpsAryProduct(String ver) {
        return dpsAryRepository.getProduct(ver);
    }

    @Override
    public List<String> getDpsAryOcProduct(String ver) {
        return dpsAryOcRepository.getProduct(ver);
    }

    @Override
    public List<String> getDpsCellProduct(String ver) {
        return dpsCellRepository.getProduct(ver);
    }

    @Override
    public List<String> getDpsLcmProduct(String ver) {
        return dpsLcmRepository.getProduct(ver);
    }

    @Override
    public List<DpsAry> getDpsAry(String ver, String product) {
        return dpsAryRepository.findByVerAndProduct(ver, product);
    }

    @Override
    public List<DpsAryOc> getDpsAryOc(String ver, String product) {
        return dpsAryOcRepository.findByVerAndProduct(ver, product);
    }

    @Override
    public List<DpsCell> getDpsCell(String ver, String product) {
        return dpsCellRepository.findByVerAndProduct(ver, product);
    }

    @Override
    public List<DpsLcm> getDpsLcm(String ver, String product) {
        return dpsLcmRepository.findByVerAndProduct(ver, product);
    }

    @Override
    public List<Map> getDpsPackageProduct(String ver) {
        return dpsPackageRepository.getProduct(ver);
    }

    @Override
    public List<DpsPackage> getDpsPackage(String ver, String product, String type, Double linkQty, String mode) {
        return dpsPackageRepository.findByVerAndProductAndTypeAndLinkQtyAndMode(ver, product, type, linkQty, mode);
    }

    @Override
    public Page<DpsVer> queryDpsVer(int page, int limit, String searchFab, String searchType, String searchVer) {
        Pageable pageable = PageRequest.of(page, limit, Sort.Direction.ASC, "ver");
        return dpsVerRepository.findByFabLikeAndTypeLikeAndVerLikeAndValidFlagIsTrue(searchFab+"%", searchType+"%",
                searchVer+"%", pageable);
    }

    @Override
    public List getDpsDate(String ver) {
        DpsVer dpsVer = getDpsVer(ver);
        if(dpsVer == null) return new ArrayList();
        String type = dpsVer.getType();
        List list;
        switch (type) {
            case DpsVer.Type_Ary:
                list = getDpsAry(ver);
                break;
            case DpsVer.Type_Cell:
                list = getDpsCell(ver);
                break;
            case DpsVer.Type_Lcm:
                list = getDpsLcm(ver);
                break;
            case DpsVer.Type_Package:
                list = getDpsPackage(ver);
                break;
            default:
                list = new ArrayList();
        }
        return list;
    }
}
