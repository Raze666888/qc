package com.qc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.dto.BannerDTO;
import com.qc.entity.Banner;
import com.qc.exception.BusinessException;
import com.qc.mapper.BannerMapper;
import com.qc.vo.BannerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    /**
     * 分页查询轮播图
     */
    public IPage<BannerVO> getBannerPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<Banner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Banner::getStatus, status);
        }

        wrapper.eq(Banner::getDeleted, 0)
               .orderByAsc(Banner::getOrderNum)
               .orderByDesc(Banner::getCreateTime);

        IPage<Banner> result = bannerMapper.selectPage(page, wrapper);

        return result.convert(banner -> {
            BannerVO vo = new BannerVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        });
    }

    /**
     * 获取所有启用的轮播图（用于前端展示）
     */
    public List<BannerVO> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
               .eq(Banner::getDeleted, 0)
               .orderByAsc(Banner::getOrderNum);

        List<Banner> banners = bannerMapper.selectList(wrapper);
        return banners.stream().map(banner -> {
            BannerVO vo = new BannerVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据ID获取轮播图
     */
    public BannerVO getBannerById(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw new BusinessException("轮播图不存在");
        }

        BannerVO vo = new BannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }

    /**
     * 创建轮播图
     */
    public void createBanner(BannerDTO bannerDTO) {
        if (!StringUtils.hasText(bannerDTO.getTitle())) {
            throw new BusinessException("标题不能为空");
        }
        if (!StringUtils.hasText(bannerDTO.getImageUrl())) {
            throw new BusinessException("图片地址不能为空");
        }

        Banner banner = new Banner();
        BeanUtils.copyProperties(bannerDTO, banner);
        banner.setStatus(bannerDTO.getStatus() != null ? bannerDTO.getStatus() : 1);
        banner.setOrderNum(bannerDTO.getOrderNum() != null ? bannerDTO.getOrderNum() : 0);
        banner.setDeleted(0);

        bannerMapper.insert(banner);
        log.info("创建轮播图成功 - ID: {}, 标题: {}", banner.getId(), banner.getTitle());
    }

    /**
     * 更新轮播图
     */
    public void updateBanner(Long id, BannerDTO bannerDTO) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw new BusinessException("轮播图不存在");
        }

        if (StringUtils.hasText(bannerDTO.getTitle())) {
            banner.setTitle(bannerDTO.getTitle());
        }
        if (StringUtils.hasText(bannerDTO.getDescription())) {
            banner.setDescription(bannerDTO.getDescription());
        }
        if (StringUtils.hasText(bannerDTO.getImageUrl())) {
            banner.setImageUrl(bannerDTO.getImageUrl());
        }
        if (StringUtils.hasText(bannerDTO.getLinkUrl())) {
            banner.setLinkUrl(bannerDTO.getLinkUrl());
        }
        if (bannerDTO.getOrderNum() != null) {
            banner.setOrderNum(bannerDTO.getOrderNum());
        }
        if (bannerDTO.getStatus() != null) {
            banner.setStatus(bannerDTO.getStatus());
        }

        bannerMapper.updateById(banner);
        log.info("更新轮播图成功 - ID: {}, 标题: {}", id, banner.getTitle());
    }

    /**
     * 删除轮播图（逻辑删除）
     */
    public void deleteBanner(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw new BusinessException("轮播图不存在");
        }

        banner.setDeleted(1);
        bannerMapper.updateById(banner);
        log.info("删除轮播图成功 - ID: {}", id);
    }

    /**
     * 切换轮播图状态
     */
    public void toggleBannerStatus(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw new BusinessException("轮播图不存在");
        }

        banner.setStatus(banner.getStatus() == 1 ? 0 : 1);
        bannerMapper.updateById(banner);
        log.info("切换轮播图状态成功 - ID: {}, 新状态: {}", id, banner.getStatus());
    }
}
