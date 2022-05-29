package com.xunmiw.user.service;

import com.xunmiw.enums.Sex;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.Fans;
import com.xunmiw.pojo.vo.FansCountVO;
import com.xunmiw.pojo.vo.RegionRatioVO;
import com.xunmiw.utils.PagedGridResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface FansService {

    /**
     * 关注作家
     * @param writerId
     * @param fanId
     */
    void follow(String writerId, String fanId);

    /**
     * 查询是否关注了该作家
     * @param writerId
     * @param fanId
     * @return
     */
    boolean isMeFollowThisWriter(String writerId, String fanId);

    /**
     * 取关作家
     * @param writerId
     * @param fanId
     */
    void unfollow(String writerId, String fanId);

    /**
     * 查询粉丝列表
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryAll(String writerId, Integer page, Integer pageSize);

    /**
     * 从Elasticsearch中查询粉丝列表
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryAllFromES(String writerId, Integer page, Integer pageSize);

    /**
     * 查询男女粉丝数量/比例
     * @param writerId
     * @param sex
     */
    Integer queryFansRatioAndCounts(String writerId, Sex sex);

    /**
     * 从Elasticsearch中查询男女粉丝数量/比例
     * @param writerId
     * @return
     */
    FansCountVO queryFansRatioAndCountsFromES(String writerId);

    /**
     * 根据地域查询粉丝数量/比例
     * @param writerId
     * @return
     */
    List<RegionRatioVO> queryFansRatioAndCountsByRegion(String writerId);

    /**
     * 从Elasticsearch中根据地域查询粉丝数量/比例
     * @param writerId
     * @return
     */
    List<RegionRatioVO> queryFansRatioAndCountsByRegionFromES(String writerId);

    /**
     * 被动更新粉丝用户信息
     * @param relationId
     * @param fanId
     */
    void forceUpdateFanInfo(String relationId, String fanId);
}
