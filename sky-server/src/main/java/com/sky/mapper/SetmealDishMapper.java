package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 查找菜品表是否关联着套餐表
     * @param dishIds
     * @return
     */
    public List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);
}
