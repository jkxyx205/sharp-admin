package com.rick.admin.config.dialect.processor;

import com.google.common.collect.Lists;
import com.rick.db.service.SharpService;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import com.rick.meta.props.service.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
public class SelectProcessor extends AbstractElementTagProcessor {

    /**
     * 标签名
     */
    private static final String TAG_NAME = "select";

    /**
     * 优先级
     */
    private static final int PRECEDENCE = 10000;

    private final DictService dictService;

    private final SharpService sharpService;

    public SelectProcessor(String dialectPrefix, DictService dictService, SharpService sharpService) {
        super(
                // 此处理器将仅应用于HTML模式
                TemplateMode.HTML,

                // 要应用于名称的匹配前缀
                dialectPrefix,

                // 标签名称：匹配此名称的特定标签 该内容就是在使用名称空间调用的 标签
                TAG_NAME,

                // 将标签前缀应用于标签名称
                true,

                // 无属性名称：将通过标签名称匹配
                null,

                // 没有要应用于属性名称的前缀
                false,

                // 优先(内部方言自己的优先)
                PRECEDENCE
        );
        this.dictService = dictService;
        this.sharpService = sharpService;
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext, IProcessableElementTag iProcessableElementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        //  获取前端页面传递的属性
        String key = iProcessableElementTag.getAttributeValue("key");
        String selected = iProcessableElementTag.getAttributeValue("value");
        String excludeValues = iProcessableElementTag.getAttributeValue("exclude");

        Map<String, String> attrMap = iProcessableElementTag.getAttributeMap();

        // 创建标签
        IModelFactory modelFactory = iTemplateContext.getModelFactory();
        IModel model = modelFactory.createModel();
        StringBuilder openElementString = new StringBuilder();
        openElementString.append("select");
        attrMap.forEach((key1, value) -> {
            openElementString.append(" ").append(key1);
            if (Objects.nonNull(value)) {
                openElementString.append("=\"").append(value).append("\"");
            }
        });

        model.add(modelFactory.createOpenElementTag(openElementString.toString()));

        String emptyItemText = iProcessableElementTag.hasAttribute("emptyItemText") ? StringUtils.defaultString(iProcessableElementTag.getAttributeValue("emptyItemText"), "") : "全部";

        if (!iProcessableElementTag.hasAttribute("hideAllItem")) {
            model.add(modelFactory.createOpenElementTag("option value=\"\"selected"));
            model.add(modelFactory.createText(emptyItemText));
            model.add(modelFactory.createCloseElementTag("option"));
        }

        if (iProcessableElementTag.hasAttribute("group")) {
            initGroupOptions(modelFactory, model, key, excludeValues, selected);
        } else {
            initOptions(modelFactory, model, key, excludeValues, selected);
        }

        model.add(modelFactory.createCloseElementTag("select"));
        iElementTagStructureHandler.replaceWith(model, false);
    }

    private void initOptions(IModelFactory modelFactory, IModel model, String key, String excludeValues, String selected) {
        // 进行数据的查询 根据 type 查询
        List<Dict> dictList = dictService.getDictByType(key);
        if (StringUtils.isNotBlank(excludeValues)) {
            List<String> excludeValueArr = Lists.newArrayList(excludeValues.split(","));
            dictList = dictList.stream().filter(dict -> !excludeValueArr.contains(dict.getName())).collect(Collectors.toList());
        }


        for (Dict dict : dictList) {
            model.add(modelFactory.createOpenElementTag(String.format("option value='%s'%s", dict.getName(),(Objects.equals(dict.getName(), selected) ? " selected" : ""))));
            model.add(modelFactory.createText(dict.getLabel()));
            model.add(modelFactory.createCloseElementTag("option"));
        }
    }

    private void initGroupOptions(IModelFactory modelFactory, IModel model, String key, String excludeValues, String selected) {
        // 进行数据的查询 根据 key 查询
        String querySql = PropertyUtils.getProperty(key);
        List<Map<String, Object>> valueList = sharpService.query(querySql, null);

        if (StringUtils.isNotBlank(excludeValues)) {
            List<String> excludeValueArr = Lists.newArrayList(excludeValues.split(","));
            valueList = valueList.stream().filter(kv -> !excludeValueArr.contains(kv.get("name"))).collect(Collectors.toList());
        }

        // 分组
        Map<String, List<Map<String, Object>>> groupNameMap = valueList.stream().collect(Collectors.groupingBy(a -> StringUtils.defaultString((String)a.get("parent_name"), "其他")));
        for (Map.Entry<String, List<Map<String, Object>>> entry: groupNameMap.entrySet()) {
            IModel optgroup = modelFactory.createModel();
            optgroup.add(modelFactory.createOpenElementTag("optgroup label=\""+entry.getKey()+"\""));

            for (Map<String, Object> option : entry.getValue()) {
                optgroup.add(modelFactory.createOpenElementTag(String.format("option value='%s'%s", option.get("id"),(Objects.equals(option.get("id"), selected) ? " selected" : ""))));
                optgroup.add(modelFactory.createText((CharSequence) option.get("name")));
                optgroup.add(modelFactory.createCloseElementTag("option"));
            }

            optgroup.add(modelFactory.createCloseElementTag("optgroup"));
            model.addModel(optgroup);
        }
    }
}
