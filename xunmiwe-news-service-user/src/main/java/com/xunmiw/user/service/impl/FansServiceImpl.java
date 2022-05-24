package com.xunmiw.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.enums.Sex;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.Fans;
import com.xunmiw.pojo.eo.FansEO;
import com.xunmiw.pojo.vo.RegionRatioVO;
import com.xunmiw.user.mapper.FansMapper;
import com.xunmiw.user.service.FansService;
import com.xunmiw.user.service.UserService;
import com.xunmiw.utils.PagedGridResult;
import com.xunmiw.utils.RedisOperator;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseService implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public static final String[] regions = {"北京", "天津", "上海", "重庆",
            "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆",
            "香港", "澳门"};

    @Override
    public boolean isMeFollowThisWriter(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);
        int count = fansMapper.selectCount(fans);
        return count > 0;
    }

    @Override
    @Transactional
    public void follow(String writerId, String fanId) {
        AppUser appUser = userService.getUser(fanId);

        Fans fans = new Fans();
        fans.setId(sid.nextShort());
        fans.setFanId(fanId);
        fans.setWriterId(writerId);

        fans.setFace(appUser.getFace());
        fans.setSex(appUser.getSex());
        fans.setFanNickname(appUser.getNickname());
        fans.setProvince(appUser.getProvince());

        int result = fansMapper.insert(fans);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // Redis作家粉丝数 & 当前用户关注数累加
        redisOperator.increment(REDIS_WRITER_FANS_COUNT + ":" + writerId, 1);
        redisOperator.increment(REDIS_USER_FOLLOW_COUNT + ":" + fanId, 1);

        // 保存粉丝关系到Elasticsearch中
        FansEO fansEO = new FansEO();
        BeanUtils.copyProperties(fans, fansEO);
        IndexQuery qry = new IndexQueryBuilder()
                            .withObject(fansEO)
                            .build();
        elasticsearchTemplate.index(qry);
    }

    @Override
    @Transactional
    public void unfollow(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);
        int result = fansMapper.delete(fans);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // Redis作家粉丝数 & 当前用户关注数累减
        redisOperator.decrement(REDIS_WRITER_FANS_COUNT + ":" + writerId, 1);
        redisOperator.decrement(REDIS_USER_FOLLOW_COUNT + ":" + fanId, 1);

        // 删除Elasticsearch中的粉丝关系
        DeleteQuery qry = new DeleteQuery();
        qry.setQuery(QueryBuilders.termQuery("writerId", writerId));
        qry.setQuery(QueryBuilders.termQuery("fanId", fanId));
        elasticsearchTemplate.delete(qry, FansEO.class);
    }

    @Override
    public PagedGridResult queryAll(String writerId, Integer page, Integer pageSize) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("writerId", writerId);

        PageHelper.startPage(page, pageSize);
        List<Fans> fans = fansMapper.selectByExample(example);
        return setPagedGrid(fans, page);
    }

    @Override
    public PagedGridResult queryAllFromES(String writerId, Integer page, Integer pageSize) {
        page--;
        Pageable pageable = PageRequest.of(page, pageSize);
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("writerId", writerId))
                .withPageable(pageable)
                .build();
        AggregatedPage<FansEO> fansEOS = elasticsearchTemplate.queryForPage(query, FansEO.class);

        PagedGridResult result = new PagedGridResult();
        result.setPage(page + 1);
        result.setRecords(fansEOS.getTotalElements());
        result.setRows(fansEOS.getContent());
        result.setTotal(fansEOS.getTotalPages());

        return result;
    }

    @Override
    public Integer queryFansRatioAndCounts(String writerId, Sex sex) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setSex(sex.type);

        int count = fansMapper.selectCount(fans);
        return count;
    }

    @Override
    public List<RegionRatioVO> queryFansRatioAndCountsByRegion(String writerId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);

        List<RegionRatioVO> result = new ArrayList<>();

        for (String region : regions) {
            fans.setProvince(region);
            int count = fansMapper.selectCount(fans);
            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(region);
            regionRatioVO.setValue(count);
            result.add(regionRatioVO);
        }

        return result;
    }

    @Override
    public void forceUpdateFanInfo(String relationId, String fanId) {
        // 1. 根据fanId查询粉丝最新信息
        AppUser appUser = userService.getUser(fanId);

        // 2. 更新用户信息到MySQL中
        Fans fans = new Fans();
        fans.setId(relationId);

        fans.setProvince(appUser.getProvince());
        fans.setFace(appUser.getFace());
        fans.setSex(appUser.getSex());
        fans.setFanNickname(appUser.getNickname());

        fansMapper.updateByPrimaryKeySelective(fans);

        // 3. 更新用户信息到Elasticsearch中
        Map<String, Object> map = new HashMap<>();
        map.put("sex", appUser.getSex());
        map.put("province", appUser.getProvince());
        map.put("face", appUser.getFace());
        map.put("fanNickname", appUser.getNickname());

        IndexRequest ir = new IndexRequest();
        ir.source(map);

        UpdateQuery query = new UpdateQueryBuilder()
                .withClass(FansEO.class)
                .withId(relationId)
                .withIndexRequest(ir)
                .build();

        elasticsearchTemplate.update(query);
    }
}
