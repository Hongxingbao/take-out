package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
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

    @Insert("insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) " +
            "values (#{setmeald},#{dishId},#{name},#{price},#{copies})")
    void insert(SetmealDish setmealDish);

    void insertBatch(List<SetmealDish> setmealDishes);
}
