package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartSercice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart/")
@Slf4j
@Api(tags = "购物车相关接口")
public class ShoppingCardController {

    @Autowired
    private ShoppingCartSercice shoppingCartSercice;

    /**
     * 添加购物车
     *
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车信息：{}",shoppingCartDTO);
        shoppingCartSercice.add(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 删除购物车中一个商品
     *
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车信息：{}",shoppingCartDTO);
        shoppingCartSercice.sub(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> showShoppingCart() {
        List<ShoppingCart> shoppingCarts = shoppingCartSercice.showShoppingCart();
        return Result.success(shoppingCarts);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result deleteAll(){
        shoppingCartSercice.clean();
        return Result.success();
    }
}
