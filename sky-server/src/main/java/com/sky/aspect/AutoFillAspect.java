package com.sky.aspect;

import com.sky.anno.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

//    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.anno.AutoFill)")
    @Pointcut("@annotation(com.sky.anno.AutoFill)")
    public void autoFillAspect() {
    }

    @Before("autoFillAspect()")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        log.info("已进入自动填充前置通知");
        //获取切入点注解对应的类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationType operationType = method.getAnnotation(AutoFill.class).value();

        //获取参数信息（约定需要填充的参数为第一个）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        //准备填充数据
        LocalDateTime localDateTime = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        //根据不同类型的操作，进行反射赋值
        if (operationType == OperationType.INSERT) {
            //获取方法
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //为四个公共字段赋值
            setCreateTime.invoke(entity, localDateTime);
            setCreateUser.invoke(entity, id);
            setUpdateTime.invoke(entity, localDateTime);
            setUpdateUser.invoke(entity, id);

        } else if (operationType == OperationType.UPDATE) {
            //获取方法
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //为两个公共字段赋值
            setUpdateTime.invoke(entity, localDateTime);
            setUpdateUser.invoke(entity, id);
        }
    }
    }
