package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private static final String key = "SHOP_STATUS";

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺状态为：{}", status == 1 ? "营业中" : "打样中");
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查看店铺营业状态")
    public Result getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("查看店铺状态为：{}", status == 1 ? "营业中" : "打样中");
        return Result.success(status);
    }
}
