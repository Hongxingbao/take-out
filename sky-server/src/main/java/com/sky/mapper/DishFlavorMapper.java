package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 添加菜品口味
     * @param flavors
     */
    void insert(List flavors);

    /**
     * 根据主键删除菜品
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    /**
     * 根据菜品id查询口味数据
     * @param id
     * @return
     */
    @Select("select  * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long dishId);
}
