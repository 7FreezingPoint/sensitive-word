package com.github.houbb.sensitive.word.core;

import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.sensitive.word.api.*;
import com.github.houbb.sensitive.word.support.replace.SensitiveWordReplaceContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 抽象实现
 *
 * @since 0.3.2
 */
public abstract class AbstractSensitiveWord implements ISensitiveWord {

    /**
     * 执行全部替换
     * @param string 字符串
     * @param context 上下文
     * @return 结果
     * @since 0.3.2
     */
    protected abstract List<IWordResult> doFindAll(String string, IWordContext context);

    /**
     * 替换
     * @param target 目标字符串
     * @param allList 敏感词列表
     * @param context 上下文
     * @return 结果
     * @since 0.3.2
     */
    protected String doReplace(String target, List<IWordResult> allList, IWordContext context) {
        // 根据 index 直接分割

        final ISensitiveWordReplace replace = context.sensitiveWordReplace();
        // 是否需要对 allList 排序？
        StringBuilder stringBuilder = new StringBuilder();

        // 注意边界
        int startIndex = 0;
        char[] chars = target.toCharArray();

        for(IWordResult wordResult : allList) {
            final int itemStartIx = wordResult.startIndex();
            final int itemEndIx = wordResult.endIndex();

            // 脱敏的左边
            if(startIndex < itemStartIx) {
                stringBuilder.append(chars, startIndex, itemStartIx-startIndex);
            }

            // 脱敏部分
            String word = wordResult.word();
            ISensitiveWordReplaceContext replaceContext = SensitiveWordReplaceContext.newInstance()
                    .sensitiveWord(word)
                    .wordLength(word.length());
            String replacedText = replace.replace(replaceContext);
            stringBuilder.append(replacedText);

            // 更新结尾
            startIndex = Math.max(startIndex, itemEndIx);
        }

        // 最后部分
        if (startIndex < chars.length) {
            stringBuilder.append(chars, startIndex, chars.length-startIndex);
        }

        return stringBuilder.toString();
    }

    @Override
    public List<IWordResult> findAll(String string, IWordContext context) {
        if(StringUtil.isEmpty(string)) {
            return Collections.emptyList();
        }

        return doFindAll(string, context);
    }

    @Override
    public IWordResult findFirst(String string, IWordContext context) {
        //TODO: 这个是懒惰的实现，性能一般。也可以调整为 FAST_OVER 模式。
        List<IWordResult> allList = findAll(string, context);
        if(CollectionUtil.isNotEmpty(allList)) {
            return allList.get(0);
        }

        return null;
    }

    @Override
    public String replace(String target, IWordContext context) {
        if(StringUtil.isEmpty(target)) {
            return target;
        }

        List<IWordResult> allList = findAll(target, context);
        if(CollectionUtil.isEmpty(allList)) {
            return target;
        }

        return doReplace(target, allList, context);
    }

    @Override
    public boolean contains(String string, IWordContext context) {
        //1. 第一个存在
        IWordResult firstResult = this.findFirst(string, context);
        return firstResult != null;
    }

}