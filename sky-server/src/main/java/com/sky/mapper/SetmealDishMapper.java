package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 查找菜品表是否关联着套餐表
     * @param dishIds
     * @return
     */
    public List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);


//    @Insert("insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) " +
//            "values (#{setmeald},#{dishId},#{name},#{price},#{copies})")
//    void insert(SetmealDish setmealDish);

    void insertBatch(List<SetmealDish> setmealDishes);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealIds(Long setmealId);
}
